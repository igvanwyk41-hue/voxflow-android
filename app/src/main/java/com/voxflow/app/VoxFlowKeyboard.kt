package com.voxflow.app

import android.inputmethodservice.InputMethodService
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.HorizontalScrollView
import android.widget.Toast
import java.util.Locale

class VoxFlowKeyboard : InputMethodService() {
    private var recognizer: SpeechRecognizer? = null
    private var micButton: TextView? = null
    private var status: TextView? = null
    private val purple = 0xFF9B20F4.toInt()
    private val bg = 0xFF0A0711.toInt()
    private val muted = 0xFFC9BBD8.toInt()
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun shape(color:Int,radius:Int)=GradientDrawable().apply { setColor(color); cornerRadius=dp(radius).toFloat() }

    override fun onCreateInputView(): View {
        val layout=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(14),dp(12),dp(14),dp(14))
            background=shape(bg,0)
        }
        val header=LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL; orientation=LinearLayout.HORIZONTAL }
        val brand=TextView(this).apply {
            text="VOXFLOW"; textSize=17f; setTextColor(Color.WHITE)
            typeface=Typeface.DEFAULT_BOLD; letterSpacing=.08f
        }
        header.addView(brand,LinearLayout.LayoutParams(0,-2,1f))
        val tagline=TextView(this).apply { text="SPEAK  •  FLOW  •  CONNECT"; textSize=9f; setTextColor(purple); typeface=Typeface.DEFAULT_BOLD }
        header.addView(tagline)
        layout.addView(header)
        val hint=TextView(this).apply {
            text="Tap the microphone and speak naturally"
            textSize=12f; setTextColor(muted); gravity=Gravity.CENTER
            setPadding(0,dp(10),0,dp(12))
        }
        layout.addView(hint)
        micButton=TextView(this).apply {
            text="🎙   TAP TO DICTATE"; textSize=17f; gravity=Gravity.CENTER
            setTextColor(Color.WHITE); typeface=Typeface.DEFAULT_BOLD
            background=shape(purple,16); setPadding(dp(12),dp(17),dp(12),dp(17))
            isClickable=true; setOnClickListener { dictate() }
        }
        layout.addView(micButton,LinearLayout.LayoutParams(-1,-2))
        status=TextView(this).apply {
            text="Ready when you are"; textSize=11f; setTextColor(muted)
            gravity=Gravity.CENTER; setPadding(0,dp(9),0,dp(5))
        }
        layout.addView(status)
        val actions=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
        fun key(label:String, action:()->Unit):TextView=TextView(this).apply {
            text=label; textSize=13f; gravity=Gravity.CENTER; setTextColor(Color.WHITE)
            background=shape(0xFF21152F.toInt(),11); setPadding(dp(12),dp(11),dp(12),dp(11))
            isClickable=true; setOnClickListener { action() }
        }
        val space=key("Space") { currentInputConnection?.commitText(" ",1) }
        actions.addView(space,LinearLayout.LayoutParams(0,-2,1f).apply { marginEnd=dp(6) })
        val punctuation=key(".,?!") { currentInputConnection?.commitText(".",1) }
        actions.addView(punctuation,LinearLayout.LayoutParams(0,-2,1f).apply { marginEnd=dp(6) })
        val backspace=key("⌫") { currentInputConnection?.deleteSurroundingText(1,0) }
        actions.addView(backspace,LinearLayout.LayoutParams(0,-2,1f))
        layout.addView(actions)
        val bottom=TextView(this).apply {
            text="Switch keyboards anytime using the keyboard button"
            textSize=10f; setTextColor(0xFF887B99.toInt()); gravity=Gravity.CENTER
            setPadding(0,dp(10),0,0)
        }
        layout.addView(bottom)
        return layout
    }

    private fun dictate() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status?.text="Speech recognition is not available on this device"
            return
        }
        try {
            recognizer?.destroy()
            recognizer=SpeechRecognizer.createSpeechRecognizer(this)
            micButton?.text="●   LISTENING…"
            status?.text="Speak now. Tap again when finished."
            recognizer?.setRecognitionListener(object:RecognitionListener {
                override fun onReadyForSpeech(params:Bundle?) { status?.text="Listening…" }
                override fun onBeginningOfSpeech() { status?.text="Hearing your voice…" }
                override fun onRmsChanged(rmsdB:Float) {}
                override fun onBufferReceived(buffer:ByteArray?) {}
                override fun onEndOfSpeech() { status?.text="Processing your words…" }
                override fun onError(error:Int) {
                    status?.text=when(error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Try again."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service network error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Allow microphone permission for VOXFLOW."
                        else -> "Couldn't recognise that. Please try again."
                    }
                    micButton?.text="🎙   TAP TO DICTATE"
                    recognizer?.destroy(); recognizer=null
                }
                override fun onResults(results:Bundle?) {
                    val spoken=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    val finalText=clean(spoken)
                    if(finalText.isNotBlank()) {
                        currentInputConnection?.commitText(finalText,1)
                        status?.text="Inserted. Ready for your next thought."
                    } else status?.text="No words recognised. Try again."
                    micButton?.text="🎙   TAP TO DICTATE"
                    recognizer?.destroy(); recognizer=null
                }
                override fun onPartialResults(partialResults:Bundle?) {}
                override fun onEvent(eventType:Int,params:Bundle?) {}
            })
            val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,false)
                putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to VOXFLOW")
            }
            recognizer?.startListening(intent)
        } catch (_:Exception) {
            micButton?.text="🎙   TAP TO DICTATE"
            status?.text="Couldn't start speech recognition."
        }
    }

    private fun clean(raw:String):String {
        var s=raw.trim().replace(Regex("\\s+")," ")
        if(s.isBlank()) return ""
        val dictionary=getSharedPreferences("voxflow",0).getStringSet("dictionary",emptySet()) ?: emptySet()
        dictionary.forEach { term ->
            val safe=Regex.escape(term)
            s=s.replace(Regex("(?i)\\b$safe\\b")) { term }
        }
        s=s.replace(Regex("(?i)\\b(um+|uh+|erm+)\\b[, ]*")," ")
            .replace(Regex("\\s+")," ").trim()
        val snippets=getSharedPreferences("voxflow",0).getString("snippets","").orEmpty()
        snippets.lineSequence().mapNotNull { line ->
            val parts=line.split(" :: ",limit=2)
            if(parts.size==2) parts[0].trim() to parts[1].trim() else null
        }.forEach { (shortcut,phrase) ->
            if(shortcut.isNotBlank() && phrase.isNotBlank()) {
                s=s.replace(Regex("(?i)\\b"+Regex.escape(shortcut)+"\\b")) { phrase }
            }
        }
        if(s.isNotEmpty()) s=s.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        if(s.isNotEmpty() && s.last() !in ".!?") s+="."
        return s
    }

    override fun onStartInputView(info:android.view.inputmethod.EditorInfo?,restarting:Boolean) {
        super.onStartInputView(info,restarting)
        status?.text="Ready when you are"
        micButton?.text="🎙   TAP TO DICTATE"
    }
    override fun onDestroy() { recognizer?.destroy(); recognizer=null; super.onDestroy() }
}
