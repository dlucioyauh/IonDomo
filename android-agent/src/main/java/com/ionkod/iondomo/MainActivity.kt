package com.ionkod.iondomo

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val statusText = TextView(this).apply {
            text = "🛡️ IonDomo Agent\n\nPronto para teste de conexão."
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val actionButton = Button(this).apply {
            text = "Testar Conexão com Backend"
            setOnClickListener {
                statusText.text = "⏳ Enviando requisição..."
                testarConexao(statusText)
            }
        }

        layout.addView(statusText)
        layout.addView(actionButton)
        setContentView(layout)
    }

    private fun testarConexao(statusText: TextView) {
        Thread {
            try {
                val url = URL("http://192.168.3.109:8080/api/v1/telemetry")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.write("{\"device\": \"poco_m5\"}".toByteArray())
                
                val responseCode = connection.responseCode
                
                runOnUiThread {
                    if (responseCode == 202) {
                        statusText.text = "✅ SUCESSO!\n\nO Backend recebeu os dados (Status 202)."
                        Toast.makeText(this, "Conexão estabelecida!", Toast.LENGTH_LONG).show()
                    } else {
                        statusText.text = "❌ Falha\n\nCódigo de resposta: $responseCode"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "❌ ERRO DE REDE\n\n${e.message}"
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
