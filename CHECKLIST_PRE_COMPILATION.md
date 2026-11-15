# 🛡️ Checklist de Pré-Compilação - IonDomo
**Criado pela IONKOD | Fundador: Douglas Lúcio**
**Padrão: IONfinance Security & Compliance**

## Status: 🔲 Em Progresso

---

## 1. Revisão de Segurança no Código
- [ ] Rodar `scripts/security_audit.sh`
- [ ] Verificar injeção SQL/NoSQL
- [ ] Verificar autenticação frouxa
- [ ] Verificar dados expostos em logs
- [ ] Verificar validação de input

## 2. Nenhum Segredo no Repositório
- [ ] Rodar `gitleaks detect`
- [ ] Verificar `.env` não rastreado
- [ ] Rotacionar chaves expostas (se houver)
- [ ] Verificar comentários com credenciais

## 3. Rate Limiting (Backend)
- [ ] Implementar middleware de rate limiting
- [ ] Limite: 5 tentativas de login/minuto por IP
- [ ] Limite: 10 contas/hora por IP
- [ ] Log de tentativas abusivas

## 4. SEO Mínimo
- [ ] `public/sitemap.xml` criado
- [ ] `public/robots.txt` criado
- [ ] Search Console configurado
- [ ] Meta tags Open Graph configuradas

## 5. Analytics (3 Eventos Definidos)
- [ ] Evento 1: `app_setup_completed`
- [ ] Evento 2: `first_telemetry_sync`
- [ ] Evento 3: `parent_dashboard_access`

## 6. Monitoramento de Erro com Alerta
- [ ] Sentry integrado (Android + Backend)
- [ ] Alertas configurados (Slack/Email)
- [ ] DSN do Sentry em variável de ambiente
- [ ] Testar envio de erro de exemplo

## 7. llms.txt
- [ ] `public/llms.txt` criado
- [ ] Descrição do produto legível para agentes de IA

## 8. Open Graph Configurado
- [ ] `og:title` definido
- [ ] `og:description` definido
- [ ] `og:image` definido (1200x630px)
- [ ] `og:url` definido

## 9. LGPD - Termos e Privacidade
- [ ] `docs/LGPD_PRIVACY_POLICY.md` criado
- [ ] `docs/LGPD_TERMS_OF_USE.md` criado
- [ ] Consentimento parental explícito (Art. 14 LGPD)
- [ ] Direito ao esquecimento implementado
- [ ] Retenção de dados definida

## 10. Backup do Banco Testado
- [ ] Estratégia de backup definida
- [ ] Backup automático diário
- [ ] **TESTE DE RESTAURAÇÃO** realizado
- [ ] Tempo de recuperação documentado (RTO/RPO)

## 11. E-mail Transacional Testado
- [ ] SMTP configurado (variável de ambiente)
- [ ] E-mail de recuperação de senha testado
- [ ] Verificar se cai em spam

## 12. Testar no Celular de Verdade
- [ ] Testar em Android físico (não emulador)
- [ ] Testar com internet ruim (3G/4G)
- [ ] Testar permissões especiais (UsageStats)

---

## Workflow de Deploy (Padrão IONfinance)
1. **Development** → Servidor de desenvolvimento
2. **Testing** → Testes automatizados + manuais
3. **Staging** → Ambiente de pré-produção
4. **Production** → Deploy final com release no GitHub

---
**Última atualização:** 2026-11-15
**Responsável:** Douglas Lúcio (IONKOD)
