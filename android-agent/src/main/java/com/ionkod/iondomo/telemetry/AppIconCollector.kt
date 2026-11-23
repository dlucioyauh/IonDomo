package com.ionkod.iondomo.telemetry

import android.content.Context
import android.content.Intent
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
    private const val SENT_KEY = "catalog_sent_v7"

    // Play Store / instaladores de sistema (NÃO é sideload)
    private val OFFICIAL_INSTALLERS = setOf(
        "com.android.vending", "com.google.finsky", "com.google.android.packageinstaller",
        "com.miui.packageinstaller", "com.android.shell", "com.google.android.apps.wellbeing"
    )

    fun collectAndSendOnce(context: Context, backendUrl: String) {
        Log.i(TAG, ">>> INICIADO (v7: label + fatos de risco) <<<")
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SENT_KEY, false)) { Log.i(TAG, "Já enviado."); return }

        Thread {
            try {
                val pm = context.packageManager
                val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                Log.i(TAG, "Total apps: ${installed.size}")

                // Serviços de acessibilidade instalados (uma query só)
                val accPkgs = try {
                    pm.queryIntentServices(
                        Intent("android.accessibilityservice.AccessibilityService"), 0
                    ).map { it.serviceInfo.packageName }.toSet()
                } catch (e: Exception) { emptySet() }

                val array = JSONArray()
                var sysCount = 0
                for (info in installed) {
                    val pkg = info.packageName
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                                   (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    if (isSystem) { sysCount++; continue }

                    val label = pm.getApplicationLabel(info).toString()
                    val icon = drawableToBase64(pm, info)

                    // FATOS objetivos de risco
                    val hasOverlay = try {
                        pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
                            .requestedPermissions?.contains("android.permission.SYSTEM_ALERT_WINDOW") == true
                    } catch (e: Exception) { false }
                    val hasAccessibility = accPkgs.contains(pkg)
                    val installingPkg = try {
                        pm.getInstallSourceInfo(pkg).installingPackageName ?: ""
                    } catch (e: Exception) { "" }
                    val isSideload = installingPkg.isNotEmpty() && installingPkg !in OFFICIAL_INSTALLERS

                    array.put(JSONObject().apply {
                        put("app_name", pkg)
                        put("label", label)
                        put("is_system", false)
                        put("icon_base64", icon)
                        put("has_overlay", hasOverlay)
                        put("has_accessibility", hasAccessibility)
                        put("installing_package", installingPkg)
                        put("is_sideload", isSideload)
                    })
                }

                val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val payload = JSONObject().apply {
                    put("device_id", deviceId)
                    put("apps", array)
                }
                val body = payload.toString()
                Log.i(TAG, "✅ Payload: ${array.length()} apps, system=$sysCount, bytes=${body.length}")

                val conn = URL("$backendUrl/api/v1/app-catalog").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.outputStream.use { it.write(body.toByteArray()) }
                val code = conn.responseCode
                val resp = conn.inputStream.bufferedReader().readText()
                Log.i(TAG, "Resposta: code=$code, body=$resp")
                if (code == 202 || code == 200) {
                    prefs.edit().putBoolean(SENT_KEY, true).apply()
                    Log.i(TAG, "🎉 CATÁLOGO SALVO!")
                }
            } catch (e: Exception) { Log.e(TAG, "❌ FALHA", e) }
        }.start()
    }

    private fun drawableToBase64(pm: PackageManager, info: ApplicationInfo): String {
        return try {
            val d = pm.getApplicationIcon(info)
            val bmp = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp); d.setBounds(0,0,72,72); d.draw(c)
            val baos = ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.PNG, 85, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) { "" }
    }
}
