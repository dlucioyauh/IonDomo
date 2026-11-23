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

class MainActivity : Activity() {
    
    private lateinit var statusText: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        statusText = TextView(this).apply {
            text = "🛡️ IonDomo Agent\n\nAguardando permissão..."
            textSize = 18f
            gravity = Gravity.CENTER
        }

        actionButton = Button(this).apply {
            text = "Conceder Permissão de Uso"
            setOnClickListener { handleAction() }
        }

        layout.addView(statusText)
        layout.addView(actionButton)
        setContentView(layout)

        updateUI()
        com.ionkod.iondomo.telemetry.AppIconCollector.collectAndSendOnce(this, "http://192.168.3.109:8080")
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (hasUsageStatsPermission()) {
            statusText.text = "✅ Permissão Concedida\n\nColeta automática em segundo plano ativa."
            actionButton.text = "Forçar Envio Agora (Teste)"
        } else {
            statusText.text = "⚠️ Permissão Necessária\n\nO IonDomo precisa acessar estatísticas de uso."
            actionButton.text = "Conceder Permissão de Uso"
        }
    }

    private fun handleAction() {
        if (hasUsageStatsPermission()) {
            forceImmediateSync()
        } else {
            requestUsageStatsPermission()
        }
    }

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

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val telemetryRequest = PeriodicWorkRequestBuilder<com.ionkod.iondomo.telemetry.TelemetryWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "IonDomoTelemetryWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            telemetryRequest
        )
    }

    private fun forceImmediateSync() {
        statusText.text = "⏳ Enviando dados agora..."
        schedulePeriodicWork() // Garante que o agendamento periódico está ativo
        
        // Dispara uma execução imediata para teste
        val immediateRequest = OneTimeWorkRequestBuilder<com.ionkod.iondomo.telemetry.TelemetryWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateRequest)
        
        Toast.makeText(this, "Sincronização disparada. Verifique o backend em instantes.", Toast.LENGTH_LONG).show()
        
        // Restaura o texto após 3 segundos
        statusText.postDelayed({ updateUI() }, 3000)
    }
}
