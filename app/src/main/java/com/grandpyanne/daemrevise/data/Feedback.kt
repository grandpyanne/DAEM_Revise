package com.grandpyanne.daemrevise.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

/** The "Send feedback" button: opens WhatsApp (or any app) with a ready-made feedback message. */
object Feedback {

    /**
     * Your WhatsApp number, so feedback comes straight to your chat.
     * International format, digits only, no "+" or spaces: e.g. "254712345678".
     * Leave it empty and friends choose who to send it to (WhatsApp, Gmail, SMS…).
     */
    const val WHATSAPP_NUMBER = ""

    fun send(context: Context) = sendText(context, message(context))

    /** Sends a crash report the same way as feedback. */
    fun sendReport(context: Context, report: String) =
        sendText(context, "DAEM Revise crash report\n\nWhat I was doing when it closed:\n\n---\n$report")

    private fun sendText(context: Context, text: String) {
        if (WHATSAPP_NUMBER.isNotBlank()) {
            val chat = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=${Uri.encode(text)}"))
            try {
                context.startActivity(chat)
                return
            } catch (_: ActivityNotFoundException) {
                // No WhatsApp or browser: fall through to the share sheet.
            }
        }
        val share = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "DAEM Revise feedback")
            .putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(share, "Send feedback with…"))
    }

    /** A short template, with the app version and phone filled in so bugs are easier to track down. */
    private fun message(context: Context): String {
        val version = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "?"
        }
        return """
            |DAEM Revise feedback
            |App v$version · ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}
            |
            |What I liked:
            |
            |What was confusing or didn't work:
            |
            |What I'd add or change:
            |
        """.trimMargin()
    }
}
