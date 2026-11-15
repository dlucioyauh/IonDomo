#!/bin/bash
# IonDomo - Security Audit Script
# Criado pela IONKOD | Fundador: Douglas Lúcio
# Padrão: IONfinance Security Review

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║           IONKOD SECURITY AUDIT - IonDomo                 ║"
echo "║           Padrão: IONfinance Compliance                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

ERRORS=0
WARNINGS=0

# 1. Verificar segredos hardcoded
echo "[1/12] Verificando segredos hardcoded..."
if command -v gitleaks &> /dev/null; then
    if gitleaks detect --source . --no-git --verbose 2>/dev/null; then
        echo "  ✅ Nenhum segredo hardcoded encontrado"
    else
        echo "  ❌ CRÍTICO: Segredos hardcoded detectados!"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "  ⚠️  gitleaks não instalado. Instalando..."
    echo "  Execute: sudo pacman -S gitleaks"
    WARNINGS=$((WARNINGS + 1))
fi

# 2. Verificar .env rastreado
echo "[2/12] Verificando arquivos .env rastreados..."
if git ls-files | grep -E "\.env$|\.env\.local|\.env\.production" > /dev/null 2>&1; then
    echo "  ❌ CRÍTICO: Arquivo .env rastreado pelo Git!"
    ERRORS=$((ERRORS + 1))
else
    echo "  ✅ Nenhum arquivo .env rastreado"
fi

# 3. Verificar comentários sensíveis
echo "[3/12] Verificando comentários com dados sensíveis..."
SENSITIVE_COMMENTS=$(grep -rnw . -E "(password|senha|api_key|secret|token).*=.*['\"].*['\"]" \
    --include="*.kt" --include="*.kts" --include="*.java" --include="*.xml" \
    --exclude-dir=.git --exclude-dir=build 2>/dev/null || true)

if [ -n "$SENSITIVE_COMMENTS" ]; then
    echo "  ❌ CRÍTICO: Possíveis credenciais em comentários/código:"
    echo "$SENSITIVE_COMMENTS"
    ERRORS=$((ERRORS + 1))
else
    echo "  ✅ Nenhum comentário sensível encontrado"
fi

# 4. Verificar senhas de teste
echo "[4/12] Verificando senhas de teste em produção..."
TEST_PASSWORDS=$(grep -rnw . -E "(admin123|password123|test123|123456)" \
    --include="*.kt" --include="*.kts" --include="*.java" --include="*.xml" \
    --exclude-dir=.git --exclude-dir=build 2>/dev/null || true)

if [ -n "$TEST_PASSWORDS" ]; then
    echo "  ❌ CRÍTICO: Senhas de teste encontradas:"
    echo "$TEST_PASSWORDS"
    ERRORS=$((ERRORS + 1))
else
    echo "  ✅ Nenhuma senha de teste encontrada"
fi

# 5. Verificar permissões excessivas no AndroidManifest
echo "[5/12] Verificando permissões do AndroidManifest..."
if [ -f "android-agent/src/main/AndroidManifest.xml" ]; then
    PERMS=$(grep -c "uses-permission" android-agent/src/main/AndroidManifest.xml || echo "0")
    echo "  ℹ️  $PERMS permissões declaradas"
    
    # Verificar permissões perigosas
    if grep -q "READ_CONTACTS\|READ_SMS\|READ_PHONE_STATE\|CAMERA\|RECORD_AUDIO" \
        android-agent/src/main/AndroidManifest.xml 2>/dev/null; then
        echo "  ⚠️  AVISO: Permissões sensíveis detectadas. Justificar no LGPD."
        WARNINGS=$((WARNINGS + 1))
    else
        echo "  ✅ Nenhuma permissão excessiva detectada"
    fi
else
    echo "  ⚠️  AndroidManifest.xml não encontrado"
    WARNINGS=$((WARNINGS + 1))
fi

