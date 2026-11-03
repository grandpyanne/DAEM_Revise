package com.grandpyanne.daemrevise.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.R
import kotlinx.coroutines.launch

private val Gold = Color(0xFFF2B134)

/** One page of the welcome tour. */
private class WelcomePage(val sleepy: Boolean, val title: String, val intro: String, val points: List<Pair<String, String>>)

private val PAGES = listOf(
    WelcomePage(
        sleepy = false,
        title = "Hi, I'm Kapi 👋",
        intro = "A very chill capybara and your revision co-pilot for DAEM Year 2. Everything here comes from real KNEC papers and class notes, and it all works offline.",
        points = emptyList(),
    ),
    WelcomePage(
        sleepy = false,
        title = "Learn it",
        intro = "Open a unit, then a topic. Each topic has tabs:",
        points = listOf(
            "📝" to "Notes: short, exam-focused notes and tips",
            "📄" to "Papers: every KNEC question on the topic, with its year",
            "📐" to "Formulas and Facts for quick revision",
            "📘" to "Books: the exact textbook chapter and page, if you like working things out yourself",
        ),
    ),
    WelcomePage(
        sleepy = false,
        title = "Test yourself",
        intro = "Knowing it beats reading it.",
        points = listOf(
            "✅" to "Quizzes explain every answer, and bring back the ones you missed",
            "🃏" to "Flashcards use spaced repetition, like Anki: rate each card and it comes back just before you'd forget it",
            "🎯" to "Use \"This term\" in a unit to see only what you've covered",
            "🔎" to "Search any question by word or year, e.g. torsion 2024",
        ),
    ),
    WelcomePage(
        sleepy = true,
        title = "Keep the fire going 🔥",
        intro = "Study 5 minutes or more each day to build your streak. Skip a day and I fall asleep.",
        points = listOf(
            "⭐" to "Your study score (out of 100) mixes time, quiz accuracy and streak",
            "📱" to "Add me to your home screen to see your streak at a glance",
            "💬" to "Spot a mistake or have an idea? Tap Send feedback on the home screen",
        ),
    ),
)

// crafted by Grandpyanne™
/** First-run tour. [onDone] gets the name the student typed ("" if none). */
@Composable
fun WelcomeScreen(initialName: String, onDone: (String) -> Unit) {
    val pager = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialName) }
    val last = pager.currentPage == PAGES.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.NightTop, AppColors.NightBottom)))
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (!last) {
                TextButton(onClick = { onDone(name) }) { Text("Skip", color = Color.White.copy(alpha = 0.8f)) }
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = PAGES[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                FloatingKapi(sleepy = page.sleepy)
                Spacer(Modifier.height(20.dp))
                Text(
                    page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    page.intro,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
                if (page.points.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        page.points.forEach { (icon, text) ->
                            Row {
                                Text(icon, modifier = Modifier.width(30.dp))
                                Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                        }
                    }
                }
                if (index == PAGES.lastIndex) {
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(24) },
                        label = { Text("What should I call you? (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = Gold,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Gold,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(PAGES.size) { i ->
                val selected = i == pager.currentPage
                val width by animateDpAsState(if (selected) 22.dp else 8.dp, label = "dot")
                val color by animateColorAsState(if (selected) Gold else Color.White.copy(alpha = 0.35f), label = "dotColor")
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (last) onDone(name) else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = AppColors.NightTop),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(if (last) "Let's fly ✈️" else "Next", fontWeight = FontWeight.Bold)
        }
    }
}

/** Kapi gently bobbing up and down. */
@Composable
private fun FloatingKapi(sleepy: Boolean) {
    val bob = rememberInfiniteTransition(label = "bob")
    val y by bob.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "y",
    )
    Image(
        painter = painterResource(if (sleepy) R.drawable.mascot_sleepy else R.drawable.mascot_happy),
        contentDescription = "Kapi the capybara",
        modifier = Modifier
            .size(width = 180.dp, height = 150.dp)
            .offset(y = y.dp),
    )
}
