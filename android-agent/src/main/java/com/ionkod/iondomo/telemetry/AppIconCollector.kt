package com.ionkod.iondomo.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object AppIconCollector {
    private const val TAG = "IonDomo_Catalog"
    private const val PREFS = "icon_catalog_prefs"
    private const val SENT_KEY = "catalog_sent_v6"

    fun collectAndSendOnce(context: Context, backendUrl: String) {
        Log.i(TAG, ">>> INICIADO (v6: label real + updated_system) <<<")
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SENT_KEY, false)) {
            Log.i(TAG, "Já enviado. Abortando.")
            return
        }

        Thread {
            try {
                val pm = context.packageManager
                val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                Log.i(TAG, "Total de apps no dispositivo: ${installed.size}")

                val array = JSONArray()
                var sysCount = 0
                var iconCount = 0

                for (info in installed) {
                    val pkg = info.packageName
                    // FIX 1: pega apps pré-instalados de OEM/Google atualizados
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                                   (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                    if (isSystem) {
                        sysCount++
                        continue
                    }

                    // FIX 2: nome REAL que o app se apresenta (ex: "PayPal", "Santo Amaro")
                    val label = pm.getApplicationLabel(info).toString()
                    val icon = drawableToBase64(pm, info)
                    if (icon.isNotEmpty()) iconCount++

                    array.put(JSONObject().apply {
                        put("app_name", pkg)
                        put("label", label)
                        put("is_system", false)
                        put("icon_base64", icon)
                    })
                }

                val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val payload = JSONObject().apply {
                    put("device_id", deviceId)
                    put("apps", array)
                }
                val body = payload.toString()
                Log.i(TAG, "✅ Payload: ${array.length()} apps, system=$sysCount, comIcone=$iconCount, bytes=${body.length}")

                val url = URL("$backendUrl/api/v1/app-catalog")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.outputStream.use { it.write(body.toByteArray()) }

                val code = conn.responseCode
                val resp = conn.inputStream.bufferedReader().readText()
                Log.i(TAG, "Resposta backend: code=$code, body=$resp")

                if (code == 202 || code == 200) {
                    prefs.edit().putBoolean(SENT_KEY, true).apply()
                    Log.i(TAG, "🎉 CATÁLOGO SALVO COM SUCESSO!")
                } else {
                    Log.e(TAG, "❌ Backend rejeitou: code=$code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ FALHA CRÍTICA", e)
            }
        }.start()
    }

    private fun drawableToBase64(pm: PackageManager, info: ApplicationInfo): String {
        return try {
            val d: Drawable = pm.getApplicationIcon(info)
            val size = 72
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            d.setBounds(0, 0, size, size)
            d.draw(canvas)
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 85, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }
}
