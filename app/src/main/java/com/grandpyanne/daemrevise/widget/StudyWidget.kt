package com.grandpyanne.daemrevise.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.grandpyanne.daemrevise.MainActivity
import com.grandpyanne.daemrevise.R
import com.grandpyanne.daemrevise.data.Progress

/**
 * Home-screen widget: study streak with a fire, today's minutes against the daily goal,
 * the study score, and Kapi the capybara, who is awake once you have studied today.
 *
 * Small (2×2): Kapi, streak and minutes. Wide (4×2): everything plus Kapi's message.
 * Tapping it opens the app.
 */
class StudyWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val progress = Progress.get(context)
        provideContent { WidgetContent(progress) }
    }

    companion object {
        private val SMALL = DpSize(110.dp, 110.dp)
        private val WIDE = DpSize(250.dp, 110.dp)

        /** Redraws every widget on the home screen with fresh numbers. */
        suspend fun refresh(context: Context) = StudyWidget().updateAll(context)

        /** True if the launcher can add the widget from inside the app (Android 8+). */
        fun canPin(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

        /** True if the widget is already on the home screen. */
        fun isOnHomeScreen(context: Context): Boolean =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, StudyWidgetReceiver::class.java))
                .isNotEmpty()

        /** Asks the launcher to add the widget; the phone shows its own "Add" dialog. */
        fun pin(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppWidgetManager.getInstance(context).requestPinAppWidget(
                    ComponentName(context, StudyWidgetReceiver::class.java), null, null,
                )
            }
        }
    }
}

/** Android talks to this; it hands the work to [StudyWidget]. */
class StudyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StudyWidget()
}

private val White = ColorProvider(Color.White)
private val Faded = ColorProvider(Color(0xCCFFFFFF))
private val Gold = ColorProvider(Color(0xFFF2B134))
private val Track = ColorProvider(Color(0x33FFFFFF))

@Composable
private fun WidgetContent(progress: Progress) {
    val streak = progress.streakDays()
    val today = progress.minutesOn(0)
    val goal = Progress.DAILY_GOAL_MINUTES
    val score = progress.studyScore()
    val awake = today >= Progress.STREAK_MINUTES
    val wide = LocalSize.current.width >= 250.dp

    val mascot = ImageProvider(if (awake) R.drawable.mascot_happy else R.drawable.mascot_sleepy)
    val streakText = "🔥 $streak"
    val message = when {
        today >= goal -> "Cruising altitude! Goal smashed 🎉"
        awake -> "Climbing nicely ✈️ ${goal - today} min to go"
        streak > 0 -> "Don't let the fire go out! 5 min keeps it."
        else -> "Engines cold… 5 minutes to take off?"
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg))
            .cornerRadius(24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (wide) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(provider = mascot, contentDescription = "Kapi the capybara", modifier = GlanceModifier.size(84.dp, 70.dp))
                Spacer(GlanceModifier.width(10.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(streakText, style = TextStyle(color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
                        Spacer(GlanceModifier.width(6.dp))
                        Text("day streak", style = TextStyle(color = Faded, fontSize = 12.sp))
                        Spacer(GlanceModifier.defaultWeight())
                        Text("Score $score", style = TextStyle(color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold))
                    }
                    Spacer(GlanceModifier.height(4.dp))
                    Text("Today: $today / $goal min", style = TextStyle(color = White, fontSize = 13.sp))
                    Spacer(GlanceModifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = (today.toFloat() / goal).coerceIn(0f, 1f),
                        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
                        color = Gold,
                        backgroundColor = Track,
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text("Kapi: $message", style = TextStyle(color = Faded, fontSize = 11.sp), maxLines = 2)
                }
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(provider = mascot, contentDescription = "Kapi the capybara", modifier = GlanceModifier.size(64.dp, 53.dp))
                Text(streakText, style = TextStyle(color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold))
                Text("$today / $goal min", style = TextStyle(color = Faded, fontSize = 11.sp))
            }
        }
    }
}
