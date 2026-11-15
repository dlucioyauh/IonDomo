# IonDomo - Enterprise Parental Control System

[![IONKOD Security Audit](https://github.com/dlucioyauh/IonDomo/actions/workflows/security-audit.yml/badge.svg)](https://github.com/dlucioyauh/IonDomo/actions/workflows/security-audit.yml)

## 🛡️ Visão Geral

**IonDomo** é uma solução enterprise de controle parental e bem-estar digital desenvolvida pela **IONKOD**. Projetada para pais e responsáveis legais monitorarem o uso de dispositivos digitais por crianças e adolescentes com transparência, segurança e conformidade com a LGPD.

## ✨ Funcionalidades Principais

- **Gerenciamento de Tempo de Tela**: Limites diários, agendamentos e restrições por aplicativo
- **Filtragem de Conteúdo**: Bloqueio de sites e aplicativos inadequados
- **Análise de Uso**: Rastreamento de uso de aplicativos, tempo de tela e hábitos digitais
- **Telemetria Criptografada**: Todos os dados são criptografados de ponta a ponta (AES-256-GCM + RSA-2048)
- **Conformidade com LGPD**: Total conformidade com a Lei Geral de Proteção de Dados, especialmente Artigo 14 (dados de crianças)

## 🏗️ Stack Tecnológica

### Mobile Agent (Android)
- **Linguagem**: Kotlin
- **Segurança**: AndroidX Security (EncryptedSharedPreferences)
- **Background Tasks**: WorkManager
- **Monitoramento de Erros**: Sentry
- **Rede**: Retrofit + OkHttp

### Backend API
- **Stack**: A definir (Go/Node.js/Python)
- **Banco de Dados**: PostgreSQL + TimescaleDB
- **Autenticação**: OAuth2 + MFA
- **Criptografia**: RSA-2048 + AES-256-GCM

### CI/CD
- **Pipeline**: GitHub Actions
- **Auditoria de Segurança**: gitleaks
- **Deploy**: Automatizado com aprovação manual

## 🔒 Segurança e Conformidade

### Princípios de Segurança
- **Zero hardcoded secrets**: Todas as credenciais em variáveis de ambiente
- **Auditoria automatizada**: gitleaks em todo push/PR
- **Princípio do menor privilégio**: Apenas permissões necessárias
- **Criptografia de ponta a ponta**: Dados sensíveis nunca em texto plano

### Conformidade LGPD
- Consentimento parental explícito (Art. 14)
- Minimização de dados
- Direito ao esquecimento (retenção: 90 dias)
- Documentação completa de privacidade

### Documentação de Segurança
- [Política de Privacidade LGPD](docs/LGPD_PRIVACY_POLICY.md)
- [Termos de Uso](docs/LGPD_TERMS_OF_USE.md)
- [Estratégia de Backup](docs/BACKUP_STRATEGY.md)
- [Checklist de Pré-Compilação](CHECKLIST_PRE_COMPILATION.md)

## 📊 Analytics

Apenas 3 eventos essenciais são rastreados (sem dados pessoais):
1. `app_setup_completed` - Configuração inicial concluída
2. `first_telemetry_sync` - Primeira sincronização de dados
3. `parent_dashboard_access` - Primeiro acesso ao painel

[Documentação completa](docs/ANALYTICS_EVENTS.md)

## 🚀 Desenvolvimento

### Pré-requisitos
- Android Studio (última versão estável)
- JDK 17+
- Android SDK 34
- gitleaks (para auditoria de segurança)

### Configuração Local

```bash
# Clonar repositório
git clone https://github.com/dlucioyauh/IonDomo.git
cd IonDomo

# Instalar gitleaks (Arch Linux)
curl -L -o gitleaks.tar.gz https://github.com/gitleaks/gitleaks/releases/download/v8.18.1/gitleaks_8.18.1_linux_x64.tar.gz
tar -xzf gitleaks.tar.gz && sudo mv gitleaks /usr/local/bin/ && rm gitleaks.tar.gz

# Rodar auditoria de segurança
./scripts/security_audit.sh

# Abrir no Android Studio
# File → Open → Selecionar pasta android-agent
```

### Workflow de Desenvolvimento

Seguimos o padrão IONfinance:
1. Issue criada no GitHub
2. Branch criada: `feature/issue-#-descricao`
3. Desenvolvimento + auditoria local
4. PR para `main` com code review obrigatório
5. Deploy automático em staging
6. Aprovação do CHEFE → Deploy em produção

[Documentação completa](docs/DEVELOPMENT_WORKFLOW.md)

## 📁 Estrutura do Projeto

```
IonDomo/
├── android-agent/              # Aplicativo Android (agente)
│   ├── src/main/
│   │   ├── java/com/ionkod/iondomo/
│   │   │   ├── config/        # Gerenciamento de configuração segura
│   │   │   ├── telemetry/     # Worker de telemetria criptografada
│   │   │   └── utils/         # Utilitários de criptografia
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── backend-api/                # API Backend (a implementar)
├── docs/                       # Documentação completa
├── public/                     # Arquivos públicos (SEO, llms.txt)
├── scripts/                    # Scripts de automação
├── .github/                    # CI/CD
└── CHECKLIST_PRE_COMPILATION.md
```

## 🤖 Para Agentes de IA

Este projeto inclui [llms.txt](public/llms.txt) com documentação estruturada para agentes de IA entenderem o propósito, arquitetura e requisitos de segurança do IonDomo.

## 👥 Empresa

**IONKOD**
- **Fundador**: Douglas Lúcio
- **GitHub**: [@dlucioyauh](https://github.com/dlucioyauh)
- **LinkedIn**: [IONKOD](https://www.linkedin.com/company/144483944/)
- **Perfil**: [Douglas Lúcio](https://www.linkedin.com/in/douglaslucio/)

## ⚠️ Aviso Legal

Este produto é destinado **exclusivamente** para monitoramento parental legítimo de crianças e adolescentes sob responsabilidade legal do usuário. Monitorar adultos sem consentimento é estritamente proibido e ilegal.

Todos os dados de crianças são tratados com máxima segurança e privacidade, em conformidade com o Artigo 14 da LGPD.

## 📄 Licença

Proprietário - IONKOD © 2026. Todos os direitos reservados.

---

**Desenvolvido com ❤️ pela IONKOD seguindo os mais altos padrões de segurança e conformidade.**
