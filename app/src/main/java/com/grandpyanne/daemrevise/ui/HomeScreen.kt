package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.R
import com.grandpyanne.daemrevise.data.Feedback
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.widget.StudyWidget
import com.grandpyanne.daemrevise.data.StudyUnit
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    units: List<StudyUnit>,
    courseLabel: String,
    progress: Progress,
    onOpenUnit: (StudyUnit) -> Unit,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
    name: String,
    onShowTour: () -> Unit,
) {
    // Save study time every 30 s while the home screen is showing, so the card stays current.
    LaunchedEffect(Unit) {
        while (true) {
            progress.flush()
            delay(30_000)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (name.isBlank()) greeting() else "${greeting()}, $name", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("DAEM Revise", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(courseLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onShowTour) {
                    Icon(Icons.Filled.Info, contentDescription = "How the app works")
                }
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search questions")
                }
            }
        }
        item { StudyScoreCard(progress) }
        item { LibraryRow(onClick = onLibrary) }
        item { WidgetRow() }
        item {
            Text(
                "Your units",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        itemsIndexed(units, key = { _, u -> u.id }) { index, unit ->
            UnitCard(unit, AppColors.unitAccent(index), progress, onClick = { onOpenUnit(unit) })
        }
        item { FeedbackRow() }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun UnitCard(unit: StudyUnit, accent: Color, progress: Progress, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unit.ready) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unit.ready) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = unit.ready, onClick = onClick),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = if (unit.ready) 1f else 0.35f)),
            ) {
                Text(
                    text = unit.shortName.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(unit.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (unit.ready) {
                    Text(
                        text = "${unit.topics.size} topics · ${unit.mcqCount} MCQs · ${unit.questionCount} past Qs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(unit.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(10.dp))
            if (unit.ready) {
                val known = progress.knownCards(unit)
                val fraction = if (unit.cardCount == 0) 0f else known.toFloat() / unit.cardCount
                ProgressRing(fraction = fraction, label = "${(fraction * 100).toInt()}%", color = accent)
            } else {
                Tag("Soon")
            }
        }
    }
}

/** Shortcut to the textbook shelf. */
@Composable
private fun LibraryRow(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text("📚", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Textbooks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "10 books built in, sorted by unit: read the real chapters yourself",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Offers to put Kapi on the home screen, until the widget is there. */
@Composable
private fun WidgetRow() {
    val context = LocalContext.current
    var show by remember {
        mutableStateOf(runCatching { StudyWidget.canPin(context) && !StudyWidget.isOnHomeScreen(context) }.getOrDefault(false))
    }
    if (!show) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.NightBottom)
            .clickable {
                runCatching { StudyWidget.pin(context) }
                show = false
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.mascot_happy),
            contentDescription = null,
            modifier = Modifier.size(width = 56.dp, height = 47.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Add Kapi to your home screen", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, color = Color.White)
            Text("See your 🔥 streak and today's minutes without opening the app",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
        Text("+", style = MaterialTheme.typography.titleLarge, color = AppColors.ScoreLilac)
    }
}

/** Lets testers send their thoughts straight to the developer. */
@Composable
private fun FeedbackRow() {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { Feedback.send(context) }
            .padding(12.dp),
    ) {
        Text("💬  Send feedback", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary)
        Text(
            "Found a mistake or have an idea? Tell me.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
