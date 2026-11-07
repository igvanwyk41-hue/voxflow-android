package com.voxflow.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.os.Build

class VoxFlowAccessibilityService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var bubble: ImageView? = null
    private var showing = false
    companion object { @Volatile var active: VoxFlowAccessibilityService? = null }

    override fun onServiceConnected() {
        super.onServiceConnected()
        active = this
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 120
        }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow
        val focused = root?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val editable = focused?.isEditable == true
        if (editable) showBubble()
        else if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) hideBubble()
        focused?.recycle()
        root?.recycle()
    }

    private fun showBubble() {
        if (showing || wm == null) return
        val view = ImageView(this).apply {
            setImageResource(R.drawable.voxflow_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF8D18E8.toInt())
                setStroke(2, 0xFFE2C8FF.toInt())
            }
            setPadding(dp(4), dp(4), dp(4), dp(4))
            elevation = dp(8).toFloat()
            contentDescription = "Start VOXFLOW dictation"
            isClickable = true
            setOnClickListener { startDictation() }
        }
        val params = WindowManager.LayoutParams(
            dp(58), dp(58), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            x = dp(12)
        }
        try {
            wm?.addView(view, params)
            bubble = view
            showing = true
        } catch (_: Exception) { }
    }

    private fun hideBubble() {
        if (showing) try { wm?.removeView(bubble) } catch (_: Exception) { }
        bubble = null
        showing = false
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun startDictation() {
        val intent = Intent(this, DictationService::class.java).setAction(DictationService.ACTION_START)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    fun insert(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: root
        val pasted = node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        val args = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val ok = pasted || node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        node.recycle()
        root.recycle()
        return ok
    }

    override fun onInterrupt() {}
    override fun onDestroy() {
        hideBubble()
        active = null
        super.onDestroy()
    }
}
