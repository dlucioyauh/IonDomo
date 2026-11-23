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
    private const val PREFS = "iondomo_catalog"
    private const val SENT_KEY = "catalog_sent_v3"

    fun collectAndSendOnce(context: Context, backendUrl: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SENT_KEY, false)) {
            Log.i(TAG, "Catalog already sent, skipping.")
            return
        }

        // CRÍTICO: rede NUNCA na main thread -> NetworkOnMainThreadException
        Thread {
            try {
                val pm = context.packageManager
                val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val array = JSONArray()

                var sysCount = 0
                var iconCount = 0
                for (info in installed) {
                    val pkg = info.packageName
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (isSystem) sysCount++
                    val icon = drawableToBase64(pm, info)
                    if (icon.isNotEmpty()) iconCount++
                    array.put(JSONObject().apply {
                        put("app_name", pkg)
                        put("is_system", isSystem)
                        put("icon_base64", icon)
                    })
                }

                val deviceId = Settings.Secure.getString(
                    context.contentResolver, Settings.Secure.ANDROID_ID
                ) ?: "unknown"

                val payload = JSONObject()
                payload.put("device_id", deviceId)
                payload.put("apps", array)
                val body = payload.toString()

                Log.i(TAG, "Collected ${array.length()} apps (system=$sysCount, withIcon=$iconCount), body=${body.length} bytes")

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
                Log.i(TAG, "Response code=$code body=$resp")

                if (code == 202 || code == 200) {
                    prefs.edit().putBoolean(SENT_KEY, true).apply()
                    Log.i(TAG, "Catalog saved flag set.")
                } else {
                    Log.e(TAG, "Backend rejected catalog: $code")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Catalog send FAILED", e)
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
