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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Book
import com.grandpyanne.daemrevise.data.Card as StudyCard
import com.grandpyanne.daemrevise.data.Formula
import com.grandpyanne.daemrevise.data.NoteBlock
import com.grandpyanne.daemrevise.data.PdfRef
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Question
import com.grandpyanne.daemrevise.data.Topic

private val TABS = listOf("Notes", "Papers", "Formulas", "Facts", "Books")

@Composable
fun TopicScreen(
    topic: Topic,
    accent: Color,
    progress: Progress,
    books: Map<String, Book>,
    onOpenBook: (Book, Int) -> Unit,
    onBack: () -> Unit,
    onQuiz: () -> Unit,
    onStudyCards: () -> Unit,
) {
    // Open on Notes when the topic has notes, otherwise on Past papers.
    var tab by rememberSaveable { mutableIntStateOf(if (topic.notes.isNotEmpty()) 0 else 1) }

    Column(Modifier.fillMaxSize()) {
        Header(title = topic.title, subtitle = topic.section, onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusTag(topic.status)
                    if (topic.sittings > 0) Tag("Asked in ${topic.sittings} sittings")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val best = progress.bestQuizScore(topic.id)
                    ActionTile(
                        title = "Quiz",
                        subtitle = when {
                            topic.mcqs.isEmpty() -> "Coming soon"
                            best >= 0 -> "${topic.mcqs.size} questions · best $best%"
                            else -> "${topic.mcqs.size} questions"
                        },
                        color = accent,
                        enabled = topic.mcqs.isNotEmpty(),
                        onClick = onQuiz,
                        modifier = Modifier.weight(1f),
                    )
                    ActionTile(
                        title = "Flashcards",
                        subtitle = "${progress.knownCards(topic)} / ${topic.cards.size} known",
                        color = AppColors.Safety,
                        enabled = topic.cards.isNotEmpty(),
                        onClick = onStudyCards,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item { TabBar(selected = tab, accent = accent, onSelect = { tab = it }) }

            when (tab) {
                0 -> {
                    if (topic.notes.isEmpty()) {
                        item { EmptyNote("Notes for this topic are coming in a later update. The summary below covers the essentials.") }
                        if (topic.summary.isNotBlank()) item { SummaryBox(topic.summary) }
                    }
                    itemsIndexed(topic.notes) { _, block -> NoteView(block, accent) }
                    item { ClassNotesCard(topic.title) }
                    if (topic.refs.isNotEmpty()) item {
                        Text(
                            text = "📘 Want the full story? See the Books tab for the textbook chapters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { tab = 4 }
                                .padding(vertical = 8.dp),
                        )
                    }
                }
                1 -> {
                    item {
                        ProgressLine("questions practised", progress.doneQuestions(topic), topic.questions.size)
                    }
                    itemsIndexed(topic.questions) { index, q ->
                        QuestionItem(
                            number = index + 1,
                            question = q,
                            done = progress.isQuestionDone(topic.id, index),
                            onToggle = { progress.toggleQuestionDone(topic.id, index) },
                        )
                    }
                }
                4 -> item { TopicBooks(topic.refs, books, accent, onOpen = onOpenBook) }
                2 -> {
                    if (topic.formulas.isEmpty()) item { EmptyNote("No formulas for this topic.") }
                    itemsIndexed(topic.formulas) { _, f -> FormulaItem(f, accent) }
                }
                else -> {
                    if (topic.cards.isEmpty()) item { EmptyNote("No key facts for this topic yet.") }
                    itemsIndexed(topic.cards) { index, card ->
                        FactItem(card, known = progress.isCardKnown(topic.id, index))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = if (enabled) 0.14f else 0.06f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TabBar(selected: Int, accent: Color, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
    ) {
        TABS.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) accent else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Renders one block of revision notes. */
@Composable
private fun NoteView(block: NoteBlock, accent: Color) {
    when (block) {
        is NoteBlock.Heading -> Text(
            text = block.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.padding(top = 8.dp),
        )
        is NoteBlock.Paragraph -> Text(block.text, style = MaterialTheme.typography.bodyMedium)
        is NoteBlock.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEach { item ->
                Row {
                    Text("•", color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is NoteBlock.FormulaLine -> Text(
            text = block.text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Serif,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
        is NoteBlock.Tip -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.Safety.copy(alpha = 0.14f))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Exam tip", style = MaterialTheme.typography.labelLarge, color = AppColors.Safety, fontWeight = FontWeight.Bold)
            Text(block.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Reminds the student to go back to the notes they wrote in class. */
@Composable
private fun ClassNotesCard(topicTitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("📒 Full notes: your class notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "These are summaries. For the full working, diagrams and your lecturer's examples, go through the notes you wrote in class on $topicTitle.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Points to the lecturer's full PDFs on the PC. Not shown at the moment (replaced by ClassNotesCard). */
@Suppress("unused")
@Composable
private fun PdfCard(pdfs: List<PdfRef>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Full notes on your PC", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            pdfs.forEach { pdf ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("📄 ${pdf.file}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (pdf.where.isNotBlank()) {
                        Text(pdf.where, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(pdf.folder, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SummaryBox(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun QuestionItem(number: Int, question: Question, done: Boolean, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$number. ${question.text}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (question.source.isNotBlank()) Tag(question.source)
                }
                Text("Practised", style = MaterialTheme.typography.labelMedium)
                Checkbox(checked = done, onCheckedChange = { onToggle() })
            }
        }
    }
}

@Composable
private fun FormulaItem(formula: Formula, accent: Color) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = formula.name,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            Text(
                text = formula.expr,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
            )
        }
    }
}

@Composable
private fun FactItem(card: StudyCard, known: Boolean) {
    // Answers start hidden so you can test yourself; tap to reveal.
    var revealed by remember(card) { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { revealed = !revealed },
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (known) {
                    Spacer(Modifier.width(8.dp))
                    Tag("Known")
                }
            }
            Text(
                text = if (revealed) card.back else "Tap to show answer",
                style = MaterialTheme.typography.bodyMedium,
                color = if (revealed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
