package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Question
import com.grandpyanne.daemrevise.data.StudyUnit
import com.grandpyanne.daemrevise.data.Topic

// — Grandpyanne™
private data class Hit(val unit: StudyUnit, val topic: Topic, val question: Question)

// crafted by Grandpyanne™
/** Search every past-paper question, e.g. "epicyclic", "zener" or "2024". */
@Composable
fun SearchScreen(
    units: List<StudyUnit>,
    onBack: () -> Unit,
    onOpenTopic: (StudyUnit, Topic) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val all = remember(units) {
        units.flatMap { u -> u.topics.flatMap { t -> t.questions.map { q -> Hit(u, t, q) } } }
    }
    val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val hits = if (words.isEmpty()) emptyList() else all.filter { hit ->
        val haystack = (hit.question.text + " " + hit.question.source + " " + hit.topic.title).lowercase()
        words.all { it in haystack }
    }

    Column(Modifier.fillMaxSize()) {
        Header(title = "Search questions", subtitle = "Try: torsion 2024, epicyclic, zener, Routh", onBack = onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Keyword or year") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        if (words.isNotEmpty()) {
            Text(
                text = "${hits.size} matching questions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(hits) { hit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(hit.unit, hit.topic) },
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${hit.unit.shortName} · ${hit.topic.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(hit.question.text, style = MaterialTheme.typography.bodyMedium)
                        if (hit.question.source.isNotBlank()) Tag(hit.question.source)
                    }
                }
            }
        }
    }
}
