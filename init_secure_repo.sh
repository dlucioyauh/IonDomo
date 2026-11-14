#!/bin/bash
# IonDomo Secure Init Script
# Criado pela IONKOD | Douglas Lúcio

echo "[IONKOD] Iniciando configuração segura do repositório IonDomo..."

# 1. Inicializar Git se não existir
if [ ! -d ".git" ]; then
    echo "[IONKOD] Repositório Git não encontrado. Inicializando..."
    git init
    git branch -m main
fi

# 2. Garantir que o .gitignore existe
if [ ! -f ".gitignore" ]; then
    echo "[IONKOD] Criando .gitignore..."
    cat > .gitignore << 'GITIGNORE_EOF'
# Variáveis de ambiente e segredos
.env
.env.local
.env.production
*.pem
*.key
*.jks
*.keystore
secrets.xml
local.properties

# Builds e IDEs
/build/
.idea/
.vscode/
*.iml
.DS_Store

# Logs e dados sensíveis de debug
*.log
capture/
*.hprof
GITIGNORE_EOF
fi

# 3. Limpar cache do Git e aplicar .gitignore
echo "[IONKOD] Limpando cache do Git e aplicando .gitignore..."
git rm -r --cached . > /dev/null 2>&1 || true
git add .

# 4. Auditoria estática pré-commit (excluindo este script para evitar falso positivo)
echo "[IONKOD] Executando varredura de segurança pré-commit..."

# Padrões de busca
PATTERNS=(
    "password\s*=\s*['\"]admin"
    "senha\s*=\s*['\"]123"
    "api_key\s*=\s*['\"]sk_"
    "TODO.*senha"
    "FIXME.*chave"
    "hardcoded.*secret"
)

FOUND_ISSUES=0
for pattern in "${PATTERNS[@]}"; do
    if grep -rnw . -E "$pattern" --exclude-dir=.git --exclude="init_secure_repo.sh" 2>/dev/null; then
        echo "[AVISO] Padrão suspeito encontrado: $pattern"
        FOUND_ISSUES=1
    fi
done

if [ $FOUND_ISSUES -eq 1 ]; then
    echo "[ERRO CRÍTICO] Possíveis credenciais ou comentários sensíveis encontrados. Abortando commit."
    exit 1
fi

# 5. Verificar se .env real existe e está ignorado
if [ -f ".env" ]; then
    if git ls-files --error-unmatch .env > /dev/null 2>&1; then
        echo "[ERRO CRÍTICO] O arquivo .env está sendo rastreado pelo Git! Remova-o imediatamente."
        exit 1
    fi
fi

# 6. Primeiro commit seguro
echo "[IONKOD] Realizando o primeiro commit seguro..."
git commit -m "chore: initial secure project structure by IONKOD (Douglas Lúcio)"

echo "[IONKOD] Repositório inicializado com sucesso e auditado."
