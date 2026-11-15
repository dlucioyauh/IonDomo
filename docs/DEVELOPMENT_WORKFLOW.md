# 🔄 Workflow de Desenvolvimento - IonDomo
**Criado pela IONKOD | Fundador: Douglas Lúcio**
**Padrão: IONfinance Development Workflow**

---

## Ambientes

### 1. Development (dev)
- **URL:** `dev.iondomo.ionkod.com`
- **Propósito:** Desenvolvimento e testes locais
- **Dados:** Dados fictícios/sintéticos
- **Deploy:** Automático a cada push para branch `develop`

### 2. Testing (qa)
- **URL:** `qa.iondomo.ionkod.com`
- **Propósito:** Testes automatizados e manuais
- **Deploy:** Automático após aprovação de PR

### 3. Staging
- **URL:** `staging.iondomo.ionkod.com`
- **Propósito:** Pré-produção, validação final
- **Deploy:** Manual após aprovação do CHEFE

### 4. Production
- **URL:** `iondomo.ionkod.com`
- **Propósito:** Ambiente de produção
- **Deploy:** Manual com release no GitHub

---

## Fluxo de Desenvolvimento

1. Issue criada no GitHub
2. Branch criada: `feature/issue-#-descricao`
3. Desenvolvimento local
4. Security audit local (`scripts/security_audit.sh`)
5. Push para develop → Deploy automático em dev
6. Testes automatizados (unitários + integração)
7. Pull Request para main
8. Code review obrigatório
9. Security audit no CI/CD (GitHub Actions)
10. Merge para main → Deploy em staging
11. Validação manual em staging
12. Aprovação do CHEFE
13. Release no GitHub com ficha técnica
14. Deploy em production
15. Monitoramento pós-deploy (Sentry + Analytics)

---

## Regras de Branch

| Branch | Propósito | Deploy |
|--------|-----------|--------|
| `main` | Produção | Staging → Production |
| `develop` | Desenvolvimento | Dev |
| `feature/*` | Novas funcionalidades | Nenhum |
| `bugfix/*` | Correções | Nenhum |
| `hotfix/*` | Correções críticas | Production (emergência) |

---

## Convenções de Commit (Padrão IONfinance)

`<type>(<scope>): <description>`

**Types:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `security`: Correção de segurança
- `lgpd`: Conformidade LGPD
- `docs`: Documentação
- `test`: Testes
- `chore`: Tarefas de manutenção
- `refactor`: Refatoração
- `perf`: Performance

---

## Contato de Emergência

Em caso de incidente crítico em produção:
1. **CHEFE:** Douglas Lúcio
2. **E-mail:** emergency@ionkod.com
