# IonDomo Backend API

Backend em Go seguindo padroes enterprise da IONKOD.

## Features
- Rate Limiting (protecao contra abuso)
- Sentry (monitoramento de erros)
- Structured Logging (observabilidade)
- Graceful Shutdown
- Zero Hardcoded Secrets
- Docker Ready
- LGPD Compliant

## Quick Start

    cp .env.example .env
    go mod tidy
    go run ./cmd/server
    curl http://localhost:8080/health

## Empresa
**IONKOD** | Douglas Lucio
