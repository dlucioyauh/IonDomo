package api

import (
    "github.com/labstack/echo/v4"
    echomw "github.com/labstack/echo/v4/middleware"

    "github.com/dlucioyauh/IonDomo/backend-api/internal/config"
    "github.com/dlucioyauh/IonDomo/backend-api/internal/middleware"
)

func SetupRouter(cfg *config.Config) *echo.Echo {
    e := echo.New()
    e.HideBanner = true

    e.Use(echomw.Recover())
    e.Use(echomw.RequestID())
    e.Use(middleware.Logger())
    e.Use(echomw.CORSWithConfig(echomw.CORSConfig{
        AllowOrigins: cfg.CORS.Origins,
        AllowMethods: []string{"GET", "POST", "OPTIONS"},
    }))

    rl := middleware.NewRateLimiter(cfg.RateLimit.RequestsPerMinute, cfg.RateLimit.Burst)
    e.Use(rl.Middleware())

    handlers := NewHandlers()
    e.GET("/health", handlers.HealthCheck)
    e.POST("/api/v1/telemetry", handlers.ReceiveTelemetry)
    e.GET("/", func(c echo.Context) error {
        return c.HTML(200, landingPageHTML)
    })

    return e
}

const landingPageHTML = `<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IonDomo - Controle Parental Enterprise | IONKOD</title>
    <meta name="description" content="Solucao enterprise de controle parental com criptografia ponta a ponta e conformidade LGPD.">
    <meta property="og:title" content="IonDomo - Controle Parental Enterprise">
    <meta property="og:description" content="Proteja seus filhos online com seguranca enterprise. Criptografia E2E, LGPD compliant.">
    <meta property="og:type" content="website">
    <meta property="og:url" content="https://iondomo.ionkod.com">
    <meta property="og:image" content="https://iondomo.ionkod.com/og-image.png">
    <meta property="og:site_name" content="IonDomo">
    <meta property="og:locale" content="pt_BR">
    <meta name="twitter:card" content="summary_large_image">
    <style>
        body { font-family: system-ui, sans-serif; margin: 0; padding: 40px; background: #0a0a0a; color: #fff; }
        .container { max-width: 800px; margin: 0 auto; }
        h1 { font-size: 3rem; }
        .badge { background: #3b82f6; padding: 4px 12px; border-radius: 999px; font-size: 0.875rem; }
        .feature { background: #1a1a1a; padding: 20px; border-radius: 8px; margin: 16px 0; }
        a { color: #3b82f6; }
    </style>
</head>
<body>
    <div class="container">
        <span class="badge">IONKOD Enterprise</span>
        <h1>IonDomo</h1>
        <p>Solucao enterprise de controle parental e bem-estar digital.</p>
        <div class="feature"><h3>Seguranca Enterprise</h3><p>Criptografia ponta a ponta (RSA-2048 + AES-256-GCM).</p></div>
        <div class="feature"><h3>LGPD Compliant</h3><p>Conformidade total com a LGPD, Artigo 14.</p></div>
        <div class="feature"><h3>Arquitetura</h3><p>Go, PostgreSQL + TimescaleDB, Sentry.</p></div>
        <p>Desenvolvido por <a href="https://www.linkedin.com/in/douglaslucio/">Douglas Lucio</a> | <a href="https://www.linkedin.com/company/144483944/">IONKOD</a></p>
    </div>
</body>
</html>`
