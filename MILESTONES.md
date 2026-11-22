# IonDomo - Roadmap & Milestones

## 🎯 Visão do Produto
Sistema enterprise de controle parental e bem-estar digital, com foco em privacidade (LGPD) e segurança de dados.

---

## ✅ MVP Completado (v0.1.0) - 2026-11-22

### O Que Funciona
- [x] Backend Go recebe telemetria via HTTP
- [x] App Android envia dados de teste
- [x] Comunicação ponta a ponta (3.5ms latency)
- [x] CI/CD pipeline com auditoria de segurança
- [x] Conformidade LGPD (12/12 pontos auditados)

### O Que Falta para Produção
- [ ] Coleta REAL de dados de uso (UsageStatsManager)
- [ ] Persistência em banco de dados (PostgreSQL + TimescaleDB)
- [ ] Criptografia de ponta a ponta (RSA + AES-GCM)
- [ ] Autenticação de usuários (OAuth2 + MFA)
- [ ] Dashboard web para pais
- [ ] Notificações de uso excessivo
- [ ] Exportação de dados (LGPD Art. 18)

---

## 🚧 Próximos Marcos (Prioridade)

### Marco 1: Coleta Real de Dados (v0.2.0)
**Prioridade: 🔴 CRÍTICA**
- Implementar UsageStatsManager no Android
- Coletar tempo de tela por aplicativo
- Enviar pacotes de telemetria estruturados
- **Por quê primeiro?** Sem dados reais, o app não entrega valor

### Marco 2: Persistência de Dados (v0.3.0)
**Prioridade: 🔴 CRÍTICA**
- PostgreSQL + TimescaleDB para séries temporais
- Schema para telemetria (device_id, app_name, duration, timestamp)
- API para consulta de histórico
- **Por quê segundo?** Dados coletados precisam ser armazenados

### Marco 3: Criptografia Real (v0.4.0)
**Prioridade: 🟡 ALTA**
- RSA-2048 para troca de chaves
- AES-GCM para criptografia de payload
- Chave pública embutida no app, privada no backend
- **Por quê terceiro?** LGPD exige, mas MVP já funciona

### Marco 4: Dashboard Web (v0.5.0)
**Prioridade: 🟡 ALTA**
- React/Next.js frontend
- Gráficos de tempo de tela (Chart.js)
- Autenticação OAuth2 + MFA
- Alertas configuráveis
- **Por quê quarto?** Precisa dos dados persistidos primeiro

### Marco 5: Produção (v1.0.0)
**Prioridade: 🟢 MÉDIA**
- Deploy em cloud (AWS/GCP)
- Monitoramento (Sentry + Prometheus)
- Backup automatizado
- Documentação de API (OpenAPI/Swagger)
- Testes de carga e segurança

---

## 📊 Status Atual

| Componente | Status | Progresso |
|---|---|---|
| Backend API | ✅ Funcional | 100% |
| App Android | ✅ Funcional (teste) | 40% (falta coleta real) |
| CI/CD | ✅ Funcional | 100% |
| Segurança | ✅ Auditada | 100% |
| Banco de Dados | ❌ Não iniciado | 0% |
| Dashboard | ❌ Não iniciado | 0% |
| Criptografia | ⚠️ Placeholder | 10% |

---

## 🎓 Lições Aprendidas

1. **MIUI (Xiaomi) bloqueia instalação via ADB** → Solução: instalação manual via Gerenciador de Arquivos
2. **Sentry plugin injeta ContentProvider** → Solução: remover dependência completamente
3. **Branch protection sem admin bypass trava merge solo** → Solução: adicionar Repository admin ao bypass list
4. **Gradle 8.2 incompatível com Java 27** → Solução: usar JDK 17
5. **Emulador Android precisa de KVM** → Solução: instalar qemu-full e adicionar usuário ao grupo kvm

---

*Última atualização: 2026-11-22*
