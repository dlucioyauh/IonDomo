package main

import (
    "context"
    "fmt"
    "net/http"
    "os"
    "os/signal"
    "syscall"
    "time"

    "github.com/getsentry/sentry-go"
    "github.com/rs/zerolog/log"

    "github.com/dlucioyauh/IonDomo/backend-api/internal/api"
    "github.com/dlucioyauh/IonDomo/backend-api/internal/config"
    "github.com/dlucioyauh/IonDomo/backend-api/internal/middleware"
)

func main() {
    cfg, err := config.Load()
    if err != nil {
        log.Fatal().Err(err).Msg("Config load failed")
    }

    middleware.SetupLogger(cfg.Server.Env)

    if cfg.Sentry.DSN != "" {
        if err := sentry.Init(sentry.ClientOptions{
            Dsn:         cfg.Sentry.DSN,
            Environment: cfg.Sentry.Environment,
            Release:     "iondomo-backend@1.0.0",
        }); err == nil {
            defer sentry.Flush(2 * time.Second)
            log.Info().Msg("Sentry initialized")
        }
    }

    router := api.SetupRouter(cfg)
    addr := fmt.Sprintf("%s:%s", cfg.Server.Host, cfg.Server.Port)
    srv := &http.Server{
        Addr:         addr,
        Handler:      router,
        ReadTimeout:  15 * time.Second,
        WriteTimeout: 15 * time.Second,
    }

    go func() {
        log.Info().Str("addr", addr).Msg("IonDomo Backend started")
        if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
            log.Fatal().Err(err).Msg("Server failed")
        }
    }()

    quit := make(chan os.Signal, 1)
    signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
    <-quit

    log.Info().Msg("Shutting down...")
    ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
    defer cancel()
    srv.Shutdown(ctx)
    log.Info().Msg("Server gracefully stopped")
}
