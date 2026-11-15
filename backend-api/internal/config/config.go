package config

import (
    "os"
    "strconv"
    "strings"

    "github.com/joho/godotenv"
)

type Config struct {
    Server    ServerConfig
    Database  DatabaseConfig
    Sentry    SentryConfig
    Security  SecurityConfig
    RateLimit RateLimitConfig
    CORS      CORSConfig
}

type ServerConfig struct {
    Port string
    Env  string
    Host string
}

type DatabaseConfig struct {
    URL string
}

type SentryConfig struct {
    DSN         string
    Environment string
}

type SecurityConfig struct {
    JWTSecret string
}

type RateLimitConfig struct {
    RequestsPerMinute int
    Burst             int
}

type CORSConfig struct {
    Origins []string
}

func Load() (*Config, error) {
    env := getEnv("SERVER_ENV", "development")
    if env == "development" {
        _ = godotenv.Load()
    }

    return &Config{
        Server: ServerConfig{
            Port: getEnv("SERVER_PORT", "8080"),
            Env:  env,
            Host: getEnv("SERVER_HOST", "0.0.0.0"),
        },
        Database:  DatabaseConfig{URL: getEnv("DATABASE_URL", "")},
        Sentry:    SentryConfig{DSN: getEnv("SENTRY_DSN", ""), Environment: env},
        Security:  SecurityConfig{JWTSecret: getEnv("JWT_SECRET", "")},
        RateLimit: RateLimitConfig{
            RequestsPerMinute: getEnvAsInt("RATE_LIMIT_REQUESTS_PER_MINUTE", 60),
            Burst:             getEnvAsInt("RATE_LIMIT_BURST", 10),
        },
        CORS: CORSConfig{Origins: strings.Split(getEnv("CORS_ORIGINS", "http://localhost:3000"), ",")},
    }, nil
}

func getEnv(key, def string) string {
    if v := os.Getenv(key); v != "" {
        return v
    }
    return def
}

func getEnvAsInt(key string, def int) int {
    if v := os.Getenv(key); v != "" {
        if i, err := strconv.Atoi(v); err == nil {
            return i
        }
    }
    return def
}