# 6. Verificar LGPD
echo "[6/12] Verificando documentação LGPD..."
if [ -f "docs/LGPD_PRIVACY_POLICY.md" ]; then
    echo "  ✅ Política de privacidade LGPD existe"
else
    echo "  ❌ CRÍTICO: Política de privacidade LGPD ausente!"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "docs/LGPD_TERMS_OF_USE.md" ]; then
    echo "  ✅ Termos de uso existem"
else
    echo "  ❌ CRÍTICO: Termos de uso ausentes!"
    ERRORS=$((ERRORS + 1))
fi

# 7. Verificar llms.txt
echo "[7/12] Verificando llms.txt..."
if [ -f "public/llms.txt" ]; then
    echo "  ✅ llms.txt existe"
else
    echo "  ⚠️  AVISO: llms.txt ausente (recomendado para agentes de IA)"
    WARNINGS=$((WARNINGS + 1))
fi

# 8. Verificar sitemap e robots
echo "[8/12] Verificando SEO (sitemap.xml e robots.txt)..."
if [ -f "public/sitemap.xml" ]; then
    echo "  ✅ sitemap.xml existe"
else
    echo "  ⚠️  AVISO: sitemap.xml ausente"
    WARNINGS=$((WARNINGS + 1))
fi

if [ -f "public/robots.txt" ]; then
    echo "  ✅ robots.txt existe"
else
    echo "  ⚠️  AVISO: robots.txt ausente"
    WARNINGS=$((WARNINGS + 1))
fi

# 9. Verificar Sentry
echo "[9/12] Verificando monitoramento de erro (Sentry)..."
if grep -rq "sentry" android-agent/build.gradle.kts 2>/dev/null; then
    echo "  ✅ Sentry configurado no build.gradle.kts"
else
    echo "  ⚠️  AVISO: Sentry não configurado"
    WARNINGS=$((WARNINGS + 1))
fi

# 10. Verificar rate limiting
echo "[10/12] Verificando rate limiting (backend)..."
if [ -f "backend-api/src/middleware/rateLimiter.js" ] || \
   [ -f "backend-api/src/middleware/rate_limiter.go" ] || \
   grep -rq "rate.limit\|rateLimit" backend-api/ 2>/dev/null; then
    echo "  ✅ Rate limiting configurado"
else
    echo "  ⚠️  AVISO: Rate limiting não configurado no backend"
    WARNINGS=$((WARNINGS + 1))
fi

# 11. Verificar backup strategy
echo "[11/12] Verificando estratégia de backup..."
if [ -f "docs/BACKUP_STRATEGY.md" ]; then
    echo "  ✅ Estratégia de backup documentada"
else
    echo "  ⚠️  AVISO: Estratégia de backup não documentada"
    WARNINGS=$((WARNINGS + 1))
fi

# 12. Verificar Open Graph
echo "[12/12] Verificando Open Graph..."
if [ -f "public/index.html" ] && grep -q "og:title" public/index.html 2>/dev/null; then
    echo "  ✅ Open Graph configurado"
else
    echo "  ⚠️  AVISO: Open Graph não configurado"
    WARNINGS=$((WARNINGS + 1))
fi

# Resumo
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    RESUMO DA AUDITORIA                     ║"
echo "╠════════════════════════════════════════════════════════════╣"
echo "║  Erros Críticos: $ERRORS                                        ║"
echo "║  Avisos:         $WARNINGS                                        ║"
echo "╚════════════════════════════════════════════════════════════╝"

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo "❌ AUDITORIA FALHOU: $ERRORS erro(s) crítico(s) encontrado(s)."
    echo "   Corrija os erros antes de prosseguir para produção."
    exit 1
else
    echo ""
    echo "✅ AUDITORIA CONCLUÍDA: Nenhum erro crítico encontrado."
    if [ $WARNINGS -gt 0 ]; then
        echo "   ⚠️  $WARNINGS aviso(s) encontrado(s). Recomenda-se corrigir."
    fi
    exit 0
fi
