package com.thuonegame.blockblast

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
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
            setPadding(28, 36, 28, 36)
            setBackgroundColor(0xFF101820.toInt())
        }
        fun label(value: String, size: Float = 16f) = TextView(this).apply {
            text = value; textSize = size; setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER; setPadding(8, 12, 8, 12)
        }
        root.addView(label("BLOCK BLAST SOLVER", 24f))
        root.addView(label("Nền tảng Android • AI solver đang phát triển"))
        status = label("Trạng thái: Chưa cấp quyền chụp màn hình")
        root.addView(status)
        root.addView(Button(this).apply {
            text = "Cấp quyền trợ năng"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Cho phép chụp màn hình"
            setOnClickListener {
                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
            }
        })
        root.addView(Button(this).apply {
            text = "Dừng dịch vụ chụp màn hình"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java))
                status.text = "Trạng thái: Đã dừng"
            }
        })
        root.addView(label("Chưa bật AUTO PLAY ở giai đoạn này. Chỉ bật tự động thao tác sau khi nhận diện và kiểm thử an toàn.", 13f))
        setContentView(root)
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
            status.text = "Trạng thái: Đã cấp quyền chụp màn hình"
        }
    }
}
