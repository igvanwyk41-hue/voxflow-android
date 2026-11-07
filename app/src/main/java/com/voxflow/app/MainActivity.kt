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
import android.widget.*
import android.content.Context

class MainActivity : Activity() {
    private val bg = 0xFF08060F.toInt()
    private val panel = 0xFF171020.toInt()
    private val panel2 = 0xFF21152F.toInt()
    private val purple = 0xFFB018F5.toInt()
    private val violet = 0xFF7418E8.toInt()
    private val muted = 0xFFBDB4CA.toInt()
    private lateinit var root: LinearLayout

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun text(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(this).apply {
            text = value; textSize = size; setTextColor(color); includeFontPadding = true
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setLineSpacing(dp(3).toFloat(), 1f)
        }
    private fun button(value: String, primary: Boolean = false, click: () -> Unit) =
        TextView(this).apply {
            text = value; textSize = 14f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
            background = rounded(if (primary) violet else panel2, 15)
            setPadding(dp(16), dp(17), dp(16), dp(17))
            isClickable = true; setOnClickListener { click() }
        }
    private fun gap(height: Int) = android.view.View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        showHome()
    }

    private fun page(title: String, subtitle: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(28))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(0, 0, 0, dp(12))
            addView(root)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = ImageView(this).apply {
            setImageResource(R.drawable.voxflow_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        header.addView(mark, LinearLayout.LayoutParams(dp(54), dp(54)))
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            addView(text("VOXFLOW", 19f, Color.WHITE, true))
            addView(text("SPEAK  •  FLOW  •  CONNECT", 9f, purple, true))
        }
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)
        root.addView(gap(27))
        root.addView(text(title, 31f, Color.WHITE, true))
        root.addView(gap(7))
        root.addView(text(subtitle, 15f, muted))
        root.addView(gap(24))
        setContentView(scroll)
    }

    private fun card(title: String, description: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(17), dp(17), dp(17), dp(17))
        }
        box.addView(text(title, 14f, purple, true))
        box.addView(gap(7))
        box.addView(text(description, 14f, muted))
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(13) })
    }
    private fun action(value: String, primary: Boolean = false, click: () -> Unit) {
        root.addView(button(value, primary, click),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(11) })
    }

    private fun showHome() {
        page("Your voice.\nYour flow.", "Speak naturally. VOXFLOW turns your speech into polished text in the apps you already use.")
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(0xFF21102F.toInt(), 22)
            setPadding(dp(18), dp(23), dp(18), dp(23))
        }
        val heroIcon = ImageView(this).apply {
            setImageResource(R.drawable.voxflow_launcher)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        hero.addView(heroIcon, LinearLayout.LayoutParams(dp(104), dp(104)))
        hero.addView(gap(10))
        hero.addView(text("DICTATE ANYWHERE", 16f, Color.WHITE, true))
        hero.addView(gap(5))
        hero.addView(text("Choose VOXFLOW as your keyboard, then tap the purple microphone to dictate.", 13f, muted).apply {
            gravity = Gravity.CENTER
        })
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(17) })
        action("SET UP VOXFLOW", true) { showSetup() }
        card("HOW IT WORKS", "1. Enable the VOXFLOW accessibility service.\n2. Open WhatsApp, email or any app with a text field.\n3. Tap the floating V and speak. Your recognised words are inserted into the active field.")
        card("MAKE IT YOURS", "Add names, places and specialist terms to your personal dictionary. Save frequently used phrases as voice snippets.")
        action("PERSONAL DICTIONARY") { showDictionary() }
        action("VOICE SNIPPETS") { showSnippets() }
        action("VOXFLOW PREMIUM  •  R599.88 / YEAR") { showPremium() }
        action("SETTINGS") { showSettings() }
        root.addView(text("FREE VOICE TYPING  •  OPTIONAL PREMIUM AI", 10f, 0xFF756B83.toInt(), true).apply {
            gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(8))
        })
    }

    private fun showSetup() {
        page("Set up VOXFLOW", "Activate the VOXFLOW keyboard once. Then choose it whenever you want to dictate.")
        card("01  MICROPHONE", "VOXFLOW needs microphone access to hear your speech. Allow this first.")
        action("ALLOW MICROPHONE", true) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 10)
        }
        card("02  ENABLE THE KEYBOARD", "Android will show its installed keyboards. Switch VOXFLOW on in the list. This is a standard Android keyboard service.")
        action("OPEN KEYBOARD SETTINGS", true) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        card("03  SELECT VOXFLOW", "Open any app with a text field. Tap the keyboard switcher and select VOXFLOW. Tap the purple microphone and speak. Your words appear directly where your cursor is.")
        action("CHOOSE VOXFLOW KEYBOARD") {
            val imm = getSystemService(android.view.inputmethod.InputMethodManager::class.java)
            imm?.showInputMethodPicker()
        }
        card("OPTIONAL FLOATING V", "The floating V shortcut is an optional accessibility feature. The keyboard works without it.")
        action("OPTIONAL ACCESSIBILITY SETTINGS") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        action("BACK TO HOME") { showHome() }
    }

    private fun showDictionary() {
        page("Personal dictionary", "Teach VOXFLOW the words you use every day.")
        val input = EditText(this).apply {
            hint = "Add a name, place or term"
            textSize = 16f; setTextColor(Color.WHITE); setHintTextColor(muted)
            background = rounded(panel, 13); setPadding(dp(15), dp(14), dp(15), dp(14))
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(gap(12))
        action("SAVE WORD", true) {
            val prefs = getSharedPreferences("voxflow", 0)
            val words = prefs.getStringSet("dictionary", emptySet())!!.toMutableSet()
            val word = input.text.toString().trim()
            if (word.isNotEmpty()) {
                words.add(word)
                prefs.edit().putStringSet("dictionary", words).apply()
                input.setText("")
                Toast.makeText(this, "Word saved", Toast.LENGTH_SHORT).show()
                showDictionary()
            }
        }
        val words = getSharedPreferences("voxflow", 0).getStringSet("dictionary", emptySet())!!.toList().sorted()
        card("SAVED WORDS", if (words.isEmpty()) "Your saved words will appear here." else words.joinToString("\n"))
        action("BACK") { showHome() }
    }

    private fun showSnippets() {
        page("Voice snippets", "Save phrases you want to reuse.")
        val name = EditText(this).apply {
            hint = "Shortcut name"; textSize = 16f; setTextColor(Color.WHITE)
            setHintTextColor(muted); background = rounded(panel, 13)
            setPadding(dp(15), dp(13), dp(15), dp(13))
        }
        val phrase = EditText(this).apply {
            hint = "Phrase to save"; textSize = 16f; setTextColor(Color.WHITE)
            setHintTextColor(muted); background = rounded(panel, 13); minLines = 3
            setPadding(dp(15), dp(13), dp(15), dp(13))
        }
        root.addView(name, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(11) })
        root.addView(phrase, LinearLayout.LayoutParams(-1, -2))
        root.addView(gap(12))
        action("SAVE SNIPPET", true) {
            if (name.text.isNotBlank() && phrase.text.isNotBlank()) {
                val prefs = getSharedPreferences("voxflow", 0)
                val all = prefs.getString("snippets", "")!!
                prefs.edit().putString("snippets", all + "\n" + name.text.toString().trim() + " :: " + phrase.text.toString().trim()).apply()
                Toast.makeText(this, "Snippet saved", Toast.LENGTH_SHORT).show()
                showSnippets()
            }
        }
        card("YOUR SNIPPETS", getSharedPreferences("voxflow", 0).getString("snippets", "")!!.ifBlank { "No snippets saved yet." })
        action("BACK") { showHome() }
    }

    private fun showPremium() {
        page("VOXFLOW Premium", "More than voice typing. Your personal voice-controlled AI assistant.")
        val price = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(0xFF29123A.toInt(), 20)
            setPadding(dp(18), dp(22), dp(18), dp(22))
        }
        price.addView(text("R599.88", 38f, Color.WHITE, true))
        price.addView(text("BILLED ONCE EVERY 12 MONTHS", 12f, purple, true))
        price.addView(gap(8))
        price.addView(text("Equivalent to R49.99 per month. One payment of R599.88 every 12 months. Free voice typing stays free.", 13f, muted).apply { gravity = Gravity.CENTER })
        root.addView(price, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(17) })
        card("FEMALE VOICE CONVERSATION", "Talk naturally with VOX. VOX responds aloud using a female voice where a compatible female voice is available on your device. No male voice fallback.")
        card("PHONE VOICE CONTROL", "Ask VOX to open apps, navigate supported screens, work with contacts and prepare messages. Android permissions and app restrictions still apply.")
        card("AI DOCUMENT STUDIO", "Create, dictate, rewrite, summarise, translate and review documents. Prepare letters, CVs, reports, proposals, minutes and emails.")
        card("SMART APP INTEGRATIONS", "Connect supported services and automate multi-step tasks, with confirmation before sensitive actions.")
        card("SUBSCRIPTION", "Premium checkout is not active yet. Recurring payments will be added through a secure payment provider. Your banking details must never be stored in the app. R599.88 will be charged once every 12 months only after a customer explicitly subscribes and authorises annual recurring payments. The advertised equivalent is R49.99 per month.")
        action("PREMIUM COMING SOON", true) {
            Toast.makeText(this, "Premium subscriptions are not open yet.", Toast.LENGTH_LONG).show()
        }
        action("BACK TO FREE VOICE TYPING") { showHome() }
    }

    private fun showSettings() {
        page("Settings", "Control how VOXFLOW works on your phone.")
        card("DICTATION LANGUAGE", "VOXFLOW uses the language selected in your Android speech recognition service. English (South Africa) and other installed languages may be available.")
        action("ANDROID SPEECH SETTINGS") {
            startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
        }
        card("APP PERMISSIONS", "Review microphone and other Android permissions.")
        action("OPEN APP PERMISSIONS") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
        action("ACCESSIBILITY SETTINGS") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        action("BACK") { showHome() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            Toast.makeText(this,
                if (grantResults.firstOrNull() == 0) "Microphone enabled" else "Microphone permission is required",
                Toast.LENGTH_LONG).show()
        }
    }
}
