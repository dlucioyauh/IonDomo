package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

var dbPool *pgxpool.Pool

// ===== TELEMETRIA (batch de todos os apps) =====
type AppUsage struct {
	AppName         string `json:"app_name"`
	DurationSeconds int    `json:"duration_seconds"`
}

type TelemetryPayload struct {
	DeviceID string     `json:"device_id"`
	Date     string     `json:"date"`
	Apps     []AppUsage `json:"apps"`
}

func handleTelemetry(c echo.Context) error {
	reqID := c.Response().Header().Get(echo.HeaderXRequestID)
	var payload TelemetryPayload
	if err := c.Bind(&payload); err != nil {
		log.Warn().Err(err).Str("request_id", reqID).Msg("Invalid telemetry payload")
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid payload"})
	}
	if payload.DeviceID == "" || len(payload.Apps) == 0 {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Missing device_id or apps"})
	}
	now := time.Now()
	for _, app := range payload.Apps {
		_, err := dbPool.Exec(context.Background(),
			`INSERT INTO app_usage (time, device_id, app_name, duration_seconds, date)
			 VALUES ($1, $2, $3, $4, $5)`,
			now, payload.DeviceID, app.AppName, app.DurationSeconds, payload.Date)
		if err != nil {
			log.Error().Err(err).Str("app", app.AppName).Msg("Failed to insert app usage")
		}
	}
	log.Info().Str("request_id", reqID).Str("device", payload.DeviceID).
		Int("apps_count", len(payload.Apps)).Msg("Telemetry batch saved")
	return c.JSON(http.StatusAccepted, map[string]interface{}{
		"status": "accepted", "apps_saved": len(payload.Apps),
	})
}

// ===== CATÁLOGO DE APPS (label + ícone + risco por FATOS) =====
type CatalogApp struct {
	AppName          string `json:"app_name"`
	Label            string `json:"label"`
	IsSystem         bool   `json:"is_system"`
	IconBase64       string `json:"icon_base64"`
	HasOverlay       bool   `json:"has_overlay"`
	HasAccessibility bool   `json:"has_accessibility"`
	InstallingPkg    string `json:"installing_package"`
	IsSideload       bool   `json:"is_sideload"`
}

// Lista negra CURADA de mods REAIS (nunca heurística por nome)
var modBlacklist = map[string]bool{
	"com.gbwhatsapp": true, "com.yowhatsapp": true, "com.whatsappplus": true,
	"com.fmwhatsapp": true, "com.gowhatsapp": true, "com.whatsapp.a1": true,
	"com.wa.mod": true, "com.gbinstafeed": true,
}

func computeRisk(a CatalogApp) (string, string) {
	var reasons []string
	level := "safe"
	if modBlacklist[a.AppName] {
		reasons = append(reasons, "App modificado (não oficial)")
		level = "high"
	}
	if a.HasAccessibility {
		reasons = append(reasons, "Acesso via Acessibilidade (pode burlar bloqueio)")
		level = "high"
	}
	if a.HasOverlay {
		reasons = append(reasons, "Desenha sobre outros apps")
		if level == "safe" {
			level = "caution"
		}
	}
	if a.IsSideload {
		reasons = append(reasons, "Instalado fora da Play Store")
		if level == "safe" {
			level = "caution"
		}
	}
	if len(reasons) == 0 {
		return "safe", ""
	}
	out := ""
	for i, r := range reasons {
		if i > 0 {
			out += "; "
		}
		out += r
	}
	return level, out
}

func handleAppCatalog(c echo.Context) error {
	reqID := c.Response().Header().Get(echo.HeaderXRequestID)
	var payload struct {
		DeviceID string       `json:"device_id"`
		Apps     []CatalogApp `json:"apps"`
	}
	if err := c.Bind(&payload); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid payload"})
	}
	saved := 0
	for _, a := range payload.Apps {
		level, reasons := computeRisk(a)
		_, err := dbPool.Exec(context.Background(),
			`INSERT INTO app_catalog (app_name, label, is_system, icon_base64, risk_level, risk_reasons, updated_at)
			 VALUES ($1, $2, $3, $4, $5, $6, NOW())
			 ON CONFLICT (app_name) DO UPDATE SET
				label = EXCLUDED.label, is_system = EXCLUDED.is_system,
				icon_base64 = EXCLUDED.icon_base64, risk_level = EXCLUDED.risk_level,
				risk_reasons = EXCLUDED.risk_reasons, updated_at = NOW()`,
			a.AppName, a.Label, a.IsSystem, a.IconBase64, level, reasons)
		if err == nil {
			saved++
		}
	}
	log.Info().Str("request_id", reqID).Int("apps", saved).Msg("App catalog synced")
	return c.JSON(http.StatusAccepted, map[string]int{"saved": saved})
}

