package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grandpyanne.daemrevise.R
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.TopicStatus
import kotlin.random.Random

/**
 * The home-screen "Study score" card: a night-sky panel with today's score,
 * minutes studied, streak and a small chart of the last 7 days.
 */
@Composable
fun StudyScoreCard(progress: Progress) {
    val score = progress.studyScore()
    val today = progress.minutesOn(0)
    val yesterday = progress.minutesOn(1)
    val streak = progress.streakDays()
    val accuracy = progress.overallAccuracy()
    val week = progress.thisWeek()
    val todayIndex = progress.todayIndexInWeek()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(AppColors.NightTop, AppColors.NightBottom))),
    ) {
        StarField(Modifier.matchParentSize())
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Study score", color = AppColors.ScoreLilac, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("⏱  ${formatMinutes(today)} today", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (streak > 0) "🔥  $streak-day streak" else "Study 5 min to start a streak",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$score", color = AppColors.ScoreLilac, fontSize = 64.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(10.dp))
                        TrendBadge(today - yesterday)
                    }
                    Text(Progress.scoreLabel(score), color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Image(
                    painter = painterResource(R.drawable.mascot_happy),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            WeekBars(week, todayIndex)
            Text(
                text = buildString {
                    append("Goal ${Progress.DAILY_GOAL_MINUTES} min a day")
                    if (accuracy >= 0) append(" · Quiz accuracy $accuracy%")
                },
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/** ▲ 12 min / ▼ 5 min compared with yesterday. */
@Composable
private fun TrendBadge(deltaMinutes: Int) {
    val up = deltaMinutes >= 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF111833)),
        ) {
            Text(if (up) "▲" else "▼", color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${kotlin.math.abs(deltaMinutes)} min",
            color = AppColors.ScoreLilac,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Seven small bars, one per day, today highlighted. */
/** This week's study minutes, Sunday to Saturday. Days still to come are null (faint, empty). */
@Composable
private fun WeekBars(minutes: List<Int?>, todayIndex: Int) {
    val max = (minutes.filterNotNull().maxOrNull() ?: 0).coerceAtLeast(Progress.DAILY_GOAL_MINUTES)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        minutes.forEachIndexed { i, m ->
            val isToday = i == todayIndex
            val future = m == null
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                val fraction = ((m ?: 0).toFloat() / max).coerceIn(0.04f, 1f)
                // Empty space above the bar, then the bar itself, so bars grow upwards.
                Spacer(Modifier.weight(1.0001f - fraction))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            when {
                                isToday -> AppColors.ScoreLilac
                                future -> Color.White.copy(alpha = 0.12f)
                                else -> Color.White.copy(alpha = 0.35f)
                            },
                        ),
                )
                Text(
                    text = Progress.WEEK_LABELS[i],
                    color = Color.White.copy(alpha = if (isToday) 1f else if (future) 0.35f else 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/** Faint random stars, fixed per card so they do not jump around. */
@Composable
private fun StarField(modifier: Modifier) {
    val stars = remember {
        val rnd = Random(7)
        List(70) { Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()) }
    }
    Canvas(modifier) {
        for (star in stars) {
            val brightness = star.third
            drawCircle(
                color = Color.White.copy(alpha = 0.25f + 0.5f * brightness),
                radius = 0.6.dp.toPx() + brightness * 1.2.dp.toPx(),
                center = Offset(star.first * size.width, star.second * size.height),
            )
        }
    }
}

/** Ring showing a 0–1 fraction with a label in the middle. */
@Composable
fun ProgressRing(fraction: Float, label: String, color: Color, size: Int = 52) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp)) {
        CircularProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.18f),
            strokeWidth = 5.dp,
            modifier = Modifier.size(size.dp),
        )
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** Coloured label for Covered / Safety / Later. */
@Composable
fun StatusTag(status: TopicStatus) {
    val color = when (status) {
        TopicStatus.COVERED -> AppColors.Correct
        TopicStatus.SAFETY -> AppColors.Safety
        TopicStatus.LATER -> AppColors.Later
    }
    Tag(text = status.label, container = color.copy(alpha = 0.15f), content = color)
}

fun formatMinutes(minutes: Int): String =
    if (minutes < 60) "$minutes min" else "${minutes / 60} h ${minutes % 60} m"
