# 📊 Analytics - Eventos Definidos (IonDomo)
**Criado pela IONKOD | Fundador: Douglas Lúcio**

## Princípio
Não coletamos tudo. Medimos apenas o que importa para o negócio.

---

## Evento 1: `app_setup_completed`
**Descrição:** Usuário completou a configuração inicial do app.

**Propriedades:**
- `setup_duration_seconds`: Tempo gasto na configuração
- `permissions_granted`: Lista de permissões concedidas
- `device_android_version`: Versão do Android

**Por que medimos:** Para identificar gargalos na configuração inicial e reduzir abandono.

---

## Evento 2: `first_telemetry_sync`
**Descrição:** Primeira sincronização bem-sucedida de dados de telemetria.

**Propriedades:**
- `sync_duration_ms`: Tempo de sincronização
- `data_points_sent`: Número de pontos de dados enviados
- `encryption_success`: Se a criptografia foi bem-sucedida

**Por que medimos:** Para validar que o pipeline de dados está funcionando.

---

## Evento 3: `parent_dashboard_access`
**Descrição:** Primeiro acesso do responsável ao painel de controle.

**Propriedades:**
- `access_method`: Web ou mobile
- `time_since_setup_minutes`: Tempo entre configuração e primeiro acesso
- `features_viewed`: Funcionalidades visualizadas

**Por que medimos:** Para entender engajamento inicial.

---

## LGPD Compliance
- Nenhum evento contém dados pessoais identificáveis
- Dados são anonimizados antes de serem enviados
- Retenção de dados de analytics: 12 meses