// ===== DASHBOARD (label real + risco) =====
type DashboardApp struct {
	AppName         string `json:"app_name"`
	DurationSeconds int    `json:"duration_seconds"`
	IsBlocked       bool   `json:"is_blocked"`
	DailyLimit      int    `json:"daily_limit_seconds"`
	RequiresBible   bool   `json:"requires_bible_unlock"`
	IsSystem        bool   `json:"is_system"`
	Icon            string `json:"icon"`
	Label           string `json:"label"`
	RiskLevel       string `json:"risk_level"`
	RiskReasons     string `json:"risk_reasons"`
}

func handleGetDashboard(c echo.Context) error {
	reqID := c.Response().Header().Get(echo.HeaderXRequestID)
	query := `
		SELECT
			a.app_name,
			MAX(a.duration_seconds) as total_duration,
			COALESCE(p.is_blocked, FALSE) as is_blocked,
			COALESCE(p.daily_limit_seconds, 0) as daily_limit,
			COALESCE(p.requires_bible_unlock, FALSE) as requires_bible,
			COALESCE(ac.is_system, FALSE) as is_system,
			COALESCE(ac.icon_base64, '') as icon,
			COALESCE(ac.label, '') as label,
			COALESCE(ac.risk_level, 'safe') as risk_level,
			COALESCE(ac.risk_reasons, '') as risk_reasons
		FROM app_usage a
		LEFT JOIN parental_controls p ON a.device_id = p.device_id AND a.app_name = p.app_name
		LEFT JOIN app_catalog ac ON a.app_name = ac.app_name
		WHERE a.date = CURRENT_DATE AND a.app_name != 'com.ionkod.iondomo'
		GROUP BY a.app_name, p.is_blocked, p.daily_limit_seconds, p.requires_bible_unlock,
				 ac.is_system, ac.icon_base64, ac.label, ac.risk_level, ac.risk_reasons
		ORDER BY total_duration DESC
		LIMIT 50
	`
	rows, err := dbPool.Query(context.Background(), query)
	if err != nil {
		log.Error().Err(err).Str("request_id", reqID).Msg("Dashboard query failed")
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}
	defer rows.Close()
	var apps []DashboardApp
	totalTime := 0
	for rows.Next() {
		var app DashboardApp
		if err := rows.Scan(&app.AppName, &app.DurationSeconds, &app.IsBlocked, &app.DailyLimit,
			&app.RequiresBible, &app.IsSystem, &app.Icon, &app.Label, &app.RiskLevel, &app.RiskReasons); err != nil {
			log.Error().Err(err).Msg("Failed to scan dashboard row")
			continue
		}
		if app.IsSystem {
			continue
		}
		totalTime += app.DurationSeconds
		apps = append(apps, app)
	}
	log.Info().Str("request_id", reqID).Int("apps", len(apps)).Msg("Dashboard fetched")
	return c.JSON(http.StatusOK, map[string]interface{}{
		"status": "success", "data": apps, "total_time": totalTime,
		"date": time.Now().Format("2006-01-02"),
	})
}

// ===== CONTROLES PARENTAIS =====
func handleToggleBlock(c echo.Context) error {
	var req struct {
		DeviceID string `json:"device_id"`
		AppName  string `json:"app_name"`
		Blocked  bool   `json:"is_blocked"`
	}
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid request"})
	}
	_, err := dbPool.Exec(context.Background(),
		`INSERT INTO parental_controls (device_id, app_name, is_blocked, updated_at)
		 VALUES ($1, $2, $3, NOW())
		 ON CONFLICT (device_id, app_name) DO UPDATE SET
			is_blocked = EXCLUDED.is_blocked, updated_at = NOW()`,
		req.DeviceID, req.AppName, req.Blocked)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}
	log.Info().Str("device", req.DeviceID).Str("app", req.AppName).Bool("blocked", req.Blocked).Msg("Parental control updated")
	return c.JSON(http.StatusOK, map[string]string{"status": "updated"})
}

