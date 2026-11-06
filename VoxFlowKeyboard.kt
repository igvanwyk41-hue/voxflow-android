package com.voxflow.app

import android.inputmethodservice.InputMethodService
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class VoxFlowKeyboard : InputMethodService() {
    private var recognizer: SpeechRecognizer? = null
    private val purple = 0xFF914CFF.toInt()

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(10, 12, 10, 12); setBackgroundColor(0xFF100D1D.toInt())
        }
        val title = TextView(this).apply { text = "VOXFLOW  •  Speak to type"; textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; setPadding(4, 8, 4, 12) }
        layout.addView(title)
        val mic = Button(this).apply {
            text = "🎙  TAP TO DICTATE"; setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(purple)
            setOnClickListener { dictate() }
        }
        layout.addView(mic, LinearLayout.LayoutParams(-1, 58))
        val note = TextView(this).apply { text = "Your recognised words are inserted into the active text field."; textSize = 11f; setTextColor(0xFFBBA7E8.toInt()); gravity = Gravity.CENTER; setPadding(4, 10, 4, 4) }
        layout.addView(note)
        return layout
    }

    private fun dictate() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!spoken.isNullOrBlank()) currentInputConnection?.commitText(spoken, 1)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-ZA")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type")
        }
        recognizer?.startListening(intent)
    }

    override fun onDestroy() { recognizer?.destroy(); super.onDestroy() }
}
