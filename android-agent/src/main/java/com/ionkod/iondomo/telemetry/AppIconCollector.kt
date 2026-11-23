package com.ionkod.iondomo.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Base64
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object AppIconCollector {

    private const val PREFS = "iondomo_catalog"
    private const val SENT_KEY = "catalog_sent_v1"

    data class AppInfo(val pkg: String, val isSystem: Boolean, val iconB64: String)

    fun collectAndSendOnce(context: Context, backendUrl: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SENT_KEY, false)) return // já enviado

        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val array = JSONArray()

        for (info in installed) {
            val pkg = info.packageName
            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = drawableToBase64(pm, info)
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

        try {
            val url = URL("$backendUrl/api/v1/app-catalog")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.outputStream.write(payload.toString().toByteArray())
            if (conn.responseCode == 202) {
                prefs.edit().putBoolean(SENT_KEY, true).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
