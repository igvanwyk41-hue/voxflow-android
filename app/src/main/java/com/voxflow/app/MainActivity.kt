package com.voxflow.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData

class MainActivity : Activity() {
    private val bg = 0xFF0D0A16.toInt()
    private val panel = 0xFF1B1628.toInt()
    private val purple = 0xFF9B65FF.toInt()
    private val muted = 0xFFB7AFC6.toInt()
    private lateinit var root: LinearLayout

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun shape(c:Int, r:Int)=GradientDrawable().apply { setColor(c); cornerRadius=dp(r).toFloat() }
    private fun label(s:String, size:Float=14f, color:Int=Color.WHITE, bold:Boolean=false)=TextView(this).apply {
        text=s; textSize=size; setTextColor(color); includeFontPadding=true
        if(bold) setTypeface(typeface, Typeface.BOLD)
    }
    private fun button(s:String, primary:Boolean=false, click:()->Unit)=TextView(this).apply {
        text=s; textSize=14f; gravity=Gravity.CENTER; setTextColor(Color.WHITE)
        typeface=Typeface.DEFAULT_BOLD; background=shape(if(primary) purple else panel,14)
        setPadding(dp(14),dp(15),dp(14),dp(15)); isClickable=true; setOnClickListener{click()}
    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=bg; window.navigationBarColor=bg
        showHome()
    }

    private fun shell(title:String, subtitle:String) {
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(22),dp(24),dp(22),dp(24)); setBackgroundColor(bg) }
        root.addView(label("VOXFLOW",12f,purple,true))
        root.addView(label(title,29f,Color.WHITE,true).apply { setPadding(0,dp(10),0,dp(4)) })
        root.addView(label(subtitle,14f,muted).apply { setPadding(0,0,0,dp(22)) })
        val scroll=ScrollView(this).apply { isFillViewport=true; addView(root) }
        setContentView(scroll)
    }
    private fun card(title:String, body:String) {
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; background=shape(panel,18); setPadding(dp(16),dp(16),dp(16),dp(16)) }
        box.addView(label(title,16f,Color.WHITE,true))
        box.addView(label(body,13f,muted).apply { setPadding(0,dp(7),0,0); setLineSpacing(dp(3).toFloat(),1f) })
        root.addView(box,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(12) })
    }
    private fun action(s:String, primary:Boolean=false, click:()->Unit) {
        root.addView(button(s,primary,click),LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(10) })
    }

    private fun showHome() {
        shell("Your voice,\nyour words.","Dictate naturally in the apps you already use.")
        card("FLOATING DICTATION","Open a message or document, tap a text field, then use the VOXFLOW bubble to speak. Your words are inserted where you are typing.")
        action("SET UP VOXFLOW",true){ showSetup() }
        card("YOUR FLOW","Your recent dictations and word count will appear here as you use VOXFLOW.")
        action("DICTIONARY"){ showDictionary() }
        action("VOICE SNIPPETS"){ showSnippets() }
        action("SETTINGS"){ showSettings() }
    }
    private fun showSetup() {
        shell("Get started","A few Android permissions let VOXFLOW appear over other apps and insert your speech.")
        card("1  MICROPHONE","Allow microphone access when Android asks. Dictation requires an internet connection and an installed speech recognition service.")
        action("ALLOW MICROPHONE",true) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO),10)
        }
        card("2  DISPLAY OVER OTHER APPS","This lets the floating VOXFLOW bubble appear above your keyboard and other apps.")
        action("ENABLE FLOATING BUBBLE") {
            if(android.os.Build.VERSION.SDK_INT>=23) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        card("3  ACCESSIBILITY SERVICE","This allows VOXFLOW to detect the active text field and insert the finished dictation. It does not replace your keyboard.")
        action("ENABLE TEXT INSERTION",true){ startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        action("BACK TO HOME"){ showHome() }
    }
    private fun showDictionary() {
        shell("Personal dictionary","Keep names and terms you use often.")
        val input=EditText(this).apply { hint="Add a word or name"; setTextColor(Color.WHITE); setHintTextColor(muted); background=shape(panel,12); setPadding(dp(14),dp(12),dp(14),dp(12)) }
        root.addView(input,LinearLayout.LayoutParams(-1,-2))
        action("SAVE WORD",true) {
            val words=getSharedPreferences("voxflow",0).getStringSet("dictionary", emptySet())!!.toMutableSet()
            if(input.text.isNotBlank()) { words.add(input.text.toString().trim()); getSharedPreferences("voxflow",0).edit().putStringSet("dictionary",words).apply(); input.setText(""); Toast.makeText(this,"Word saved",Toast.LENGTH_SHORT).show() }
        }
        val words=getSharedPreferences("voxflow",0).getStringSet("dictionary", emptySet())!!.toList()
        card("SAVED WORDS",if(words.isEmpty()) "Your saved words will appear here." else words.joinToString("\n"))
        action("BACK"){showHome()}
    }
    private fun showSnippets() {
        shell("Voice snippets","Save phrases you want to insert quickly.")
        val name=EditText(this).apply { hint="Shortcut name"; setTextColor(Color.WHITE); setHintTextColor(muted); background=shape(panel,12); setPadding(dp(14),dp(12),dp(14),dp(12)) }
        val phrase=EditText(this).apply { hint="Text to insert"; setTextColor(Color.WHITE); setHintTextColor(muted); background=shape(panel,12); minLines=3; setPadding(dp(14),dp(12),dp(14),dp(12)) }
        root.addView(name,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(10)})
        root.addView(phrase,LinearLayout.LayoutParams(-1,-2))
        action("SAVE SNIPPET",true) {
            if(name.text.isNotBlank() && phrase.text.isNotBlank()) {
                val prefs=getSharedPreferences("voxflow",0); val all=prefs.getString("snippets","")!!
                prefs.edit().putString("snippets",all+"\n"+name.text.toString()+" :: "+phrase.text.toString()).apply()
                name.setText(""); phrase.setText(""); Toast.makeText(this,"Snippet saved",Toast.LENGTH_SHORT).show()
            }
        }
        card("YOUR SNIPPETS",getSharedPreferences("voxflow",0).getString("snippets","")!!.ifBlank{"No snippets saved yet."})
        action("BACK"){showHome()}
    }
    private fun showSettings() {
        shell("Settings","Manage the way VOXFLOW works on your phone.")
        card("DICTATION LANGUAGE","The first version uses your Android speech recognition language settings. Choose English (South Africa) or another supported language in your device's speech settings.")
        action("ANDROID SPEECH SETTINGS"){ startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)) }
        card("PERMISSIONS","VOXFLOW needs microphone, display-over-other-apps and its accessibility service for floating dictation and text insertion.")
        action("OPEN APP PERMISSIONS"){ startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:$packageName"))) }
        action("ACCESSIBILITY SETTINGS"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
        action("BACK"){showHome()}
    }
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>, grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode==10) Toast.makeText(this,if(grantResults.firstOrNull()==0) "Microphone enabled" else "Microphone permission is required to dictate",Toast.LENGTH_LONG).show()
    }
}