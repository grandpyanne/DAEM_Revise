package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.StudyUnit
import com.grandpyanne.daemrevise.data.Topic
import com.grandpyanne.daemrevise.data.TopicStatus
import kotlinx.coroutines.launch

@Composable
fun UnitScreen(
    unit: StudyUnit,
    accent: Color,
    progress: Progress,
    onBack: () -> Unit,
    onOpenTopic: (Topic) -> Unit,
    onUnitQuiz: () -> Unit,
    onStudyAllCards: () -> Unit,
) {
    var termOnly by rememberSaveable { mutableStateOf(true) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    // This term's topics first (covered, then safety), each group sorted by how often KNEC asks it.
    fun topicsFor(part: String?): List<Topic> = unit.topics
        .filter { part == null || it.part == part }
        .filter { !termOnly || it.inTermScope }
        .sortedWith(
            compareBy<Topic> { it.status.ordinal }
                .thenByDescending { it.sittings }
                .thenByDescending { it.questions.size },
        )
    val termTopics = unit.topics.filter { it.inTermScope }
    val unitBest = progress.bestQuizScore("unit:${unit.id}")

    Column(Modifier.fillMaxSize()) {
        Header(title = unit.shortName, subtitle = unit.note, onBack = onBack)

        // Quiz, flashcards and the term filter apply to the whole unit, so they stay above the tabs.
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUnitQuiz,
                    enabled = termTopics.any { it.mcqs.isNotEmpty() },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (unitBest >= 0) "Unit quiz · best $unitBest%" else "Unit quiz (10 Qs)")
                }
                OutlinedButton(
                    onClick = onStudyAllCards,
                    enabled = termTopics.any { it.cards.isNotEmpty() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Flashcards")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("This term", selected = termOnly, accent = accent) { termOnly = true }
                FilterPill("All topics", selected = !termOnly, accent = accent) { termOnly = false }
            }
        }

        if (unit.parts.size > 1) {
            // Combined unit: one tab per part; tap a tab or swipe left/right to switch.
            val pager = rememberPagerState(pageCount = { unit.parts.size })
            val scope = rememberCoroutineScope()
            TabRow(
                selectedTabIndex = pager.currentPage,
                containerColor = Color.Transparent,
                contentColor = accent,
                indicator = { positions ->
                    if (pager.currentPage < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[pager.currentPage]),
                            color = accent,
                        )
                    }
                },
            ) {
                unit.parts.forEachIndexed { index, part ->
                    Tab(
                        selected = pager.currentPage == index,
                        onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        selectedContentColor = accent,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                part,
                                fontWeight = if (pager.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                TopicList(
                    topics = topicsFor(unit.parts[page]),
                    accent = accent,
                    progress = progress,
                    onOpenTopic = onOpenTopic,
                    onReset = { confirmReset = true },
                )
            }
        } else {
            TopicList(
                topics = topicsFor(null),
                accent = accent,
                progress = progress,
                onOpenTopic = onOpenTopic,
                onReset = { confirmReset = true },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("This clears known flashcards, practised questions and quiz best scores for ${unit.shortName}. Your study time is kept.") },
            confirmButton = {
                TextButton(onClick = {
                    progress.resetUnit(unit)
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TopicList(
    topics: List<Topic>,
    accent: Color,
    progress: Progress,
    onOpenTopic: (Topic) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        if (topics.isEmpty()) {
            item {
                Text(
                    "No topics here for this term yet. Tap \"All topics\" to see everything.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(topics, key = { it.id }) { topic ->
            TopicRow(topic, accent, progress, onClick = { onOpenTopic(topic) })
        }
        item {
            TextButton(onClick = onReset) {
                Text("Reset my progress for this unit")
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun TopicRow(topic: Topic, accent: Color, progress: Progress, onClick: () -> Unit) {
    val best = progress.bestQuizScore(topic.id)
    val known = progress.knownCards(topic)
    // Ring shows the best quiz score when there is one, otherwise flashcards known.
    val (fraction, label) = when {
        best >= 0 -> best / 100f to "$best%"
        topic.cards.isNotEmpty() -> known.toFloat() / topic.cards.size to "$known/${topic.cards.size}"
        else -> 0f to "–"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(if (topic.status == TopicStatus.LATER) AppColors.Later.copy(alpha = 0.4f) else accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusTag(topic.status)
                        if (topic.sittings > 0) Tag("${topic.sittings} sittings")
                    }
                    Text(
                        text = buildList {
                            if (topic.notes.isNotEmpty()) add("Notes")
                            if (topic.mcqs.isNotEmpty()) add("${topic.mcqs.size} MCQs")
                            add("${topic.questions.size} past Qs")
                        }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                ProgressRing(fraction = fraction, label = label, color = accent)
            }
        }
    }
}
