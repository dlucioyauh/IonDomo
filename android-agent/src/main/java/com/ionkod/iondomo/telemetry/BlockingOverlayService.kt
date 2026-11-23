package com.ionkod.iondomo.telemetry

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class BlockingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isChecking = false
    private var cachedBlocked: List<String> = emptyList()
    private var lastRulesFetch = 0L

    companion object {
        private const val TAG = "IonDomo_Blocking"
        private const val CHANNEL_ID = "IonDomoBlockingChannel"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL_MS = 1500L
        private const val RULES_CACHE_MS = 10000L
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created.")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started.")
        startForeground(NOTIFICATION_ID, createNotification())
        if (!isChecking) {
            isChecking = true
            checkTopApp()
            showToast("Servico de bloqueio ATIVO")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed.")
        isChecking = false
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "IonDomo Blocking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("IonDomo Agent")
            .setContentText("Protecao ativa")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun showToast(msg: String) {
        handler.post { Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show() }
    }

    private fun checkTopApp() {
        handler.postDelayed({ evaluateAndSchedule() }, CHECK_INTERVAL_MS)
    }

    private fun evaluateAndSchedule() {
        Thread {
            try {
                val now = System.currentTimeMillis()
                if (now - lastRulesFetch > RULES_CACHE_MS) {
                    cachedBlocked = fetchBlockedApps()
                    lastRulesFetch = now
                    Log.i(TAG, "Rules updated: $cachedBlocked")
                }
                val top = getTopPackage()
                if (top != null && cachedBlocked.contains(top)) {
                    Log.w(TAG, "MATCH! Blocking: $top")
                    showBlockingOverlay(top)
                } else {
                    removeOverlay()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Evaluation failed", e)
            }
            if (isChecking) handler.postDelayed({ evaluateAndSchedule() }, CHECK_INTERVAL_MS)
        }.start()
    }

    private fun fetchBlockedApps(): List<String> {
        return try {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
            val conn = URL("http://192.168.3.109:8080/api/v1/rules?device_id=$deviceId")
                .openConnection() as HttpURLConnection
            conn.requestMethod = "GET"; conn.connectTimeout = 5000; conn.readTimeout = 5000
            if (conn.responseCode != 200) return cachedBlocked
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val arr = json.optJSONArray("blocked_apps") ?: return emptyList()
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch rules", e)
            cachedBlocked
        }
    }

    private fun getTopPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 3000
        val events = usm.queryEvents(start, end)
        var last: String? = null
        val ev = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            if (ev.eventType == UsageEvents.Event.ACTIVITY_RESUMED) last = ev.packageName
        }
        return last
    }

    private fun showBlockingOverlay(packageName: String) {
        if (overlayView != null) return

        // Construcao da view pode ser em background; addView NAO pode.
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(250, 15, 23, 42))
            setPadding(48, 48, 48, 48)
            isClickable = true
        }
        val icon = TextView(this).apply {
            text = "\uD83D\uDEE1"
            textSize = 72f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        val message = TextView(this).apply {
            text = "Este app esta bloqueado pelos seus pais.\n($packageName)"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
            setTextColor(Color.WHITE)
        }
        val hint = TextView(this).apply {
            text = "Pressione HOME para sair."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }
        container.addView(icon)
        container.addView(message)
        container.addView(hint)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        overlayView = container

        // >>> CORRECAO CRITICA: addView na MAIN THREAD <<<
        handler.post {
            try {
                windowManager.addView(container, params)
                Log.i(TAG, "Overlay ADDED on main thread.")
                showToast("Bloqueando: $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "addView failed on main thread", e)
                overlayView = null
            }
        }
    }

    private fun removeOverlay() {
        val v = overlayView ?: return
        overlayView = null
        // >>> CORRECAO CRITICA: removeView na MAIN THREAD <<<
        handler.post {
            try {
                windowManager.removeView(v)
                Log.i(TAG, "Overlay removed on main thread.")
            } catch (e: Exception) {
            }
        }
    }
}
