package com.ionkod.iondomo

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit
import com.ionkod.iondomo.telemetry.AppIconCollector
import com.ionkod.iondomo.telemetry.TelemetryWorker
import com.ionkod.iondomo.telemetry.BlockingOverlayService

class MainActivity : Activity() {
    
    private lateinit var statusText: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(64, 64, 64, 64)
            }

            statusText = TextView(this).apply {
                text = "🛡️ IonDomo Agent\n\nInicializando..."
                textSize = 18f
                gravity = Gravity.CENTER
            }

            actionButton = Button(this).apply {
                text = "Conceder Permissões"
                setOnClickListener { handleAction() }
            }

            layout.addView(statusText)
            layout.addView(actionButton)
            setContentView(layout)

            updateUI()
            
            // Inicia o serviço APENAS se a permissão de overlay já estiver ativa
            if (Settings.canDrawOverlays(this)) {
                startBlockingServiceSafely()
            } else {
                statusText.text = "⚠️ Permissão de Overlay Necessária\n\nToque em 'Conceder Permissões' para ativar o bloqueio."
            }

        } catch (e: Exception) {
            // Se algo der errado, MOSTRA o erro na tela em vez de fechar
            statusText?.text = "❌ ERRO CRÍTICO:\n${e.message}\n\nStacktrace:\n${e.stackTraceToString().take(500)}"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        // Re-verifica a permissão ao voltar da tela de configurações
        if (Settings.canDrawOverlays(this)) {
            startBlockingServiceSafely()
        }
    }

    private fun updateUI() {
        val hasUsage = hasUsageStatsPermission()
        val hasOverlay = Settings.canDrawOverlays(this)
        
        when {
            !hasUsage -> {
                statusText.text = "⚠️ Acesso aos Dados de Uso Negado\n\nO IonDomo precisa dessa permissão para monitorar apps."
                actionButton.text = "Ativar Acesso aos Dados de Uso"
            }
            !hasOverlay -> {
                statusText.text = "⚠️ Desenhar Sobre Outros Apps Negado\n\nNecessário para BLOQUEAR apps."
                actionButton.text = "Ativar Bloqueio (Overlay)"
            }
            else -> {
                statusText.text = "✅ Sistema Protegido\n\nMonitoramento e Bloqueio ativos."
                actionButton.text = "Forçar Envio Agora (Teste)"
            }
        }
    }

    private fun handleAction() {
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            forceImmediateSync()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) { false }
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun startBlockingServiceSafely() {
        try {
            val intent = Intent(this, BlockingOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Falha ao iniciar bloqueio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun schedulePeriodicWork() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val telemetryRequest = PeriodicWorkRequestBuilder<TelemetryWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("IonDomoTelemetryWork", ExistingPeriodicWorkPolicy.UPDATE, telemetryRequest)
    }

    private fun forceImmediateSync() {
        statusText.text = "⏳ Enviando dados agora..."
        schedulePeriodicWork()
        
        // Envia catálogo de ícones uma vez
        AppIconCollector.collectAndSendOnce(this, "http://192.168.3.109:8080")
        
        val immediateRequest = OneTimeWorkRequestBuilder<TelemetryWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateRequest)
        
        Toast.makeText(this, "Sincronização disparada!", Toast.LENGTH_SHORT).show()
        statusText.postDelayed({ updateUI() }, 3000)
    }
}
