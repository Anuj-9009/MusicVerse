package com.musicverse.player

import android.app.Application
import android.content.Intent
import android.os.Process
import dagger.hilt.android.HiltAndroidApp
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Hybrid Music Player Application class.
 * Hilt entry point for dependency injection.
 */
@HiltAndroidApp
class HybridMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup global crash handler so we can diagnose "flash" crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stacktraceString = sw.toString()
                
                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra("stacktrace", stacktraceString)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Ignore
            } finally {
                Process.killProcess(Process.myPid())
                System.exit(1)
            }
        }
    }
}
