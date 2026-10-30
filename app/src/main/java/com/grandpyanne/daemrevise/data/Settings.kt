package com.grandpyanne.daemrevise.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Small app settings: whether the welcome tour was seen, and the student's name. */
class Settings(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** True after the student finishes or skips the welcome tour. */
    var welcomeSeen by mutableStateOf(prefs.getBoolean(KEY_WELCOME, false))
        private set

    /** What Kapi calls the student on the home screen ("" = no name given). */
    var name by mutableStateOf(prefs.getString(KEY_NAME, "").orEmpty())
        private set

    fun finishWelcome(name: String) {
        this.name = name.trim()
        welcomeSeen = true
        prefs.edit().putBoolean(KEY_WELCOME, true).putString(KEY_NAME, this.name).apply()
    }

    private companion object {
        const val KEY_WELCOME = "welcome_seen"
        const val KEY_NAME = "name"
    }
}
