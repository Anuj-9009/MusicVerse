package com.musicverse.player

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * CrashActivity
 * Diagnostic activity to display fatal exceptions directly on the screen
 * instead of instantly closing the app.
 */
class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val stacktrace = intent.getStringExtra("stacktrace") ?: "Unknown Crash"
        
        val textView = TextView(this).apply {
            text = "FATAL CRASH DETECTED:\n\n$stacktrace"
            setTextColor(Color.Red.toArgb())
            setPadding(32, 32, 32, 32)
            textSize = 12f
        }
        
        val scrollView = ScrollView(this).apply {
            addView(textView)
            setBackgroundColor(Color.Black.toArgb())
        }
        
        setContentView(scrollView)
    }
}
