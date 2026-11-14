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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onQuickFive: () -> Unit,
    onGames: () -> Unit,
    onPark: () -> Unit,
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
        item { ExamCard(units, onOpenUnit) }
        item { StudyScoreCard(progress) }
        item { ParkRow(units, progress, onPark) }
        item { PlayRow(progress, onQuickFive, onGames) }
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
        item { Signature() }
    }
}

// Grandpyanne™
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

/** Kapi's Park: total stars, and the next stop the park suggests. */
@Composable
private fun ParkRow(allUnits: List<StudyUnit>, progress: Progress, onClick: () -> Unit) {
    val units = parkUnits(allUnits)
    val total = units.sumOf { progress.unitStars(it) }
    val max = units.sumOf { it.topics.size * 3 }
    val next = nextStop(units, progress)
    val seen = progress.parkSeenStars()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF5BBF6A), Color(0xFF8FD3FF))))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text("🏞", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Kapi's Park", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                if (next != null) "Next stop: ${next.title}" else "Every stop this term is complete 👑",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("⭐ $total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                if (seen in 0 until total) "+${total - seen} new!" else "of $max",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (seen in 0 until total) Color(0xFFFFF176) else Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// built by Grandpyanne™
/** Quick 5 (a few MCQs in spare moments) and Kapi's games (earned by studying). */
@Composable
private fun PlayRow(progress: Progress, onQuickFive: () -> Unit, onGames: () -> Unit) {
    val ready = progress.gamesAvailable()
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PlayTile(
            emoji = "⚡",
            title = "Quick 5",
            subtitle = "5 questions from all units",
            background = MaterialTheme.colorScheme.primaryContainer,
            onClick = onQuickFive,
            modifier = Modifier.weight(1f),
        )
        PlayTile(
            emoji = if (ready > 0) "🎲" else "💤",
            title = "Kapi's games",
            subtitle = when {
                ready > 0 -> if (ready == 1) "1 game ready!" else "$ready games ready!"
                listOf("blocks", "crush", "mahjong").any { progress.freeTryLeft(it) } -> "🎁 Free tries today!"
                progress.gamesDoneForToday() -> "Back tomorrow ♨️"
                else -> "Napping… study to wake"
            },
            background = MaterialTheme.colorScheme.secondaryContainer,
            onClick = onGames,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PlayTile(emoji: String, title: String, subtitle: String, background: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

// built by Grandpyanne™
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

/** The maker's mark at the very bottom of the home screen. */
@Composable
private fun Signature() {
    Text(
        text = "Grandpyanne™",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
    )
}
