package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.CardState
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Rating
import com.grandpyanne.daemrevise.data.Srs
import com.grandpyanne.daemrevise.data.Topic
import kotlin.math.min

/** One card in a session: which topic it belongs to and its position in that topic. */
private data class DeckCard(val topic: Topic, val index: Int)

private val NewBlue = Color(0xFF3B82F6)
private val EasyBlue = Color(0xFF2563EB)

/**
 * Flashcards with spaced repetition, like Anki.
 *
 * A session shows the cards due today first, then up to [Srs.NEW_PER_SESSION] new ones.
 * After "Show answer" you rate yourself; the rating decides when the card comes back
 * (later in this session, tomorrow, or in days/weeks), so you review each card just
 * before you would forget it.
 */
// — Grandpyanne™
@Composable
fun FlashcardScreen(
    title: String,
    topics: List<Topic>,
    progress: Progress,
    onBack: () -> Unit,
) {
    // Changing sessionKey starts a fresh session; cram = practise everything without touching the schedule.
    var sessionKey by remember { mutableIntStateOf(0) }
    var cram by remember { mutableStateOf(false) }
    val queue = remember(topics, sessionKey, cram) {
        mutableStateListOf<DeckCard>().apply { addAll(if (cram) allCards(topics).shuffled() else buildSession(topics, progress)) }
    }
    var flipped by remember(sessionKey, cram) { mutableStateOf(false) }
    var done by remember(sessionKey, cram) { mutableIntStateOf(0) }
    val today = Srs.today()

    Column(Modifier.fillMaxSize()) {
        Header(
            title = title,
            subtitle = if (cram) "Practice mode · your schedule is not changed" else "Spaced repetition",
            onBack = onBack,
        )

        if (queue.isEmpty()) {
            CaughtUp(
                reviewed = done,
                cram = cram,
                newLeft = progress.newCards(topics),
                next = progress.nextReview(topics),
                today = today,
                onMoreNew = { cram = false; sessionKey++ },
                onCram = { cram = true; sessionKey++ },
                onDone = onBack,
            )
        } else {
            val current = queue.first()
            val state = progress.cardState(current.topic.id, current.index)

            if (!cram) QueueCounts(queue, progress)

            CardFace(
                current = current,
                flipped = flipped,
                onFlip = { flipped = !flipped },
                modifier = Modifier.weight(1f),
            )

            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                when {
                    !flipped -> Button(onClick = { flipped = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Show answer", fontWeight = FontWeight.SemiBold)
                    }
                    cram -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            queue.removeAt(0); queue.add(current); flipped = false
                        }, modifier = Modifier.weight(1f)) { Text("Again") }
                        Button(onClick = {
                            queue.removeAt(0); done++; flipped = false
                        }, modifier = Modifier.weight(1f)) { Text("Next") }
                    }
                    else -> RatingButtons(state ?: CardState(due = today), today) { rating ->
                        val next = progress.rateCard(current.topic.id, current.index, rating)
                        queue.removeAt(0)
                        done++
                        // Still learning: it comes back a few cards later in this session.
                        if (next.learning) queue.add(min(Srs.requeueGap(rating), queue.size), current)
                        flipped = false
                    }
                }
            }
        }
    }
}

/** "New 12 · Learning 2 · Review 8", in Anki's colours. */
@Composable
private fun QueueCounts(queue: List<DeckCard>, progress: Progress) {
    val states = queue.distinct().map { progress.cardState(it.topic.id, it.index) }
    val new = states.count { it == null }
    val learning = states.count { it?.learning == true }
    val review = states.size - new - learning
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
    ) {
        Text("New $new", color = NewBlue, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text("Learning $learning", color = AppColors.Wrong, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text("Review $review", color = AppColors.Correct, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CardFace(current: DeckCard, flipped: Boolean, onFlip: () -> Unit, modifier: Modifier = Modifier) {
    val card = current.topic.cards[current.index]
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tag(current.topic.title)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (flipped) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = onFlip),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.front,
                        style = if (flipped) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = if (flipped) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    if (flipped) {
                        Spacer(Modifier.heightIn(min = 16.dp))
                        Text(
                            text = card.back,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Spacer(Modifier.heightIn(min = 16.dp))
                        Text(
                            text = "Answer it in your head, then tap Show answer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** Again / Hard / Good / Easy, each showing when the card would come back. */
@Composable
private fun RatingButtons(state: CardState, today: Long, onRate: (Rating) -> Unit) {
    val options = listOf(
        Triple(Rating.AGAIN, "Again", AppColors.Wrong),
        Triple(Rating.HARD, "Hard", AppColors.Safety),
        Triple(Rating.GOOD, "Good", AppColors.Correct),
        Triple(Rating.EASY, "Easy", EasyBlue),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            Button(
                onClick = { onRate(option.first) },
                colors = ButtonDefaults.buttonColors(containerColor = option.third, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(60.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Srs.preview(state, option.first, today), style = MaterialTheme.typography.labelSmall)
                    Text(option.second, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CaughtUp(
    reviewed: Int,
    cram: Boolean,
    newLeft: Int,
    next: Pair<Long, Int>?,
    today: Long,
    onMoreNew: () -> Unit,
    onCram: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (cram) "Practice done 💪" else "All caught up 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        if (reviewed > 0) {
            Text("You went through $reviewed cards.", style = MaterialTheme.typography.bodyLarge)
        }
        if (next != null) {
            val days = (next.first - today).toInt()
            val whenText = if (days == 1) "tomorrow" else "in $days days"
            Text(
                "Next review $whenText (${next.second} cards). Coming back then is what makes it stick.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (newLeft > 0) {
            Button(onClick = onMoreNew, modifier = Modifier.fillMaxWidth()) {
                Text("Learn ${min(newLeft, Srs.NEW_PER_SESSION)} more new cards")
            }
        }
        OutlinedButton(onClick = onCram, modifier = Modifier.fillMaxWidth()) {
            Text("Practise all cards anyway")
        }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

private fun allCards(topics: List<Topic>): List<DeckCard> =
    topics.flatMap { topic -> topic.cards.indices.map { DeckCard(topic, it) } }

// Grandpyanne™ was here
/** Today's session: cards due (oldest first, learning ones before reviews), then a batch of new cards. */
private fun buildSession(topics: List<Topic>, progress: Progress): List<DeckCard> {
    val today = Srs.today()
    val withState = allCards(topics).map { it to progress.cardState(it.topic.id, it.index) }
    val due = withState
        .filter { (_, s) -> s != null && s.due <= today }
        .shuffled()
        .sortedWith(compareBy({ !it.second!!.learning }, { it.second!!.due }))
        .map { it.first }
    val new = withState.filter { it.second == null }.map { it.first }.shuffled().take(Srs.NEW_PER_SESSION)
    return due + new
}
