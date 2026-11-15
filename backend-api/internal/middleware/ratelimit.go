package middleware

import (
    "net/http"
    "sync"

    "github.com/labstack/echo/v4"
    "golang.org/x/time/rate"
)

type RateLimiter struct {
    ips   map[string]*rate.Limiter
    mu    *sync.RWMutex
    rate  rate.Limit
    burst int
}

func NewRateLimiter(rpm, burst int) *RateLimiter {
    return &RateLimiter{
        ips:   make(map[string]*rate.Limiter),
        mu:    &sync.RWMutex{},
        rate:  rate.Limit(float64(rpm) / 60.0),
        burst: burst,
    }
}

func (rl *RateLimiter) getLimiter(ip string) *rate.Limiter {
    rl.mu.Lock()
    defer rl.mu.Unlock()
    if lim, exists := rl.ips[ip]; exists {
        return lim
    }
    lim := rate.NewLimiter(rl.rate, rl.burst)
    rl.ips[ip] = lim
    return lim
}

func (rl *RateLimiter) Middleware() echo.MiddlewareFunc {
    return func(next echo.HandlerFunc) echo.HandlerFunc {
        return func(c echo.Context) error {
            if !rl.getLimiter(c.RealIP()).Allow() {
                return c.JSON(http.StatusTooManyRequests, map[string]string{
                    "error": "Rate limit exceeded",
                })
            }
            return next(c)
        }
    }
}
