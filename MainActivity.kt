package com.sukabumi.tvlauncher

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val io = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var prayer: TextView
    private lateinit var weather: TextView
    private lateinit var score: TextView
    private lateinit var quote: TextView

    private val tick = object : Runnable {
        override fun run() {
            val now = Date()
            clock.text = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(now) + " WIB"
            date.text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(now)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        createNotificationChannel()
        buildHome()
        handler.post(tick)
        refreshDashboard()
        handler.postDelayed(object : Runnable {
            override fun run() {
                refreshDashboard()
                handler.postDelayed(this, 60 * 60 * 1000L)
            }
        }, 60 * 60 * 1000L)
    }

    private fun buildHome() {
        val bg = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 28, 48, 26)
            setBackgroundColor(Color.rgb(12, 17, 29))
            gravity = Gravity.CENTER_VERTICAL
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = text("SUKABUMI", 18, Color.rgb(218, 184, 112), true)
        val edition = text("  •  MEKKAH EDITION", 14, Color.LTGRAY, false)
        top.addView(brand); top.addView(edition)
        bg.addView(top)

        clock = text("00:00:00 WIB", 58, Color.WHITE, true)
        date = text("Memuat tanggal…", 19, Color.LTGRAY, false)
        bg.addView(clock)
        bg.addView(date)

        val panels = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 26, 0, 20)
        }
        val left = panel()
        left.addView(text("JADWAL SHOLAT • SUKABUMI", 14, Color.rgb(218,184,112), true))
        prayer = text("Memuat jadwal sholat…", 19, Color.WHITE, false)
        left.addView(prayer)
        val right = panel()
        right.addView(text("CUACA SUKABUMI", 14, Color.rgb(218,184,112), true))
        weather = text("Memuat cuaca…", 19, Color.WHITE, false)
        right.addView(weather)
        panels.addView(left, LinearLayout.LayoutParams(0, -1, 1f))
        panels.addView(right, LinearLayout.LayoutParams(0, -1, 1f))
        bg.addView(panels)

        val info = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val scorePanel = panel()
        scorePanel.addView(text("LIVE SCORE LIGA 1", 14, Color.rgb(218,184,112), true))
        score = text("Memeriksa pertandingan…", 17, Color.WHITE, false)
        scorePanel.addView(score)
        val quotePanel = panel()
        quotePanel.addView(text("MUTIARA HIKMAH", 14, Color.rgb(218,184,112), true))
        quote = text("“Sesungguhnya bersama kesulitan ada kemudahan.” — QS. Al-Insyirah: 6", 16, Color.WHITE, false)
        quotePanel.addView(quote)
        info.addView(scorePanel, LinearLayout.LayoutParams(0, -1, 1f))
        info.addView(quotePanel, LinearLayout.LayoutParams(0, -1, 1f))
        bg.addView(info)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        addAction(actions, "YouTube") { launchPackage("com.google.android.youtube.tv") }
        addAction(actions, "Live TV") { openSettings() }
        addAction(actions, "HDMI / Input") { openSettings() }
        addAction(actions, "File Manager") { launchPackage("com.google.android.documentsui") }
        addAction(actions, "Pengaturan") { openSettings() }
        bg.addView(actions)
        setContentView(bg)
    }

    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20, 18, 20, 18)
        setBackgroundColor(Color.rgb(25, 34, 51))
        val p = LinearLayout.LayoutParams(0, -2, 1f)
        p.setMargins(0, 0, 14, 0)
        layoutParams = p
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 5)
        }

    private fun addAction(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            isFocusable = true
            isFocusableInTouchMode = true
            setOnClickListener { action() }
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(62, 79, 105))
        }
        val lp = LinearLayout.LayoutParams(0, 58, 1f)
        lp.setMargins(5, 0, 5, 0)
        parent.addView(button, lp)
    }

    private fun refreshDashboard() {
        io.execute {
            val prayerText = fetchPrayerTimes()
            val weatherText = fetchWeather()
            val scoreText = fetchLeagueScore()
            runOnUiThread {
                prayer.text = prayerText
                weather.text = weatherText
                score.text = scoreText
            }
        }
    }

    private fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.setRequestProperty("User-Agent", "SukabumiLauncher/3.0")
        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally { connection.disconnect() }
    }

    private fun fetchPrayerTimes(): String = try {
        val json = JSONObject(get("https://api.aladhan.com/v1/timingsByCity?city=Sukabumi&country=Indonesia&method=20"))
        val timings = json.getJSONObject("data").getJSONObject("timings")
        listOf("Fajr" to "Subuh", "Dhuhr" to "Dzuhur", "Asr" to "Ashar",
            "Maghrib" to "Maghrib", "Isha" to "Isya")
            .joinToString("\n") { "${it.second}: ${timings.getString(it.first)}" }
    } catch (_: Exception) { "Jadwal sholat belum tersedia.\nPeriksa koneksi internet." }

    private fun fetchWeather(): String = try {
        val json = JSONObject(get("https://api.open-meteo.com/v1/forecast?latitude=-6.9273&longitude=106.9287&current=temperature_2m,relative_humidity_2m,weather_code&timezone=Asia%2FJakarta"))
        val current = json.getJSONObject("current")
        "${current.getDouble("temperature_2m")} °C\nKelembapan: ${current.getInt("relative_humidity_2m")}%"
    } catch (_: Exception) { "Cuaca belum tersedia." }

    private fun fetchLeagueScore(): String = try {
        val json = JSONObject(get("https://site.api.espn.com/apis/site/v2/sports/soccer/idn.1/scoreboard"))
        val events = json.getJSONArray("events")
        if (events.length() == 0) "Belum ada pertandingan terjadwal."
        else (0 until minOf(events.length(), 4)).joinToString("\n") { i ->
            val event = events.getJSONObject(i)
            val competition = event.getJSONArray("competitions").getJSONObject(0)
            val competitors = competition.getJSONArray("competitors")
            val home = competitors.getJSONObject(0)
            val away = competitors.getJSONObject(1)
            "${home.getJSONObject("team").getString("displayName")} ${home.optString("score","-")} - ${away.optString("score","-")} ${away.getJSONObject("team").getString("displayName")}\n${event.optString("status","")}"
        }
    } catch (_: Exception) { "Live score tidak dapat dimuat." }

    private fun launchPackage(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) startActivity(intent)
        else AlertDialog.Builder(this).setMessage("Aplikasi tidak ditemukan di TV.").setPositiveButton("OK", null).show()
    }

    private fun openSettings() {
        try { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
        catch (_: Exception) { }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel("sukabumi_info", "Sukabumi Launcher", NotificationManager.IMPORTANCE_DEFAULT))
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        io.shutdownNow()
        super.onDestroy()
    }
}