func handleSetLimit(c echo.Context) error {
	var req struct {
		DeviceID string `json:"device_id"`
		AppName  string `json:"app_name"`
		LimitSec int    `json:"daily_limit_seconds"`
	}
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid request"})
	}
	_, err := dbPool.Exec(context.Background(),
		`INSERT INTO parental_controls (device_id, app_name, daily_limit_seconds, updated_at)
		 VALUES ($1, $2, $3, NOW())
		 ON CONFLICT (device_id, app_name) DO UPDATE SET
			daily_limit_seconds = EXCLUDED.daily_limit_seconds, updated_at = NOW()`,
		req.DeviceID, req.AppName, req.LimitSec)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}
	return c.JSON(http.StatusOK, map[string]string{"status": "updated"})
}


type DeviceRules struct {
	BlockedApps []string          `json:"blocked_apps"`
	Limits      map[string]int    `json:"limits"` // app_name -> seconds
}

func handleGetRules(c echo.Context) error {
	deviceID := c.QueryParam("device_id")
	if deviceID == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "device_id required"})
	}

	query := `
		SELECT app_name, is_blocked, daily_limit_seconds 
		FROM parental_controls 
		WHERE device_id = $1 AND (is_blocked = TRUE OR daily_limit_seconds > 0)
	`
	rows, err := dbPool.Query(context.Background(), query, deviceID)
	if err != nil {
		log.Error().Err(err).Msg("Failed to fetch rules")
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}
	defer rows.Close()

	rules := DeviceRules{
		BlockedApps: []string{},
		Limits:      make(map[string]int),
	}

	for rows.Next() {
		var appName string
		var isBlocked bool
		var limit int
		if err := rows.Scan(&appName, &isBlocked, &limit); err != nil {
			continue
		}
		if isBlocked {
			rules.BlockedApps = append(rules.BlockedApps, appName)
		}
		if limit > 0 {
			rules.Limits[appName] = limit
		}
	}

	log.Info().Str("device", deviceID).Int("blocked", len(rules.BlockedApps)).Msg("Rules fetched")
	return c.JSON(http.StatusOK, rules)
}

func main() {
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr, TimeFormat: "15:04:05"})

	dbConfig, err := pgxpool.ParseConfig("postgres://iondomo:iondomo123@localhost:5432/iondomo_db")
	if err != nil {
		log.Fatal().Err(err).Msg("Unable to parse db config")
	}
	dbPool, err = pgxpool.NewWithConfig(context.Background(), dbConfig)
	if err != nil {
		log.Fatal().Err(err).Msg("Unable to create pool")
	}
	defer dbPool.Close()
	if err := dbPool.Ping(context.Background()); err != nil {
		log.Fatal().Err(err).Msg("Unable to ping database")
	}
	log.Info().Msg("Database connection established")

	e := echo.New()
	e.HideBanner = true
	e.Use(middleware.Recover())
	e.Use(middleware.RequestID())
	e.Use(middleware.Logger())
	e.Use(middleware.CORS())
	e.Use(middleware.BodyLimit("2M"))

	e.GET("/health", func(c echo.Context) error {
		return c.JSON(http.StatusOK, map[string]string{"status": "healthy"})
	})
	e.POST("/api/v1/telemetry", handleTelemetry)
	e.GET("/api/v1/dashboard", handleGetDashboard)
	e.POST("/api/v1/app-catalog", handleAppCatalog)
	e.POST("/api/v1/control/block", handleToggleBlock)
	e.POST("/api/v1/control/limit", handleSetLimit)
	e.GET("/api/v1/rules", handleGetRules)

	go func() {
		log.Info().Str("addr", "0.0.0.0:8080").Msg("IonDomo Backend started")
		if err := e.Start("0.0.0.0:8080"); err != nil && err != http.ErrServerClosed {
			log.Fatal().Err(err).Msg("Failed to start server")
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Info().Msg("Shutting down...")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	e.Shutdown(ctx)
	log.Info().Msg("Server stopped gracefully")
}
