package com.grandpyanne.daemrevise.data

import android.content.Context
import android.os.Build
import java.io.File

// crafted by Grandpyanne™
/**
 * If the app crashes, the error is saved to a file. Next time the app opens it shows
 * the error with a "Send report" button, so testers can pass it on without a computer.
 */
object CrashLog {

    private fun file(context: Context) = File(context.filesDir, "last_crash.txt")

    fun install(context: Context) {
        // Called from Application.attachBaseContext, when applicationContext is not ready yet.
        val app = context
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val version = try {
                    app.packageManager.getPackageInfo(app.packageName, 0).versionName
                } catch (_: Exception) {
                    "?"
                }
                file(app).writeText(
                    "DAEM Revise v$version · ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}\n\n" +
                        error.stackTraceToString().take(6000),
                )
            } catch (_: Exception) {
                // Never let the recorder itself get in the way.
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** The saved crash report, or null if the app has not crashed since the last report. */
    fun read(context: Context): String? = file(context).takeIf { it.exists() }?.readText()

    fun clear(context: Context) {
        file(context).delete()
    }
}
