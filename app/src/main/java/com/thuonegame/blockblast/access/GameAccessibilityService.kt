package com.thuonegame.blockblast.access

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class GameAccessibilityService : AccessibilityService() {
    companion object { @Volatile var instance: GameAccessibilityService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }

    fun drag(fromX: Float, fromY: Float, toX: Float, toY: Float, durationMs: Long = 350): Boolean {
        val path = Path().apply { moveTo(fromX, fromY); lineTo(toX, toY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs)).build()
        return dispatchGesture(gesture, null, null)
    }
}
