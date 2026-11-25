package com.thuonegame.blockblast

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.thuonegame.blockblast.capture.ScreenCaptureService

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val captureRequest = 4102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 28, 24, 28)
            setBackgroundColor(0xFF101820.toInt())
        }
        fun label(value: String, size: Float = 16f) = TextView(this).apply {
            text = value; textSize = size; setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER; setPadding(8, 10, 8, 10)
        }
        root.addView(label("BLOCK BLAST SOLVER", 24f))
        root.addView(label("Thiết lập quyền Android"))
        status = label("")
        root.addView(status)
        root.addView(Button(this).apply {
            text = "1. Cấp quyền trợ năng (kéo thả)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "2. Cho phép hiển thị trên ứng dụng khác"
            setOnClickListener { if (Build.VERSION.SDK_INT >= 23) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))) }
        })
        root.addView(Button(this).apply {
            text = "3. Mở cài đặt tối ưu pin"
            setOnClickListener { openBatterySettings() }
        })
        root.addView(Button(this).apply {
            text = "4. Cấp quyền chụp màn hình"
            setOnClickListener {
                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
            }
        })
        root.addView(Button(this).apply {
            text = "Dừng dịch vụ chụp màn hình"
            setOnClickListener { stopService(Intent(this@MainActivity, ScreenCaptureService::class.java)); refreshStatus() }
        })
        root.addView(label("AUTO PLAY chưa được bật trong bản phát triển này. Chỉ cấp quyền khi bạn chủ động sử dụng ứng dụng.", 13f))
        setContentView(root)
        refreshStatus()
        requestNotificationPermission()
    }

    override fun onResume() { super.onResume(); if (::status.isInitialized) refreshStatus() }

    private fun refreshStatus() {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        val expected = packageName + "/" + com.thuonegame.blockblast.access.GameAccessibilityService::class.java.name
        val accessibility = enabled.split(':').any { it.equals(expected, true) }
        val overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this)
        val battery = (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        status.text = "Trợ năng: " + mark(accessibility) + "\nMenu nổi: " + mark(overlay) + "\nKhông hạn chế pin: " + mark(battery) + "\nChụp màn hình: xác nhận riêng mỗi phiên"
    }
    private fun mark(ok: Boolean) = if (ok) "Đã cấp" else "Chưa cấp"
    private fun openBatterySettings() {
        if (Build.VERSION.SDK_INT >= 23) {
            val request = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + packageName))
            try { startActivity(request) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4103)
    }
    @Deprecated("Deprecated Android callback retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == captureRequest && resultCode == RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenCaptureService.EXTRA_DATA, data)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
            refreshStatus()
        }
    }
}