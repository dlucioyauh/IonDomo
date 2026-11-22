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

func handleTelemetry(c echo.Context) error {
	reqID := c.Response().Header().Get(echo.HeaderXRequestID)
	
	var payload struct {
		DeviceID        string `json:"device_id"`
		AppName         string `json:"app_name"`
		DurationSeconds int    `json:"duration_seconds"`
		Date            string `json:"date"`
	}

	if err := c.Bind(&payload); err != nil {
		log.Warn().Err(err).Str("request_id", reqID).Msg("Invalid telemetry payload")
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "Invalid payload"})
	}

	query := `
		INSERT INTO telemetry (time, device_id, app_name, duration_seconds, date)
		VALUES ($1, $2, $3, $4, $5)
	`
	
	now := time.Now()
	_, err := dbPool.Exec(context.Background(), query, now, payload.DeviceID, payload.AppName, payload.DurationSeconds, payload.Date)
	if err != nil {
		log.Error().Err(err).Str("request_id", reqID).Msg("Failed to insert telemetry into database")
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
	}

	log.Info().
		Str("request_id", reqID).
		Str("device", payload.DeviceID).
		Str("app", payload.AppName).
		Int("duration", payload.DurationSeconds).
		Msg("Telemetry saved to database successfully")

	return c.JSON(http.StatusAccepted, map[string]string{
		"status":  "accepted",
		"message": "Telemetry saved successfully",
	})
}


func handleGetDashboard(c echo.Context) error {
    reqID := c.Response().Header().Get(echo.HeaderXRequestID)
    
    // Consulta agregada: soma o tempo de uso por app no dia de hoje
    query := `
        SELECT app_name, SUM(duration_seconds) as total_duration
        FROM telemetry
        WHERE date = CURRENT_DATE
        GROUP BY app_name
        ORDER BY total_duration DESC
        LIMIT 10
    `
    
    rows, err := dbPool.Query(context.Background(), query)
    if err != nil {
        log.Error().Err(err).Str("request_id", reqID).Msg("Failed to query dashboard data")
        return c.JSON(http.StatusInternalServerError, map[string]string{"error": "Database error"})
    }
    defer rows.Close()

    type AppUsage struct {
        AppName         string `json:"app_name"`
        DurationSeconds int    `json:"duration_seconds"`
    }
    
    var usageData []AppUsage
    for rows.Next() {
        var app AppUsage
        if err := rows.Scan(&app.AppName, &app.DurationSeconds); err != nil {
            log.Error().Err(err).Msg("Failed to scan row")
            continue
        }
        usageData = append(usageData, app)
    }

    log.Info().Str("request_id", reqID).Int("apps_count", len(usageData)).Msg("Dashboard data fetched successfully")
    return c.JSON(http.StatusOK, map[string]interface{}{
        "status": "success",
        "data":   usageData,
    })
}

func main() {
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr, TimeFormat: "15:04:05"})

	dbConfig, err := pgxpool.ParseConfig("postgres://iondomo:iondomo123@localhost:5432/iondomo_db")
	if err != nil {
		log.Fatal().Err(err).Msg("Unable to parse database config")
	}
	
	dbPool, err = pgxpool.NewWithConfig(context.Background(), dbConfig)
	if err != nil {
		log.Fatal().Err(err).Msg("Unable to create connection pool")
	}
	defer dbPool.Close()

	if err := dbPool.Ping(context.Background()); err != nil {
		log.Fatal().Err(err).Msg("Unable to ping database")
	}
	log.Info().Msg("Database connection established successfully")

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
	
	if err := e.Shutdown(ctx); err != nil {
		log.Error().Err(err).Msg("Server forced to shutdown")
	}
	log.Info().Msg("Server gracefully stopped")
}
