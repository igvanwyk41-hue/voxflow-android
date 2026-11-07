package com.voxflow.app

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast

class DictationService : Service() {
    private var recognizer:SpeechRecognizer?=null
    companion object { const val ACTION_START="com.voxflow.app.START_DICTATION"; const val CHANNEL="voxflow_dictation" }
    override fun onBind(intent:Intent?)=null
    override fun onCreate() {
        super.onCreate()
        val manager=getSystemService(NotificationManager::class.java)
        if(Build.VERSION.SDK_INT>=26) manager.createNotificationChannel(NotificationChannel(CHANNEL,"VOXFLOW dictation",NotificationManager.IMPORTANCE_LOW))
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        val notification=if(Build.VERSION.SDK_INT>=26) Notification.Builder(this,CHANNEL) else Notification.Builder(this)
        val n=notification.setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("VOXFLOW is listening").setContentText("Speak naturally. Your text will be inserted when you finish.").setOngoing(true).build()
        if(Build.VERSION.SDK_INT>=29) startForeground(7,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE) else startForeground(7,n)
        beginRecognition(); return START_NOT_STICKY
    }
    private fun beginRecognition() {
        if(!SpeechRecognizer.isRecognitionAvailable(this)) { stopSelf(); return }
        recognizer?.destroy(); recognizer=SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object:RecognitionListener {
            override fun onReadyForSpeech(params:Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB:Float) {}
            override fun onBufferReceived(buffer:ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error:Int) { stopSelf() }
            override fun onPartialResults(partialResults:Bundle?) {}
            override fun onEvent(eventType:Int,params:Bundle?) {}
            override fun onResults(results:Bundle?) {
                val raw=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                val cleaned=clean(raw)
                if(cleaned.isNotBlank()) {
                    val service=VoxFlowAccessibilityService.active
                    if(service==null || !service.insert(cleaned)) {
                        val clipboard=getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("VOXFLOW",cleaned))
                    }
                }
                stopSelf()
            }
        })
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,java.util.Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to VOXFLOW")
        }
        recognizer?.startListening(intent)
    }
    private fun clean(input:String):String {
        var s=input.replace(Regex("\\b(um+|uh+|erm+)\\b",RegexOption.IGNORE_CASE)," ")
            .replace(Regex("\\s+")," ").trim()
        if(s.isEmpty()) return s
        s=s.replace(Regex("\\bi mean\\b",RegexOption.IGNORE_CASE),"I mean")
        if(s.first().isLowerCase()) s=s.replaceFirstChar{it.titlecase()}
        if(s.last() !in ".!?") s+="."
        return s
    }
    override fun onDestroy() { recognizer?.destroy(); if(Build.VERSION.SDK_INT>=24) stopForeground(STOP_FOREGROUND_REMOVE) else @Suppress("DEPRECATION") stopForeground(true); super.onDestroy() }
}