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

// === NOVO PAYLOAD: array de apps ===
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
		log.Warn().Err(err).Str("request_id", reqID).Msg("Invalid payload")
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

	log.Info().
		Str("request_id", reqID).
		Str("device", payload.DeviceID).
		Int("apps_count", len(payload.Apps)).
		Msg("Telemetry batch saved")

	return c.JSON(http.StatusAccepted, map[string]interface{}{
		"status":  "accepted",
		"apps_saved": len(payload.Apps),
	})
}

// === DASHBOARD: retorna TODOS os apps agregados do dia ===
type DashboardApp struct {
	AppName         string `json:"app_name"`
	DurationSeconds int    `json:"duration_seconds"`
	IsBlocked       bool   `json:"is_blocked"`
	DailyLimit      int    `json:"daily_limit_seconds"`
	RequiresBible   bool   `json:"requires_bible_unlock"`
}

func handleGetDashboard(c echo.Context) error {
	reqID := c.Response().Header().Get(echo.HeaderXRequestID)

	query := `
		SELECT 
			a.app_name,
			SUM(a.duration_seconds) as total_duration,
			COALESCE(p.is_blocked, FALSE) as is_blocked,
			COALESCE(p.daily_limit_seconds, 0) as daily_limit,
			COALESCE(p.requires_bible_unlock, FALSE) as requires_bible
		FROM app_usage a
		LEFT JOIN parental_controls p ON a.device_id = p.device_id AND a.app_name = p.app_name
		WHERE a.date = CURRENT_DATE
		AND a.app_name NOT LIKE 'com.android.%'
		AND a.app_name NOT LIKE 'com.miui.%'
		AND a.app_name NOT LIKE 'com.mi.android.%'
		AND a.app_name NOT LIKE 'com.miuix.%'
		AND a.app_name NOT LIKE 'com.qualcomm.%'
		AND a.app_name NOT LIKE 'com.google.android.gms%'
		AND a.app_name NOT LIKE 'com.google.android.gsf%'
		AND a.app_name NOT LIKE 'com.google.android.providers%'
		AND a.app_name NOT LIKE 'android%'
		AND a.app_name NOT LIKE '%.settings%'
		AND a.app_name NOT LIKE '%.permissioncontroller%'
		AND a.app_name NOT LIKE '%.securitycenter%'
		AND a.app_name NOT LIKE '%.cleaner%'
		AND a.app_name NOT LIKE '%.fileexplorer%'
		AND a.app_name NOT LIKE '%.vrrh%'
		AND a.app_name NOT LIKE '%.framework%'
		AND a.app_name NOT LIKE '%.launcher%'
		GROUP BY a.app_name, p.is_blocked, p.daily_limit_seconds, p.requires_bible_unlock
		ORDER BY total_duration DESC
		LIMIT 20
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
		if err := rows.Scan(&app.AppName, &app.DurationSeconds, &app.IsBlocked, &app.DailyLimit, &app.RequiresBible); err != nil {
			continue
		}
		totalTime += app.DurationSeconds
		apps = append(apps, app)
	}

	log.Info().Str("request_id", reqID).Int("apps", len(apps)).Msg("Dashboard fetched")
	return c.JSON(http.StatusOK, map[string]interface{}{
		"status":     "success",
		"data":       apps,
		"total_time": totalTime,
		"date":       time.Now().Format("2006-01-02"),
	})
}

// === CONTROLE PARENTAL: bloquear/desbloquear app ===
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
		 ON CONFLICT (device_id, app_name) 
		 DO UPDATE SET is_blocked = EXCLUDED.is_blocked, updated_at = NOW()`,
		req.DeviceID, req.AppName, req.Blocked)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}

	log.Info().Str("device", req.DeviceID).Str("app", req.AppName).Bool("blocked", req.Blocked).Msg("Parental control updated")
	return c.JSON(http.StatusOK, map[string]string{"status": "updated"})
}

// === CONTROLE PARENTAL: definir limite de tempo ===
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
		 ON CONFLICT (device_id, app_name) 
		 DO UPDATE SET daily_limit_seconds = EXCLUDED.daily_limit_seconds, updated_at = NOW()`,
		req.DeviceID, req.AppName, req.LimitSec)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}

	return c.JSON(http.StatusOK, map[string]string{"status": "updated"})
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

	e.GET("/health", func(c echo.Context) error {
		return c.JSON(http.StatusOK, map[string]string{"status": "healthy"})
	})
	e.POST("/api/v1/telemetry", handleTelemetry)
	e.GET("/api/v1/dashboard", handleGetDashboard)
	e.POST("/api/v1/control/block", handleToggleBlock)
	e.POST("/api/v1/control/limit", handleSetLimit)

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
