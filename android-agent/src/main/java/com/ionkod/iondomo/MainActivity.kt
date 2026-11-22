package com.ionkod.iondomo

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
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
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (hasUsageStatsPermission()) {
            statusText.text = "✅ Permissão Concedida\n\nPronto para coletar dados reais."
            actionButton.text = "Coletar e Enviar Dados (24h)"
        } else {
            statusText.text = "⚠️ Permissão Necessária\n\nO IonDomo precisa acessar estatísticas de uso."
            actionButton.text = "Conceder Permissão de Uso"
        }
    }

    private fun handleAction() {
        if (hasUsageStatsPermission()) {
            collectAndSendRealData()
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

    private fun collectAndSendRealData() {
        statusText.text = "⏳ Coletando dados de uso..."
        
        Thread {
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - (1000 * 60 * 60 * 24) // Últimas 24 horas

                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, 
                    startTime, 
                    endTime
                )

                var topApp = "unknown"
                var maxTime = 0L

                for (stat in stats) {
                    if (stat.totalTimeInForeground > maxTime) {
                        maxTime = stat.totalTimeInForeground
                        topApp = stat.packageName
                    }
                }

                val durationSeconds = (maxTime / 1000).toInt()
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val deviceId = Build.SERIAL ?: android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)

                runOnUiThread {
                    statusText.text = "⏳ Enviando para o backend..."
                }

                // Enviar para o backend com o JSON correto
                val url = URL("http://192.168.3.109:8080/api/v1/telemetry")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                
                val jsonPayload = "{\"device_id\": \"$deviceId\", \"app_name\": \"$topApp\", \"duration_seconds\": $durationSeconds, \"date\": \"$currentDate\"}"
                connection.outputStream.write(jsonPayload.toByteArray())
                
                val responseCode = connection.responseCode
                
                runOnUiThread {
                    if (responseCode == 202) {
                        statusText.text = "✅ SUCESSO!\n\nApp mais usado: $topApp\nTempo: ${durationSeconds}s\nEnviado ao backend."
                        Toast.makeText(this, "Dados enviados com sucesso!", Toast.LENGTH_LONG).show()
                    } else {
                        statusText.text = "❌ Falha no Backend\n\nCódigo: $responseCode"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "❌ ERRO\n\n${e.message}"
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
