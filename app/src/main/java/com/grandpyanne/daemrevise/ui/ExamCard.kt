package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.ExamPaper
import com.grandpyanne.daemrevise.data.Exams
import com.grandpyanne.daemrevise.data.StudyUnit
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// crafted by Grandpyanne™
/**
 * Exam timetable with a live countdown, on the home screen. Shows only for the course the
 * papers belong to, and disappears a week after the last paper.
 */
@Composable
fun ExamCard(units: List<StudyUnit>, onOpenUnit: (StudyUnit) -> Unit) {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val unitIds = units.map { it.id }.toSet()
    if (!Exams.visible(now, unitIds)) return

    val papers = Exams.papers.sortedBy { it.startMillis }
    val sitting = papers.firstOrNull { now in it.startMillis until it.endMillis }
    val next = papers.firstOrNull { it.startMillis > now }
    val accent = MaterialTheme.colorScheme.tertiary

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp),
    ) {
        Text("📅 End-of-term exams", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when {
            sitting != null -> Headline("Now: ${sitting.title}", "Good luck! You've got this 🍀")
            next != null -> Headline(
                "${next.title} in ${countdown(next.startMillis - now)}",
                "${dayTime(next.startMillis)} · ${next.venue}",
            )
            else -> Headline("All papers done 🎉", "Well done. Rest, then Kapi will be here next term.")
        }
        papers.forEach { paper ->
            PaperRow(
                paper = paper,
                done = now >= paper.endMillis,
                isNext = paper == next && sitting == null,
                onClick = { units.firstOrNull { it.id == paper.unitId }?.let(onOpenUnit) },
            )
        }
        if (next != null && sitting == null) {
            Text(
                "Tap a paper to revise it.",
                style = MaterialTheme.typography.bodySmall,
                color = accent,
            )
        }
    }
}

@Composable
private fun Headline(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PaperRow(paper: ExamPaper, done: Boolean, isNext: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
    ) {
        Text(
            if (done) "✅" else if (isNext) "👉" else "•",
            modifier = Modifier.width(28.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                paper.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${dayTime(paper.startMillis)} · ${paper.venue}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "3 days 4 h", "5 h 20 min" or "12 min". */
private fun countdown(ms: Long): String {
    val minutes = (ms / 60_000).coerceAtLeast(0)
    val days = minutes / (24 * 60)
    val hours = (minutes / 60) % 24
    val mins = minutes % 60
    return when {
        days > 0 -> "$days ${if (days == 1L) "day" else "days"} $hours h"
        hours > 0 -> "$hours h $mins min"
        else -> "$mins min"
    }
}

/** "Mon 23 Nov, 9:00", always in Kenya time. */
private fun dayTime(millis: Long): String =
    SimpleDateFormat("EEE d MMM, H:mm", Locale.UK)
        .apply { timeZone = TimeZone.getTimeZone("Africa/Nairobi") }
        .format(millis)
