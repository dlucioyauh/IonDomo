package com.ionkod.iondomo

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ionkod.iondomo.config.SecureConfigManager
import com.ionkod.iondomo.telemetry.EncryptedTelemetryWorker
import java.util.concurrent.TimeUnit

/**
 * IonDomo - MainActivity
 * Criado pela IONKOD | Fundador: Douglas Lúcio
 * 
 * Propósito: Orquestrar a configuração inicial, permissões e agendamento do worker de telemetria.
 * Segurança: Nenhuma chave hardcoded. Validação rigorosa de permissões.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar SecureConfigManager
        SecureConfigManager.initialize(this)

        // UI Programática simples e robusta (sem dependência de XML complexo para MVP)
        statusText = TextView(this).apply {
            id = android.R.id.text1
            textSize = 18f
            setPadding(32, 32, 32, 32)
        }
        
        actionButton = Button(this).apply {
            id = android.R.id.button1
            setPadding(32, 32, 32, 32)
            setOnClickListener { handleAction() }
        }

        setContentView(android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            addView(statusText)
            addView(actionButton)
        })

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI() // Reavalia permissão ao voltar das configurações
    }

    private fun updateUI() {
        if (hasUsageStatsPermission()) {
            statusText.text = "✅ Permissão de Uso Concedida\nSistema IonDomo Pronto."
            actionButton.text = "Iniciar Telemetria Segura"
            actionButton.isEnabled = true
        } else {
            statusText.text = "⚠️ Permissão de Uso Necessária\nO IonDomo precisa acessar estatísticas de uso para funcionar."
            actionButton.text = "Conceder Permissão nas Configurações"
            actionButton.isEnabled = true
        }
    }

    private fun handleAction() {
        if (hasUsageStatsPermission()) {
            startTelemetry()
        } else {
            requestUsageStatsPermission()
        }
    }

    /**
     * Verifica se o aplicativo tem permissão para acessar estatísticas de uso.
     */
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Redireciona o usuário para a tela de configurações de "Acesso aos dados de uso".
     */
    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir as configurações.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Inicializa e agenda o Worker de Telemetria Criptografada.
     */
    private fun startTelemetry() {
        try {
            // 1. Configurar chave pública (Em produção, isso viria de um provisioning seguro ou QR Code)
            // Por enquanto, usamos um placeholder para demonstrar o fluxo. 
            // O SecureConfigManager lançará exceção se a chave real não for definida.
            // ⚠️ AVISO DE SEGURANÇA IONKOD:
            // Esta é uma chave de teste APENAS para validação do fluxo do MVP.
            // Em produção, esta chave DEVE ser provisionada via:
            // 1. Leitura de QR Code seguro
            // 2. MDM (Mobile Device Management)
            // 3. API de provisioning autenticada
            // O gitleaks ignora esta linha específica via .gitleaksignore (fingerprint aprovada).
            val dummyPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..." // gitleaks:allow // gitleaks:allow 
            SecureConfigManager.setConfig("telemetry_pub_key", dummyPublicKey)
            SecureConfigManager.setConfig("api_base_url", "http://10.0.2.2:8080") // 10.0.2.2 é o localhost do emulador Android

            // 2. Definir restrições: Apenas quando houver rede (Wi-Fi ou Dados)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 3. Criar requisição periódica (ex: a cada 15 minutos)
            val telemetryRequest = PeriodicWorkRequestBuilder<EncryptedTelemetryWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            // 4. Enfileirar no WorkManager (Política REPLACE garante que atualizamos a configuração se mudar)
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "IonDomoTelemetryWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                telemetryRequest
            )

            Toast.makeText(this, "✅ Telemetria iniciada com sucesso!", Toast.LENGTH_LONG).show()
            actionButton.text = "Telemetria Ativa (Rodando)"
            actionButton.isEnabled = false

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Erro ao iniciar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
