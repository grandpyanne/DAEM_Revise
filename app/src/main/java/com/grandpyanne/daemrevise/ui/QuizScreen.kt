package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Mcq
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Srs
import com.grandpyanne.daemrevise.data.Topic
import kotlin.math.roundToInt

/** One question as shown: its topic, its position in that topic, and the options in shuffled order. */
private data class QuizItem(
    val topicId: String,
    val topicTitle: String,
    val index: Int,
    val mcq: Mcq,
    val options: List<String>,
    val correctIndex: Int,
)

// crafted by Grandpyanne™
/**
 * Picks the questions with spaced repetition: first the ones due today (missed ones before
 * reviews), then ones never answered, then the rest, soonest-due first. Shown in a mixed order.
 */
private fun buildQuiz(topics: List<Topic>, limit: Int, progress: Progress): List<QuizItem> {
    val today = Srs.today()
    val all = topics.flatMap { t -> t.mcqs.indices.map { i -> Triple(t, i, progress.questionState(t.id, i)) } }
    val due = all.filter { it.third?.let { s -> s.due <= today } == true }
        .shuffled()
        .sortedWith(compareBy({ !it.third!!.learning }, { it.third!!.due }))
    val new = all.filter { it.third == null }.shuffled()
    val later = all.filter { it.third?.let { s -> s.due > today } == true }
        .shuffled()
        .sortedBy { it.third!!.due }
    return (due + new + later)
        .take(limit)
        .shuffled()
        .map { (topic, i, _) ->
            val mcq = topic.mcqs[i]
            val order = mcq.options.indices.shuffled()
            QuizItem(
                topicId = topic.id,
                topicTitle = topic.title,
                index = i,
                mcq = mcq,
                options = order.map { mcq.options[it] },
                correctIndex = order.indexOf(mcq.answer),
            )
        }
}

/**
 * Multiple-choice quiz with spaced repetition. Pick an answer to see at once whether it
 * was right, followed by a full explanation. Missed questions come back first next time;
 * ones you get right come back after a growing gap (1 day, 3 days, about a week…).
 *
 * @param scoreKey where the best score is saved (a topic id, or "unit:<id>").
 */
@Composable
fun QuizScreen(
    title: String,
    topics: List<Topic>,
    scoreKey: String,
    limit: Int,
    progress: Progress,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(0) }
    val items = remember(topics, round) { buildQuiz(topics, limit, progress) }
    var index by remember(items) { mutableIntStateOf(0) }
    var chosen by remember(items, index) { mutableStateOf<Int?>(null) }
    var correct by remember(items) { mutableIntStateOf(0) }
    var missed by remember(items) { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Header(
            title = title,
            subtitle = if (index < items.size) "Question ${index + 1} of ${items.size}" else "Quiz complete",
            onBack = onBack,
        )

        if (items.isEmpty()) {
            Text(
                "No quiz questions for this topic yet.",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (index >= items.size) {
            val percent = (100.0 * correct / items.size).roundToInt()
            LaunchedEffect(items) { progress.recordQuizScore(scoreKey, percent) }
            QuizResult(
                correct = correct,
                total = items.size,
                percent = percent,
                best = progress.bestQuizScore(scoreKey),
                missed = missed,
                onRetry = { round++ },
                onDone = onBack,
            )
        } else {
            LinearProgressIndicator(
                progress = { index.toFloat() / items.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
            )
            QuestionView(
                item = items[index],
                chosen = chosen,
                isLast = index == items.lastIndex,
                onChoose = { option ->
                    if (chosen == null) {
                        chosen = option
                        val right = option == items[index].correctIndex
                        if (right) correct++ else missed++
                        progress.recordAnswer(right)
                        progress.rateQuestion(items[index].topicId, items[index].index, right)
                    }
                },
                onNext = { index++ },
            )
        }
    }
}

@Composable
private fun QuestionView(
    item: QuizItem,
    chosen: Int?,
    isLast: Boolean,
    onChoose: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tag(item.topicTitle)
        Text(item.mcq.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        item.options.forEachIndexed { i, option ->
            OptionRow(
                letter = "ABCD".getOrElse(i) { '?' },
                text = option,
                state = when {
                    chosen == null -> OptionState.OPEN
                    i == item.correctIndex -> OptionState.RIGHT
                    i == chosen -> OptionState.WRONG
                    else -> OptionState.DIM
                },
                onClick = { onChoose(i) },
            )
        }

        if (chosen != null) {
            val right = chosen == item.correctIndex
            ExplanationBox(right = right, answer = item.options[item.correctIndex], explanation = item.mcq.explanation)
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLast) "See my score" else "Next question")
            }
        }
    }
}

private enum class OptionState { OPEN, RIGHT, WRONG, DIM }

@Composable
private fun OptionRow(letter: Char, text: String, state: OptionState, onClick: () -> Unit) {
    val accent = when (state) {
        OptionState.RIGHT -> AppColors.Correct
        OptionState.WRONG -> AppColors.Wrong
        else -> MaterialTheme.colorScheme.outline
    }
    val background = when (state) {
        OptionState.RIGHT -> AppColors.Correct.copy(alpha = 0.12f)
        OptionState.WRONG -> AppColors.Wrong.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(BorderStroke(if (state == OptionState.OPEN || state == OptionState.DIM) 1.dp else 2.dp, accent.copy(alpha = if (state == OptionState.DIM) 0.3f else 1f)), RoundedCornerShape(14.dp))
            .clickable(enabled = state == OptionState.OPEN, onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(if (state == OptionState.RIGHT || state == OptionState.WRONG) accent else MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(
                text = when (state) {
                    OptionState.RIGHT -> "✓"
                    OptionState.WRONG -> "✗"
                    else -> letter.toString()
                },
                color = if (state == OptionState.RIGHT || state == OptionState.WRONG) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (state == OptionState.DIM) 0.55f else 1f),
        )
    }
}

@Composable
private fun ExplanationBox(right: Boolean, answer: String, explanation: String) {
    val color = if (right) AppColors.Correct else AppColors.Wrong
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (right) "Correct!" else "Not quite. The answer is: $answer",
            color = color,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text("Why", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(explanation, style = MaterialTheme.typography.bodyMedium)
    }
}

// — Grandpyanne™
@Composable
private fun QuizResult(correct: Int, total: Int, percent: Int, best: Int, missed: Int, onRetry: () -> Unit, onDone: () -> Unit) {
    val color = when {
        percent >= 80 -> AppColors.Correct
        percent >= 50 -> AppColors.Safety
        else -> AppColors.Wrong
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProgressRing(fraction = percent / 100f, label = "$percent%", color = color, size = 140)
        Text("$correct of $total correct", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = when {
                percent >= 80 -> "Excellent work. You know this topic well."
                percent >= 50 -> "Good effort. Reread the notes for the ones you missed, then try again."
                else -> "Keep going. Read the notes first, then retry."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (missed > 0) "The $missed you missed will come first next time, until they stick."
            else "Questions you got right come back in a few days, to check they stuck.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (best >= 0) {
            Text("Best score: $best%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Try again") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}
