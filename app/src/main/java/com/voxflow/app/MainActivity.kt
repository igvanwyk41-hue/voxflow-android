package com.voxflow.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Typeface
import android.graphics.Color
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 7, 17)
    private val panel = Color.rgb(23, 20, 39)
    private val purple = Color.rgb(145, 76, 255)
    private lateinit var transcript: EditText
    private lateinit var status: TextView
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildScreen()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 40)
        }
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun button(text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text; setTextColor(Color.WHITE); setBackgroundTintList(android.content.res.ColorStateList.valueOf(purple));
        setOnClickListener { action() }
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 24, 22, 18); setBackgroundColor(bg) }
        root.addView(label("VOXFLOW", 30f, Color.WHITE, true))
        root.addView(label("Speak naturally. Write intelligently.", 14f, 0xFFBBA7E8.toInt()))
        val mic = TextView(this).apply {
            text = "\n🎙\nTAP TO SPEAK\n"; textSize = 23f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); setBackgroundColor(panel); setPadding(10, 24, 10, 24)
            setOnClickListener { startDictation() }
        }
        val params = LinearLayout.LayoutParams(-1, 190); params.topMargin = 28; root.addView(mic, params)
        status = label("Ready when you are", 13f, 0xFFBBA7E8.toInt())
        status.gravity = Gravity.CENTER; root.addView(status)
        root.addView(label("YOUR TRANSCRIPTION", 12f, 0xFFBBA7E8.toInt(), true).apply { setPadding(0, 22, 0, 8) })
        transcript = EditText(this).apply {
            hint = "Your words will appear here…"; textSize = 16f; setTextColor(Color.WHITE); setHintTextColor(0xFF80768F.toInt())
            setPadding(14, 14, 14, 14); setBackgroundColor(panel); minLines = 5; gravity = Gravity.TOP
        }
        root.addView(transcript, LinearLayout.LayoutParams(-1, 0, 1f))
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; }
        row1.addView(button("✨  Enhance") { enhanceText() }, LinearLayout.LayoutParams(0, 54, 1f))
        row1.addView(button("Copy") { copyText() }, LinearLayout.LayoutParams(0, 54, 1f))
        row1.addView(button("Clear") { transcript.setText("") }, LinearLayout.LayoutParams(0, 54, 1f))
        root.addView(row1)
        val keyboard = button("ACTIVATE VOXFLOW KEYBOARD") {
            startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
        }
        val kp = LinearLayout.LayoutParams(-1, 54); kp.topMargin = 12; root.addView(keyboard, kp)
        root.addView(label("English speech recognition • Afrikaans availability depends on your Android speech service", 11f, 0xFF80768F.toInt()).apply { setPadding(0, 12, 0, 0) })
        setContentView(root)
    }

    private fun startDictation() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { status.text = "Speech recognition is unavailable on this device."; return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 40); return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "Listening…" }
            override fun onBeginningOfSpeech() { status.text = "Hearing your voice…" }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "Processing speech…" }
            override fun onError(error: Int) { status.text = "Didn't catch that. Tap the microphone to try again." }
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (words.isNotBlank()) {
                    val old = transcript.text.toString().trim()
                    transcript.setText(if (old.isEmpty()) words else "$old $words")
                    status.text = "Transcription complete"
                } else status.text = "No speech detected"
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) status.text = partial
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-ZA")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to VOXFLOW")
        }
        recognizer?.startListening(intent)
    }

    private fun enhanceText() {
        val original = transcript.text.toString().trim()
        if (original.isEmpty()) { status.text = "Record or enter some text first."; return }
        // Local demonstration enhancement only. Connect a secure AI service for advanced rewriting.
        val cleaned = original.replace(Regex("\\b(um+|uh+|erm+)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ").trim()
        val formatted = if (cleaned.isNotEmpty()) cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } else cleaned
        transcript.setText(if (formatted.isNotEmpty() && formatted.last() !in ".!?") "$formatted." else formatted)
        status.text = "Basic cleanup applied • AI service not yet connected"
    }

    private fun copyText() {
        val clip = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clip.setPrimaryClip(android.content.ClipData.newPlainText("VOXFLOW transcription", transcript.text))
        status.text = "Copied to clipboard"
    }

    override fun onDestroy() { recognizer?.destroy(); super.onDestroy() }
}