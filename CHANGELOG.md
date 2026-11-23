# Changelog - IonDomo

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [0.4.1] - 2026-11-22

### 🎨 UX: Filtrar apps de sistema + Ícones reais
- ✅ Backend: query com NOT LIKE para pacotes MIUI/Android/Google internos
- ✅ Frontend: `isSystemApp()` como filtro duplo (defesa em profundidade)
- ✅ Catálogo `appCatalog.ts` com ícones do Google Play (Instagram, WhatsApp, YouTube, TikTok, ChatGPT, Discord, Roblox, Minecraft, Netflix, Spotify, etc.)
- ✅ Fallback elegante com inicial colorida + cor determinística
- ✅ Barra de busca por nome de app
- ✅ LGPD: minimização de dados (apps de sistema removidos)

### 🎮 Roadmap de Gamificação (Issues #7-#11)
- #7 XP + ranking mundial anonimizado
- #8 9 Coros Angelicais (patentes)
- #9 Classes de Personagens Bíblicos
- #10 Desafios de Treino Físico (LGPD Art.11)
- #11 Bíblia Unlock (motor de conteúdo + quiz)

---

## [0.4.0] - 2026-11-22

### 🎮 Nova Visão: Discipulado Gamificado
- ✅ Migration v0.4: tabelas `app_usage`, `parental_controls`, `bible_unlocks`, `devices`
- ✅ Backend: coleta de TODOS os apps (batch) + endpoints de controle parental
- ✅ Dashboard 3D gamificado (padrão Ionkod Premium) com toggles de bloqueio/limite
- 🚧 Issues #7-#11 criadas: ranking XP, coros angelicais, classes bíblicas, desafios físicos, Bíblia Unlock
- 🔧 Fix: removido import 'encoding/json' não usado (crash do backend)

### 🎯 Reposicionamento do Produto
IonDomo evolui de 'controle parental passivo' para 'plataforma de discipulado gamificado com controle parental ativo'.

---

## [0.3.0] - 2026-11-22

### 🎨 Dashboard Web (Marco 2 Concluído)
- ✅ Next.js 16.4.6 com App Router + Turbopack
- ✅ Gráfico de barras (Recharts) com dados reais de telemetria
- ✅ Endpoint `GET /api/v1/dashboard` no backend Go
- ✅ Query SQL agregada: `SUM(duration_seconds) GROUP BY app_name`
- ✅ Cards de resumo: Dispositivo, Apps Monitorados, Status
- ✅ Testado end-to-end: Poco M5 → Go → TimescaleDB → Browser
- ✅ Dados reais: Instagram 4h26m, WhatsApp 20min

### 🔧 Backend Go (v0.3.0)
- ✅ `pgxpool` para pool de conexões PostgreSQL
- ✅ Handler `handleGetDashboard` com query otimizada
- ✅ Logging estruturado com contagem de apps
- ✅ Latência média: 7.9ms

### 📊 Métricas Atualizadas
- Commits: 15+
- Endpoints API: 3 (health, telemetry, dashboard)
- Camadas funcionais: 6 (coleta, transporte, API, DB, UI, CI/CD)
- Dispositivos reais testados: Poco M5 (Android 14, MIUI)

---

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
