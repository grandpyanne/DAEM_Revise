package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Topic

/** One card in a deck: which topic it belongs to and its position in that topic. */
private data class DeckCard(val topic: Topic, val index: Int)

/**
 * Flashcard practice. Cards you have not marked "Got it" come first, shuffled.
 * Tap the card to flip it, then choose "Again" or "Got it".
 */
@Composable
fun FlashcardScreen(
    title: String,
    topics: List<Topic>,
    progress: Progress,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(0) }
    val deck = remember(topics, round) { buildDeck(topics, progress) }
    var position by remember(deck) { mutableIntStateOf(0) }
    var flipped by remember(deck, position) { mutableStateOf(false) }
    var gotIt by remember(deck) { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Header(
            title = title,
            subtitle = if (position < deck.size) "Card ${position + 1} of ${deck.size}" else "Finished",
            onBack = onBack,
        )

        if (position >= deck.size) {
            Finished(
                gotIt = gotIt,
                total = deck.size,
                onRestart = { round++ },
                onDone = onBack,
            )
        } else {
            val current = deck[position]
            CardPractice(
                current = current,
                flipped = flipped,
                onFlip = { flipped = !flipped },
                onAgain = {
                    progress.setCardKnown(current.topic.id, current.index, false)
                    position++
                },
                onGotIt = {
                    progress.setCardKnown(current.topic.id, current.index, true)
                    gotIt++
                    position++
                },
            )
        }
    }
}

@Composable
private fun CardPractice(
    current: DeckCard,
    flipped: Boolean,
    onFlip: () -> Unit,
    onAgain: () -> Unit,
    onGotIt: () -> Unit,
) {
    val card = current.topic.cards[current.index]
    Column {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                            text = if (flipped) "ANSWER" else "QUESTION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.heightIn(min = 8.dp))
                        Text(
                            text = if (flipped) card.back else card.front,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (flipped) FontWeight.Normal else FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        if (!flipped) {
                            Spacer(Modifier.heightIn(min = 16.dp))
                            Text(
                                text = "Tap to flip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onAgain, modifier = Modifier.weight(1f)) { Text("Again") }
                Button(onClick = onGotIt, modifier = Modifier.weight(1f)) { Text("Got it") }
            }
        }
    }
}

@Composable
private fun Finished(gotIt: Int, total: Int, onRestart: () -> Unit, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Round complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = "You got $gotIt of $total. Cards marked \"Again\" come first next round.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Study again") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

private fun buildDeck(topics: List<Topic>, progress: Progress): List<DeckCard> {
    val all = topics.flatMap { topic -> topic.cards.indices.map { DeckCard(topic, it) } }
    val (known, unknown) = all.partition { progress.isCardKnown(it.topic.id, it.index) }
    return unknown.shuffled() + known.shuffled()
}
