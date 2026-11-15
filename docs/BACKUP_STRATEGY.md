# 💾 Estratégia de Backup - IonDomo
**Criado pela IONKOD | Fundador: Douglas Lúcio**
**Padrão: IONfinance Backup Strategy**

---

## 1. Visão Geral

Esta estratégia define como os dados do IonDomo são protegidos contra perda, garantindo disponibilidade e integridade dos dados de telemetria parental.

---

## 2. Tipos de Dados

### 2.1 Dados Críticos
- **Banco de dados PostgreSQL**: Contas, configurações, regras parentais
- **Banco de dados TimescaleDB**: Métricas de telemetria (séries temporais)
- **Configurações de ambiente**: Variáveis de produção (armazenadas em secrets manager)

### 2.2 Dados Não-Críticos
- **Logs de aplicação**: Podem ser regenerados
- **Arquivos estáticos**: Versionados no Git

---

## 3. Frequência de Backup

| Tipo de Dado | Frequência | Retenção | Método |
|--------------|-----------|----------|--------|
| PostgreSQL (config) | Diário (00:00 UTC) | 30 dias | pg_dump + compressão |
| TimescaleDB (métricas) | A cada 6 horas | 90 dias | pg_dump com filtro temporal |
| Configurações | A cada deploy | Permanente | Git + Secrets Manager |

---

## 4. Armazenamento de Backup

### 4.1 Localização
- **Primário:** AWS S3 (bucket: `iondomo-backups-prod`)
- **Secundário:** Cópia local em servidor de backup

### 4.2 Criptografia
- Todos os backups são criptografados com **AES-256**
- Chaves gerenciadas pelo AWS KMS
- Acesso restrito via IAM roles

---

## 5. Teste de Restauração

### 5.1 Frequência
- **Mensal:** Teste completo de restauração em ambiente de staging
- **Trimestral:** Teste de disaster recovery completo

### 5.2 Procedimento
1. Criar ambiente isolado de teste
2. Restaurar backup mais recente
3. Validar integridade dos dados
4. Testar funcionalidades críticas
5. Documentar resultados

### 5.3 Métricas de Sucesso
- **RPO (Recovery Point Objective):** Máximo 6 horas de perda de dados
- **RTO (Recovery Time Objective):** Restauração completa em até 2 horas

---

## 6. Monitoramento e Alertas

### 6.1 Verificações Automáticas
- Validação de checksum após cada backup
- Alerta se backup falhar 2x consecutivas
- Alerta se backup exceder 150% do tamanho normal

### 6.2 Dashboard
- Status dos últimos 7 dias de backups
- Tempo de execução de cada backup
- Espaço utilizado vs. disponível

---

## 7. Plano de Disaster Recovery

### 7.1 Cenário: Perda Total do Banco de Dados
1. Identificar backup mais recente válido
2. Provisionar nova instância de banco
3. Restaurar backup
4. Validar dados restaurados
5. Atualizar configurações de conexão
6. Monitorar logs por 24h

### 7.2 Cenário: Corrupção de Dados
1. Identificar ponto de corrupção
2. Restaurar backup anterior ao ponto
3. Replay de logs de transação (se disponível)
4. Validar integridade

---

## 8. Responsabilidades

| Atividade | Responsável | Frequência |
|-----------|------------|-----------|
| Executar backups | Automatizado | Diária/6h |
| Monitorar backups | DevOps/SRE | Diária |
| Testar restauração | Douglas Lúcio | Mensal |
| Revisar estratégia | CHEFE | Trimestral |

---

## 9. Contato de Emergência

Em caso de falha crítica de backup:
1. **CHEFE:** Douglas Lúcio
2. **E-mail:** emergency@ionkod.com
3. **Slack:** #iondomo-emergency

---

**Última revisão:** 2026-11-15
**Próxima revisão:** 2027-02-15
