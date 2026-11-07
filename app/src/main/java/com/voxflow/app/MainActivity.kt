package com.voxflow.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private val bg = Color.rgb(10, 8, 20)
    private val card = Color.rgb(25, 21, 43)
    private val purple = Color.rgb(151, 91, 255)
    private val muted = Color.rgb(183, 169, 211)
    private lateinit var transcript: EditText
    private lateinit var status: TextView
    private var recognizer: SpeechRecognizer? = null

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        includeFontPadding = true
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun actionButton(value: String, filled: Boolean = false, action: () -> Unit): TextView =
        TextView(this).apply {
            text = value
            textSize = 14f
            maxLines = 2
            gravity = Gravity.CENTER
            includeFontPadding = true
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = rounded(if (filled) purple else card, 16, if (filled) 0 else 0xFF51436E.toInt())
            setPadding(dp(8), dp(12), dp(8), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        buildScreen()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 40)
        }
    }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply {
            setFillViewport(true)
            clipToPadding = false
            isVerticalScrollBarEnabled = false
            setBackgroundColor(bg)
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(28))
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("VOXFLOW", 27f, Color.WHITE, true))
        brand.addView(text("YOUR VOICE. YOUR WORDS.", 10f, muted, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val version = text("ANDROID", 10f, muted, true).apply {
            background = rounded(card, 20)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }
        header.addView(version)
        root.addView(header)

        val intro = text("Speak naturally.\nSee your words appear.", 19f, Color.WHITE, true).apply {
            setPadding(0, dp(28), 0, dp(16))
            setLineSpacing(dp(2).toFloat(), 1f)
        }
        root.addView(intro)

        val mic = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(0xFF211936.toInt(), 22, 0xFF574078.toInt())
            setPadding(dp(16), dp(20), dp(16), dp(20))
            isClickable = true
            isFocusable = true
            setOnClickListener { startDictation() }
        }
        mic.addView(text("🎙", 38f, Color.WHITE).apply { gravity = Gravity.CENTER })
        mic.addView(text("TAP TO SPEAK", 15f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        })
        mic.addView(text("Tap once and start talking", 12f, muted).apply { gravity = Gravity.CENTER })
        val micParams = LinearLayout.LayoutParams(-1, dp(158))
        root.addView(mic, micParams)

        status = text("●  READY WHEN YOU ARE", 12f, 0xFFBFA5FF.toInt(), true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(16), dp(2), dp(18))
        }
        root.addView(status)

        root.addView(text("YOUR TRANSCRIPTION", 12f, muted, true).apply {
            setPadding(dp(2), 0, 0, dp(9))
        })
        transcript = EditText(this).apply {
            hint = "Your words will appear here…"
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF9388A8.toInt())
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = rounded(card, 18, 0xFF403451.toInt())
            minLines = 5
            gravity = Gravity.TOP or Gravity.START
            isVerticalScrollBarEnabled = true
            setLineSpacing(dp(3).toFloat(), 1f)
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        }
        root.addView(transcript, LinearLayout.LayoutParams(-1, dp(178)))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val actionParams = LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginStart = dp(4); marginEnd = dp(4)
        }
        actions.addView(actionButton("✦  Enhance", true) { enhanceText() }, actionParams)
        actions.addView(actionButton("Copy") { copyText() }, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginStart = dp(4); marginEnd = dp(4)
        })
        actions.addView(actionButton("Clear") { transcript.setText("") }, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginStart = dp(4); marginEnd = dp(4)
        })
        val actionTop = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) }
        root.addView(actions, actionTop)

        val keyboard = actionButton("ACTIVATE VOXFLOW KEYBOARD", true) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
            try {
                if (settingsIntent.resolveActivity(packageManager) != null) {
                    startActivity(settingsIntent)
                } else {
                    startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                    Toast.makeText(this, "Open General management, then Keyboard list and default.", Toast.LENGTH_LONG).show()
                }
            } catch (_: android.content.ActivityNotFoundException) {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                Toast.makeText(this, "Open General management, then Keyboard list and default.", Toast.LENGTH_LONG).show()
            }
        }
        root.addView(keyboard, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(12)
        })

        root.addView(text(
            "Speech recognition uses your Android speech service. Language support depends on your device settings.",
            11f, 0xFF91869F.toInt()
        ).apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(16), dp(8), dp(4))
            setLineSpacing(dp(2).toFloat(), 1f)
        })

        scroll.addView(root, ViewGroup.LayoutParams(-1, -2))
        setContentView(scroll)
    }

    private fun startDictation() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "Speech recognition is unavailable on this device."
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 40)
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "●  LISTENING…" }
            override fun onBeginningOfSpeech() { status.text = "●  HEARING YOUR VOICE…" }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "●  PROCESSING SPEECH…" }
            override fun onError(error: Int) { status.text = "Didn't catch that. Tap to try again." }
            override fun onResults(results: Bundle?) {
                val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (words.isNotBlank()) {
                    val old = transcript.text.toString().trim()
                    transcript.setText(if (old.isEmpty()) words else "$old $words")
                    status.text = "●  TRANSCRIPTION COMPLETE"
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
        if (original.isEmpty()) { status.text = "Enter or dictate some text first."; return }
        val cleaned = original.replace(Regex("\\b(um+|uh+|erm+)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ").trim()
        val formatted = if (cleaned.isNotEmpty()) cleaned.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        } else cleaned
        transcript.setText(if (formatted.isNotEmpty() && formatted.last() !in ".!?") "$formatted." else formatted)
        status.text = "Basic cleanup applied • AI service not connected"
    }

    private fun copyText() {
        val clip = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clip.setPrimaryClip(android.content.ClipData.newPlainText("VOXFLOW transcription", transcript.text))
        status.text = "●  COPIED TO CLIPBOARD"
    }

    override fun onDestroy() {
        recognizer?.destroy()
        super.onDestroy()
    }
}
