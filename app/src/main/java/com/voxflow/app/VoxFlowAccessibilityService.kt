package com.voxflow.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.os.Build

class VoxFlowAccessibilityService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var bubble: TextView? = null
    private var showing = false
    companion object { @Volatile var active: VoxFlowAccessibilityService? = null }
    override fun onServiceConnected() {
        super.onServiceConnected(); active=this
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 120
        }
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root=rootInActiveWindow
        val focused=root?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val editable=focused?.isEditable==true
        if(editable) showBubble() else if(event?.eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) hideBubble()
        focused?.recycle(); root?.recycle()
    }
    private fun showBubble() {
        if(showing || wm==null) return
        val view=TextView(this).apply {
            text="V"; textSize=17f; setTextColor(Color.WHITE); gravity=Gravity.CENTER
            background=GradientDrawable().apply { shape=GradientDrawable.OVAL; setColor(0xFF9257F5.toInt()); setStroke(2,0xFFD9C5FF.toInt()) }
            elevation=12f; contentDescription="Start VOXFLOW dictation"
            setOnClickListener { startDictation() }
        }
        val type=WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        val params=WindowManager.LayoutParams(dp(54),dp(54),type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL; x=dp(12) }
        try { wm?.addView(view,params); bubble=view; showing=true } catch (_:Exception) {}
    }
    private fun hideBubble() { if(showing) try { wm?.removeView(bubble) } catch (_:Exception) {}; bubble=null; showing=false }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun startDictation() {
        val i=Intent(this,DictationService::class.java).setAction(DictationService.ACTION_START)
        if(Build.VERSION.SDK_INT>=26) startForegroundService(i) else startService(i)
    }
    fun insert(text:String):Boolean {
        val root=rootInActiveWindow ?: return false
        val node=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: root
        val args=android.os.Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text) }
        val ok=node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)
        node.recycle(); root.recycle(); return ok
    }
    override fun onInterrupt() {}
    override fun onDestroy() { hideBubble(); active=null; super.onDestroy() }
}