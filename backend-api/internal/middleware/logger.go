package middleware

import (
    "time"

    "github.com/labstack/echo/v4"
    "github.com/rs/zerolog"
    "github.com/rs/zerolog/log"
)

func SetupLogger(env string) {
    if env == "development" {
        log.Logger = log.Output(zerolog.ConsoleWriter{Out: zerolog.ConsoleTestWriter()})
        zerolog.SetGlobalLevel(zerolog.DebugLevel)
    } else {
        zerolog.SetGlobalLevel(zerolog.InfoLevel)
    }
}

func Logger() echo.MiddlewareFunc {
    return func(next echo.HandlerFunc) echo.HandlerFunc {
        return func(c echo.Context) error {
            start := time.Now()
            err := next(c)
            log.Info().
                Str("method", c.Request().Method).
                Str("path", c.Request().URL.Path).
                Int("status", c.Response().Status).
                Str("ip", c.RealIP()).
                Dur("latency", time.Since(start)).
                Err(err).
                Msg("http_request")
            return err
        }
    }
}
