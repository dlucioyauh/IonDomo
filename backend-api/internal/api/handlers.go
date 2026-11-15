package api

import (
    "net/http"
    "time"

    "github.com/labstack/echo/v4"
)

type Handlers struct {
    startTime time.Time
}

func NewHandlers() *Handlers {
    return &Handlers{startTime: time.Now()}
}

func (h *Handlers) HealthCheck(c echo.Context) error {
    return c.JSON(http.StatusOK, map[string]interface{}{
        "status":    "healthy",
        "service":   "iondomo-backend",
        "version":   "1.0.0",
        "uptime":    time.Since(h.startTime).String(),
        "timestamp": time.Now().UTC().Format(time.RFC3339),
    })
}

func (h *Handlers) ReceiveTelemetry(c echo.Context) error {
    return c.JSON(http.StatusAccepted, map[string]string{
        "status":  "accepted",
        "message": "Telemetry queued for processing",
    })
}
