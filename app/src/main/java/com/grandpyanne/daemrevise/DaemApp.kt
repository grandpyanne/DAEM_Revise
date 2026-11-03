package com.grandpyanne.daemrevise

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.grandpyanne.daemrevise.data.CrashLog

// crafted by Grandpyanne™
/**
 * Runs before any screen.
 *  - Switches on the crash recorder as early as possible.
 *  - Lets WorkManager (used only by the home-screen widget) start on demand
 *    instead of at every app launch.
 */
class DaemApp : Application(), Configuration.Provider {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        CrashLog.install(base)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
