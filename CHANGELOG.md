# Changelog - IonDomo

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [0.1.0] - 2026-11-22

### 🎉 Conquistas Épicas

#### CI/CD & Segurança Enterprise
- ✅ Pipeline GitHub Actions completo com `gitleaks` para detecção de secrets
- ✅ Branch protection rules com required status checks
- ✅ Code owners configurado para revisão obrigatória
- ✅ Admin bypass configurado para fundador (dlucioyauh)
- ✅ Security audit script com 12 pontos de conformidade LGPD
- ✅ Zero secrets hardcoded no repositório (gitleaks clean)

#### Backend Go (API REST)
- ✅ Servidor HTTP com Echo framework
- ✅ Rate limiting configurado (100 req/min por IP)
- ✅ Logging estruturado com zerolog
- ✅ Graceful shutdown (SIGTERM/SIGINT)
- ✅ Middleware de tracing (X-Request-Id)
- ✅ Health check endpoint (`/health`)
- ✅ Telemetry endpoint (`POST /api/v1/telemetry`) - Status 202 Accepted
- ✅ CORS configurado para desenvolvimento local

#### Agente Android (Kotlin)
- ✅ App compila com Gradle 8.2 + JDK 17
- ✅ MainActivity funcional com teste de conectividade
- ✅ Network security config para HTTP local (cleartext)
- ✅ Permissões: INTERNET + PACKAGE_USAGE_STATS
- ✅ Teste de integração bem-sucedido: Poco M5 → Backend Go
- ✅ Latência: 3.5ms (Wi-Fi local)

#### Infraestrutura & DevOps
- ✅ KVM habilitado no Arch Linux para emulação Android
- ✅ ADB configurado para dispositivos físicos (Poco M5)
- ✅ Bypass de restrições MIUI (instalação manual via APK)
- ✅ Sentry removido do build (causava crash por DSN faltante)

### 🔧 Correções Críticas
- Fix: Removido plugin Sentry (causava `IllegalArgumentException: DSN is required`)
- Fix: Corrigido `grep -rnwE` no workflow (sintaxe quebrada)
- Fix: Alinhado nome do status check com branch protection rule
- Fix: Adicionado admin bypass para permitir merge solo

### 📊 Métricas
- Commits: 12+
- Pull Requests: 17 (1 merged)
- Checks CI/CD: 2 (Android Build + Security Audit) - ambos verdes
- Dispositivos testados: Poco M5 (Android 14, MIUI)
- Backend: Go 1.21+ rodando em Arch Linux

---

## [0.0.1] - 2026-11-21

### 🚀 Fundação do Projeto
- Initial secure project structure
- GitHub Actions security audit workflow
- Go backend com rate limiting e Open Graph
- Android Manifest e Gradle config com Sentry
- Pre-compilation infrastructure via Python
- Clean README.md (post-history-cleanup)
