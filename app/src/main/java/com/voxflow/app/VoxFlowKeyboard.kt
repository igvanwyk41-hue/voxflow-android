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
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class VoxFlowKeyboard : InputMethodService() {
    private var recognizer: SpeechRecognizer? = null
    private var micButton: TextView? = null
    private var status: TextView? = null
    private var listening = false

    private val purple = 0xFF9B20F4.toInt()
    private val bg = 0xFF0A0711.toInt()
    private val muted = 0xFFC9BBD8.toInt()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun shape(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(14))
            background = shape(bg, 0)
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        val brand = TextView(this).apply {
            text = "VOXFLOW"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = .08f
        }
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val tagline = TextView(this).apply {
            text = "SPEAK  •  FLOW  •  CONNECT"
            textSize = 10f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
        }
        header.addView(tagline)
        layout.addView(header)

        val hint = TextView(this).apply {
            text = "Your voice becomes text wherever you type"
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(12))
        }
        layout.addView(hint)

        micButton = TextView(this).apply {
            text = "🎙   TAP TO DICTATE"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = shape(purple, 17)
            setPadding(dp(12), dp(20), dp(12), dp(20))
            isClickable = true
            isFocusable = true
            setOnClickListener { toggleDictation() }
        }
        layout.addView(micButton, LinearLayout.LayoutParams(-1, -2))

        status = TextView(this).apply {
            text = "Ready when you are"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            minLines = 2
            setPadding(dp(4), dp(10), dp(4), dp(7))
        }
        layout.addView(status)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun key(label: String, action: () -> Unit): TextView = TextView(this).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = shape(0xFF21152F.toInt(), 11)
            setPadding(dp(10), dp(13), dp(10), dp(13))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        actions.addView(key("Space") {
            currentInputConnection?.commitText(" ", 1)
        }, LinearLayout.LayoutParams(0, -2, 1.25f).apply { marginEnd = dp(6) })

        actions.addView(key(".,?!") {
            currentInputConnection?.commitText(".", 1)
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(6) })

        actions.addView(key("⌫") {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }, LinearLayout.LayoutParams(0, -2, .65f).apply { marginEnd = dp(6) })

        actions.addView(key("↵") {
            val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                ?: EditorInfo.IME_ACTION_NONE
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                currentInputConnection?.performEditorAction(action)
            } else {
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }, LinearLayout.LayoutParams(0, -2, .65f))

        layout.addView(actions)

        val bottom = TextView(this).apply {
            text = "Free voice typing  •  Works in your other apps"
            textSize = 10f
            setTextColor(0xFF887B99.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }
        layout.addView(bottom)
        return layout
    }

    private fun toggleDictation() {
        if (listening) {
            status?.text = "Finishing your words…"
            micButton?.text = "◌   PROCESSING"
            try {
                recognizer?.stopListening()
            } catch (_: Exception) {
                resetRecognizer("Couldn't finish dictation. Please try again.")
            }
        } else {
            startDictation()
        }
    }

    private fun startDictation() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status?.text = "Speech recognition isn't available. Install or enable a speech service on your phone."
            return
        }

        try {
            resetRecognizer(null)
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            listening = true
            micButton?.text = "●   LISTENING — TAP TO FINISH"
            status?.text = "Listening… speak naturally."

            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    status?.text = "Listening…"
                }

                override fun onBeginningOfSpeech() {
                    status?.text = "Hearing your voice…"
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    status?.text = "Processing your words…"
                    micButton?.text = "◌   PROCESSING"
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Please try again."
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service network error. Check your connection."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Allow microphone permission for VOXFLOW."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Try again in a moment."
                        else -> "Couldn't recognise that. Please try again."
                    }
                    resetRecognizer(message)
                }

                override fun onResults(results: Bundle?) {
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    val finalText = clean(spoken)

                    if (finalText.isNotBlank()) {
                        val inserted = currentInputConnection?.commitText(finalText, 1) == true
                        status?.text = if (inserted) "Inserted. Ready for your next thought." else
                            "Text recognised, but the text field is no longer active."
                    } else {
                        status?.text = "No words recognised. Try again."
                    }
                    resetRecognizer(null)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val preview = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    if (preview.isNotBlank()) status?.text = preview
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val language = Locale.getDefault().toLanguageTag().ifBlank { "en-ZA" }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to VOXFLOW")
            }
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            resetRecognizer("Couldn't start speech recognition. Check microphone permission and try again.")
        }
    }

    private fun resetRecognizer(message: String?) {
        listening = false
        micButton?.text = "🎙   TAP TO DICTATE"
        if (message != null) status?.text = message
        recognizer?.destroy()
        recognizer = null
    }

    private fun clean(raw: String): String {
        var s = raw.trim().replace(Regex("\\s+"), " ")
        if (s.isBlank()) return ""

        val dictionary = getSharedPreferences("voxflow", 0)
            .getStringSet("dictionary", emptySet()) ?: emptySet()
        dictionary.forEach { term ->
            val safe = Regex.escape(term)
            s = s.replace(Regex("(?i)\\b$safe\\b")) { term }
        }

        s = s.replace(Regex("(?i)\\b(um+|uh+|erm+)\\b[, ]*"), " ")
            .replace(Regex("\\s+"), " ").trim()

        val snippets = getSharedPreferences("voxflow", 0).getString("snippets", "").orEmpty()
        snippets.lineSequence().mapNotNull { line ->
            val parts = line.split(" :: ", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.forEach { (shortcut, phrase) ->
            if (shortcut.isNotBlank() && phrase.isNotBlank()) {
                s = s.replace(Regex("(?i)\\b" + Regex.escape(shortcut) + "\\b")) { phrase }
            }
        }

        if (s.isNotEmpty()) s = s.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        if (s.isNotEmpty() && s.last() !in ".!?") s += "."
        return s
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        resetRecognizer(null)
        status?.text = "Ready when you are"
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (listening) {
            try { recognizer?.cancel() } catch (_: Exception) {}
        }
        resetRecognizer(null)
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        resetRecognizer(null)
        super.onDestroy()
    }
}
