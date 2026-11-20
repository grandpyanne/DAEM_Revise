package com.grandpyanne.daemrevise.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grandpyanne.daemrevise.data.CrushGame
import com.grandpyanne.daemrevise.data.Progress
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// built by Grandpyanne™

/** Gem colours by kind: sapphire, emerald, ruby, topaz, amethyst. */
private val GemColors = listOf(
    Color(0xFF2F6BFF),
    Color(0xFF19B36B),
    Color(0xFFE5243F),
    Color(0xFFF2B320),
    Color(0xFF9B4DFF),
)

/** A calm night-navy background, like Kapi Blocks. */
private val Night = listOf(Color(0xFF0B1426), Color(0xFF111F3B), Color(0xFF0B1426))
private val Navy = Color(0xFF0F1C36)
private val NavyDeep = Color(0xFF1B2A4C)
private val Gold = Color(0xFFFFC94D)
private val Mist = Color(0xFFC9D3EA)

/** Falls with a small bounce at the end. */
private val DropEasing = CubicBezierEasing(0.3f, 1.25f, 0.6f, 1f)

private fun lerpOffset(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

/**
 * Kapi Crush. One game is a run of levels: beat a level and the next one follows in the same
 * game, with the score adding up. Run out of moves and you get one +5 moves rescue per game.
 */
@Composable
internal fun CrushPlay(round: Int, progress: Progress, onGameOver: (Int) -> Unit) {
    val startLevel = remember(round) { progress.gameLevel("crush") }
    var level by remember(round) { mutableIntStateOf(startLevel) }
    var runScore by remember(round) { mutableIntStateOf(0) }
    var rescueUsed by remember(round) { mutableStateOf(false) }
    // Levels beaten in this game (up to LEVELS_PER_GAME, then it's Finish).
    var levelsDone by remember(round) { mutableIntStateOf(0) }
    val best = progress.bestGameScore("crush")

    val game = remember(round, level) { CrushGame.forLevel(level) }
    val startMoves = remember(round, level) { game.movesLeft }
    var tick by remember(round, level) { mutableIntStateOf(0) }
    var refresh by remember(round, level) { mutableIntStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer()
    var cellPx by remember { mutableStateOf(0f) }

    // "intro", "won", "lost" or null (playing).
    var card by remember(round, level) { mutableStateOf<String?>("intro") }
    var stepIndex by remember(round, level) { mutableIntStateOf(-1) }
    var falling by remember(round, level) { mutableStateOf(false) }
    var busy by remember(round, level) { mutableStateOf(true) }
    val phase = remember { Animatable(1f) }
    var swapping by remember(round, level) { mutableStateOf<Pair<Int, Int>?>(null) }
    val swapAnim = remember { Animatable(0f) }
    var selected by remember(round, level) { mutableStateOf<Int?>(null) }
    var nope by remember(round, level) { mutableStateOf<Pair<Int, Int>?>(null) }
    var hint by remember(round, level) { mutableStateOf<Pair<Int, Int>?>(null) }
    var cheer by remember(round, level) { mutableStateOf<String?>(null) }
    var cheerTick by remember(round, level) { mutableIntStateOf(0) }
    val confetti = remember { Animatable(1f) }
    val total = runScore + game.score.also { tick }
    val shownScore by animateIntAsState(total, tween(500), label = "score")

    // Level intro card, then play.
    LaunchedEffect(round, level) {
        delay(1700)
        if (card == "intro") {
            card = null
            busy = false
        }
    }

    // Play the clearing steps of the last move, one after another.
    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        busy = true
        game.lastSteps.forEachIndexed { k, step ->
            stepIndex = k
            val label = step.events.firstOrNull() ?: when {
                step.level >= 5 -> "Brilliant!"
                step.level == 4 -> "Excellent!"
                step.level == 3 -> "Great!"
                step.level == 2 -> "Nice"
                else -> null
            }
            if (label != null) {
                cheer = label
                cheerTick++
            }
            if (step.blasts.isNotEmpty() || step.level > 1) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            falling = false
            phase.snapTo(0f)
            phase.animateTo(1f, tween(if (step.blasts.isNotEmpty()) 460 else 280, easing = LinearEasing))
            falling = true
            phase.snapTo(0f)
            phase.animateTo(1f, tween(340, easing = DropEasing))
        }
        stepIndex = -1
        falling = false
        if (game.isOver) {
            if (game.won) {
                progress.setGameLevel("crush", level + 1)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    confetti.snapTo(0f)
                    confetti.animateTo(1f, tween(2200, easing = LinearEasing))
                }
                delay(500)
                card = "won"
            } else {
                delay(300)
                card = "lost"
            }
        } else {
            busy = false
        }
    }
    // Stuck? After a while, two tiles wiggle to show a move.
    LaunchedEffect(tick, card) {
        hint = null
        delay(7000)
        if (!game.isOver && card == null) hint = game.hint()
    }
    LaunchedEffect(nope) {
        if (nope != null) {
            delay(320)
            nope = null
        }
    }
    val clock = rememberInfiniteTransition(label = "clock")
    val time by clock.animateFloat(0f, 1f, infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "time")
    val wig by clock.animateFloat(-1f, 1f, infiniteRepeatable(tween(220), RepeatMode.Reverse), label = "wig")
    val twinkle by clock.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "twinkle")

    fun trySwap(a: Int, b: Int) {
        selected = null
        if (busy || card != null) return
        busy = true
        scope.launch {
            swapping = a to b
            swapAnim.snapTo(0f)
            swapAnim.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
            if (game.swap(a, b)) {
                // Show the first clearing step straight away (no flash of the finished board).
                phase.snapTo(0f)
                falling = false
                stepIndex = 0
                swapping = null
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                tick++
            } else {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                swapAnim.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
                swapping = null
                nope = a to b
                busy = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        NightSky()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            HowTo(
                progress, "crush",
                listOf(
                    "• Swipe a jewel into its neighbour (or tap one, then the other) to swap them.",
                    "• Line up 3 or more of the same jewel to clear them. Cascades score more each time.",
                    "• 4 in a line makes a Beam jewel (clears its row or column).",
                    "• An L or T shape makes a Burst jewel (clears the 3×3 around it).",
                    "• 5 in a line makes a Prism: swap it with any jewel to clear every jewel of that colour.",
                    "• Swap two power-ups together for a big combo.",
                    "• Collect the level's goals before the moves run out. Beat a level and the next follows in the same game (up to 3 a game).",
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("♛", fontSize = 22.sp, color = Gold)
                Spacer(Modifier.size(6.dp))
                Text("${maxOf(best, total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gold, modifier = Modifier.weight(1f))
                Glass(Modifier.padding(0.dp)) {
                    Text("Level $level", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
            Text(
                "$shownScore",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(Color(0x66000000), Offset(0f, 4f), 10f),
                ),
            )

            // Goals and moves, on a frosted bar.
            Glass(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    tick + refresh
                    Text("Collect", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Mist)
                    game.goals.forEach { (kind, need) ->
                        val left = (need - game.collected[kind]).coerceAtLeast(0)
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Canvas(Modifier.size(38.dp)) {
                                drawGem(kind, CrushGame.NONE, game.kinds, center, size.width, 1f, twinkle = twinkle)
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (left == 0) Color(0xFF2FBF71) else NavyDeep)
                                    .border(2.dp, Color.White, CircleShape),
                            ) {
                                Text(if (left == 0) "✓" else "$left", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    HintButton(enabled = !busy && card == null) { hint = game.hint() }
                    val moves = game.movesLeft
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (moves <= 5) Color(0xFFB3261E) else Color(0xFF1E3A7A))
                            .border(2.5.dp, Gold, CircleShape),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$moves", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("moves", fontSize = 9.sp, color = Mist)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                val n = game.size
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(16.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Navy)
                        .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                        .padding(8.dp)
                        .onGloballyPositioned { cellPx = it.size.width / n.toFloat() }
                        .pointerInput(round, level) {
                            var from = -1
                            var moved = Offset.Zero
                            detectDragGestures(
                                onDragStart = { at ->
                                    val c = (at.x / cellPx).toInt().coerceIn(0, n - 1)
                                    val r = (at.y / cellPx).toInt().coerceIn(0, n - 1)
                                    from = r * n + c
                                    moved = Offset.Zero
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    moved += amount
                                    if (from >= 0 && maxOf(abs(moved.x), abs(moved.y)) > cellPx * 0.35f) {
                                        val r = from / n
                                        val c = from % n
                                        val to = if (abs(moved.x) > abs(moved.y)) {
                                            if (moved.x > 0 && c + 1 < n) from + 1 else if (moved.x < 0 && c > 0) from - 1 else -1
                                        } else {
                                            if (moved.y > 0 && r + 1 < n) from + n else if (moved.y < 0 && r > 0) from - n else -1
                                        }
                                        if (to >= 0) trySwap(from, to)
                                        from = -1
                                    }
                                },
                            )
                        }
                        .pointerInput(round, level, "tap") {
                            detectTapGestures { at ->
                                val c = (at.x / cellPx).toInt().coerceIn(0, n - 1)
                                val r = (at.y / cellPx).toInt().coerceIn(0, n - 1)
                                val i = r * n + c
                                val s0 = selected
                                selected = when {
                                    s0 == null -> i
                                    s0 == i -> null
                                    else -> { trySwap(s0, i); null }
                                }
                            }
                        },
                ) {
                    tick + refresh
                    val cell = size.width / n
                    // Slots, in a soft checkerboard.
                    for (i in 0 until n * n) {
                        drawRoundRect(
                            Color.White.copy(alpha = if (((i / n) + (i % n)) % 2 == 0) 0.065f else 0.035f),
                            Offset((i % n) * cell + cell * 0.04f, (i / n) * cell + cell * 0.04f),
                            Size(cell * 0.92f, cell * 0.92f),
                            CornerRadius(cell * 0.22f),
                        )
                    }
                    val step = game.lastSteps.getOrNull(stepIndex)
                    val t = phase.value
                    // Big moves shake the board a little.
                    val big = step != null && !falling && (step.blasts.isNotEmpty() || step.level >= 3)
                    val shake = if (big) cell * 0.07f * (1f - t) * sin(t * 48f) else 0f
                    translate(shake, -shake * 0.6f) {
                        clipRect {
                            if (step != null && !falling) {
                                for (i in 0 until n * n) {
                                    val center = Offset((i % n + 0.5f) * cell, (i / n + 0.5f) * cell)
                                    val kind = step.before[i]
                                    if (i in step.cleared) {
                                        val sc = if (t < 0.25f) 1f + t * 0.8f else (1.2f - (t - 0.25f) / 0.75f * 1.2f).coerceAtLeast(0f)
                                        if (sc > 0f) drawGem(kind, step.beforeSpecial[i], game.kinds, center, cell, sc, flash = 1f - t, twinkle = twinkle)
                                        drawSparkleBurst(center, gemColor(kind, game.kinds), cell, t, seed = i * 17 + stepIndex)
                                    } else {
                                        drawGem(kind, step.beforeSpecial[i], game.kinds, center, cell, 1f, twinkle = twinkle, wobble = time * 40f + i)
                                    }
                                }
                                step.blasts.forEach { (cellIndex, type) -> drawBlast(cellIndex, type, n, cell, t, twinkle) }
                                // Points float up from the middle of what cleared.
                                if (step.points > 0) {
                                    val mid = step.cleared.fold(Offset.Zero) { acc, i -> acc + Offset((i % n + 0.5f) * cell, (i / n + 0.5f) * cell) } / step.cleared.size.toFloat()
                                    val style = TextStyle(
                                        fontSize = (18 + minOf(step.level, 4) * 3).sp,
                                        fontWeight = FontWeight.Black,
                                        color = Gold.copy(alpha = (1.4f - t).coerceIn(0f, 1f)),
                                        shadow = androidx.compose.ui.graphics.Shadow(Color(0xAA000000), Offset(0f, 3f), 6f),
                                    )
                                    val layout = measurer.measure("+${step.points}", style)
                                    drawText(layout, topLeft = mid - Offset(layout.size.width / 2f, layout.size.height / 2f + cell * 0.9f * t))
                                }
                            } else if (step != null) {
                                for (i in 0 until n * n) {
                                    val drop = step.fall[i] * cell * (1f - t)
                                    val center = Offset((i % n + 0.5f) * cell, (i / n + 0.5f) * cell - drop)
                                    drawGem(step.after[i], step.afterSpecial[i], game.kinds, center, cell, 1f, twinkle = twinkle)
                                }
                            } else {
                                val sw = swapping
                                val st = swapAnim.value
                                for (i in 0 until n * n) {
                                    val jiggle = when {
                                        nope?.let { i == it.first || i == it.second } == true -> cell * 0.08f * wig
                                        hint?.let { i == it.first || i == it.second } == true -> cell * 0.05f * wig
                                        else -> 0f
                                    }
                                    val home = Offset((i % n + 0.5f) * cell + jiggle, (i / n + 0.5f) * cell)
                                    val center = when {
                                        sw != null && i == sw.first -> lerpOffset(home, Offset((sw.second % n + 0.5f) * cell, (sw.second / n + 0.5f) * cell), st)
                                        sw != null && i == sw.second -> lerpOffset(home, Offset((sw.first % n + 0.5f) * cell, (sw.first / n + 0.5f) * cell), st)
                                        else -> home
                                    }
                                    val sel = selected == i
                                    if (sel) {
                                        drawRoundRect(
                                            Gold.copy(alpha = 0.28f),
                                            Offset((i % n) * cell, (i / n) * cell),
                                            Size(cell, cell),
                                            CornerRadius(cell * 0.25f),
                                        )
                                    }
                                    drawGem(game.board[i], game.special[i], game.kinds, center, cell, if (sel) 1.1f else 1f, twinkle = twinkle, wobble = time * 40f + i)
                                }
                            }
                        }
                    }
                }
                FloatingCheer(cheer, cheerTick)
                when (card) {
                    "intro" -> LevelCard(level, game, startMoves, twinkle) {
                        card = null
                        busy = false
                    }
                    "won" -> {
                        val stars = when {
                            game.movesLeft >= startMoves * 0.3f -> 3
                            game.movesLeft >= startMoves * 0.1f -> 2
                            else -> 1
                        }
                        val more = levelsDone + 1 < LEVELS_PER_GAME
                        ResultCard(
                            title = "Level $level complete!",
                            stars = stars,
                            lines = listOf(
                                "Score ${game.score}",
                                if (game.bonus > 0) "+${game.bonus} for moves left" else "",
                                if (more) "Level ${levelsDone + 1} of $LEVELS_PER_GAME this game" else "That's $LEVELS_PER_GAME levels: game complete! 🏆",
                            ),
                            primary = if (more) "Next level ▶" else null,
                            onPrimary = {
                                levelsDone += 1
                                runScore += game.score
                                level += 1
                            },
                            secondary = "Finish",
                            onSecondary = { onGameOver(runScore + game.score) },
                        )
                    }
                    "lost" -> ResultCard(
                        title = "Out of moves!",
                        stars = 0,
                        lines = game.goals.filter { (k, need) -> game.collected[k] < need }
                            .map { (k, need) -> "Still need ${need - game.collected[k]} ${GemNames[k]}" },
                        primary = if (!rescueUsed) "+5 moves (once)" else null,
                        onPrimary = {
                            rescueUsed = true
                            game.addMoves(5)
                            refresh++
                            card = null
                            busy = false
                        },
                        secondary = "End game",
                        onSecondary = { onGameOver(runScore + game.score) },
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Swipe a jewel, or tap two neighbours, to swap",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8592B3),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            )
        }
        // Confetti when a level is beaten.
        val c = confetti.value
        if (c < 1f) {
            Canvas(Modifier.fillMaxSize()) {
                val rnd = kotlin.random.Random(42)
                repeat(90) {
                    val x0 = rnd.nextFloat() * size.width
                    val speed = 0.6f + rnd.nextFloat() * 0.8f
                    val y = -40f + (size.height + 80f) * c * speed
                    val x = x0 + sin(c * 12f + it) * 30f
                    val color = if (it % 3 == 0) Gold else GemColors[it % GemColors.size]
                    rotate(c * 720f * (if (it % 2 == 0) 1f else -1f) + it * 20f, Offset(x, y)) {
                        drawRect(color, Offset(x - 6f, y - 3f), Size(12f, 6f))
                    }
                }
            }
        }
    }
}

private val GemNames = listOf("sapphires", "emeralds", "rubies", "topazes", "amethysts")

/** A small lightbulb button in the goals bar: shows a possible swap. */
@Composable
private fun HintButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.5.dp, Gold.copy(alpha = if (enabled) 0.6f else 0.25f), CircleShape)
            .pointerInput(enabled) { detectTapGestures { if (enabled) onClick() } },
    ) {
        Text("💡", fontSize = 20.sp, color = Color.White.copy(alpha = if (enabled) 1f else 0.4f))
    }
}

/** A quiet translucent panel. */
@Composable
private fun Glass(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp)),
    ) { content() }
}

/** The background: a plain night-navy gradient (no bubbles). */
@Composable
private fun NightSky() {
    Canvas(Modifier.fillMaxSize()) { drawRect(Brush.verticalGradient(Night)) }
}

/** "Level 5 · Collect 14 🍒 · 22 moves", shown before a level starts. Tap to skip. */
@Composable
private fun LevelCard(level: Int, game: CrushGame, moves: Int, twinkle: Float, onGo: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(24.dp)
            .shadow(16.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1E2F57), Color(0xFF121D38))))
            .border(1.5.dp, Gold.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
            .pointerInput(Unit) { detectTapGestures { onGo() } }
            .padding(horizontal = 28.dp, vertical = 20.dp),
    ) {
        Text("Level $level", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Collect", fontSize = 14.sp, color = Mist)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            game.goals.forEach { (kind, need) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(46.dp)) { drawGem(kind, CrushGame.NONE, game.kinds, center, size.width, 1f, twinkle = twinkle) }
                    Text("$need", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Text("in $moves moves", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Gold)
        Text("Tap to start", fontSize = 11.sp, color = Mist.copy(alpha = 0.7f))
    }
}

/** Level won (with stars) or lost (with the rescue offer). */
@Composable
private fun ResultCard(
    title: String,
    stars: Int,
    lines: List<String>,
    primary: String?,
    onPrimary: () -> Unit,
    secondary: String,
    onSecondary: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(20.dp)
            .shadow(16.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1E2F57), Color(0xFF121D38))))
            .border(1.5.dp, Gold.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
            .padding(22.dp),
    ) {
        if (stars > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (k in 1..3) Text("★", fontSize = if (k == 2) 42.sp else 34.sp, color = if (k <= stars) Gold else Color.White.copy(alpha = 0.18f))
            }
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        lines.filter { it.isNotBlank() }.forEach { Text(it, fontSize = 14.sp, color = Mist, textAlign = TextAlign.Center) }
        if (primary != null) {
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF1A1A2E)),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(primary, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
        OutlinedButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) { Text(secondary, color = Mist) }
    }
}

private fun gemColor(kind: Int, kinds: Int): Color =
    if (kind in 0 until kinds) GemColors[kind % GemColors.size] else Color.White

/** Light beams for Beam jewels, a gold shockwave for Bursts, a white ring for a Prism. [t] goes 0..1. */
private fun DrawScope.drawBlast(cellIndex: Int, type: Int, n: Int, cell: Float, t: Float, twinkle: Float) {
    val c = Offset((cellIndex % n + 0.5f) * cell, (cellIndex / n + 0.5f) * cell)
    val fade = (1f - t).coerceIn(0f, 1f)
    when (type) {
        CrushGame.JET_ROW -> {
            val h = cell * (0.25f + 0.5f * fade)
            drawRect(
                Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = fade), Color.Transparent), startY = c.y - h, endY = c.y + h),
                Offset(0f, c.y - h),
                Size(size.width, h * 2),
            )
        }
        CrushGame.JET_COL -> {
            val w = cell * (0.25f + 0.5f * fade)
            drawRect(
                Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = fade), Color.Transparent), startX = c.x - w, endX = c.x + w),
                Offset(c.x - w, 0f),
                Size(w * 2, size.height),
            )
        }
        CrushGame.BOMB -> {
            drawCircle(Color(0xFFFFE27A).copy(alpha = fade), cell * (0.5f + 2.2f * t), c, style = Stroke(cell * 0.3f * fade))
            drawCircle(Color.White.copy(alpha = fade * 0.6f), cell * (0.3f + 1.4f * t), c)
        }
        CrushGame.SPLASH -> {
            drawCircle(
                Brush.radialGradient(listOf(Color.White, Color(0xFFBFD8FF), Color.Transparent), c, cell * (0.6f + 7f * t)),
                cell * (0.5f + 7f * t),
                c,
                alpha = fade,
                style = Stroke(cell * 0.4f * fade),
            )
        }
    }
}

/** Little sparks flying out of a cleared jewel. */
private fun DrawScope.drawSparkleBurst(center: Offset, color: Color, cell: Float, t: Float, seed: Int) {
    if (t >= 1f) return
    val rnd = kotlin.random.Random(seed)
    repeat(7) { k ->
        val angle = rnd.nextFloat() * 6.2832f
        val speed = cell * (0.7f + rnd.nextFloat() * 1.3f)
        val p = center + Offset(cos(angle) * speed * t, sin(angle) * speed * t + cell * 0.5f * t * t)
        val s = cell * (0.12f + rnd.nextFloat() * 0.08f) * (1f - t)
        if (k % 2 == 0) {
            drawCircle(lerp(color, Color.White, 0.3f).copy(alpha = 1f - t), s, p)
        } else {
            drawStar(p, s * 1.4f, Color.White.copy(alpha = 1f - t))
        }
    }
}

private fun DrawScope.drawStar(c: Offset, r: Float, color: Color) {
    val path = Path()
    for (k in 0 until 8) {
        val a = -PI / 2 + k * PI / 4
        val rr = if (k % 2 == 0) r else r * 0.35f
        val x = c.x + (cos(a) * rr).toFloat()
        val y = c.y + (sin(a) * rr).toFloat()
        if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/**
 * One jewel: a cut gemstone with its own outline per colour (round sapphire, emerald cut,
 * hexagonal ruby, lozenge topaz, trillion amethyst), facets lit from the top left, a table on
 * top and a glint. Power-ups: light bars for a Beam, a glowing cracked core for a Burst, and a
 * white diamond with rainbow fire for a Prism.
 */
private fun DrawScope.drawGem(
    kind: Int,
    special: Int,
    kinds: Int,
    center: Offset,
    cell: Float,
    zoom: Float,
    flash: Float = 0f,
    twinkle: Float = 0f,
    wobble: Float = 0f,
) {
    if (kind < 0) return
    val r = cell * 0.4f
    val prism = special == CrushGame.SPLASH || kind >= kinds
    val shape = if (prism) 0 else kind % 5
    val base = lerp(if (prism) Color(0xFFE8F1FF) else gemColor(kind, kinds), Color.White, flash.coerceIn(0f, 1f) * 0.75f)
    val outer = gemOutline(shape, center, r)
    val table = gemOutline(shape, center - Offset(0f, r * 0.04f), r * (if (shape == 4) 0.42f else 0.52f))
    val sway = 0.012f * sin(wobble)
    scale(zoom * (1f + sway), center) {
        // Shadow.
        drawPath(polygon(outer.map { it + Offset(0f, r * 0.12f) }), Color(0x73000000))
        // Facets between the outline and the table, shaded by which way they face.
        val light = -2.3f
        for (i in outer.indices) {
            val j = (i + 1) % outer.size
            val mid = (outer[i] + outer[j]) / 2f - center
            val facing = (cos(kotlin.math.atan2(mid.y, mid.x) - light) + 1f) / 2f
            val shade = if (facing > 0.5f) lerp(base, Color.White, (facing - 0.5f) * 0.9f) else lerp(base, Color.Black, (0.5f - facing) * 1.1f)
            val facet = polygon(listOf(outer[i], outer[j], table[j], table[i]))
            drawPath(facet, shade)
            drawPath(facet, lerp(base, Color.Black, 0.55f).copy(alpha = 0.35f), style = Stroke(r * 0.025f))
        }
        // The table (flat top), lit diagonally.
        drawPath(
            polygon(table),
            Brush.linearGradient(
                0f to lerp(base, Color.White, 0.45f), 0.55f to base, 1f to lerp(base, Color.Black, 0.25f),
                start = center - Offset(r * 0.5f, r * 0.5f), end = center + Offset(r * 0.5f, r * 0.5f),
            ),
        )
        for (i in outer.indices) drawLine(Color.White.copy(alpha = 0.25f), table[i], outer[i], strokeWidth = r * 0.02f)
        if (prism) {
            // Rainbow fire turning slowly inside the diamond.
            clipPolygon(outer) {
                val fire = listOf(Color(0xFFFF6B8A), Color(0xFFFFD166), Color(0xFF5FE3C0), Color(0xFF6CA8FF), Color(0xFFC08BFF))
                fire.forEachIndexed { k, c ->
                    drawArc(c.copy(alpha = 0.3f), twinkle * 360f + k * 72f, 28f, true, center - Offset(r, r), Size(r * 2, r * 2))
                }
            }
        }
        drawPath(polygon(outer), lerp(base, Color.Black, 0.6f), style = Stroke(r * 0.05f))
        // Highlight and an occasional glint.
        rotate(-34f, center - Offset(r * 0.22f, r * 0.28f)) {
            drawOval(Color.White.copy(alpha = 0.55f), center - Offset(r * 0.44f, r * 0.37f), Size(r * 0.44f, r * 0.18f))
        }
        val glint = ((twinkle + kind * 0.2f + center.x * 0.0007f) % 1f)
        if (glint < 0.16f) drawStar(center + Offset(r * 0.36f, -r * 0.42f), r * 0.26f * sin(glint / 0.16f * PI.toFloat()), Color.White)

        when (special) {
            CrushGame.JET_ROW, CrushGame.JET_COL -> {
                // Two bars of light across the jewel, and chevrons showing which way it fires.
                val horizontal = special == CrushGame.JET_ROW
                val pulse = 0.7f + 0.3f * sin(twinkle * 6.28f * 2f)
                clipPolygon(outer) {
                    for (o in listOf(-0.22f, 0.22f)) {
                        if (horizontal) {
                            drawRect(
                                Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.9f * pulse), Color.Transparent), startY = center.y + o * r - r * 0.1f, endY = center.y + o * r + r * 0.1f),
                                Offset(center.x - r, center.y + o * r - r * 0.1f), Size(r * 2, r * 0.2f),
                            )
                        } else {
                            drawRect(
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.9f * pulse), Color.Transparent), startX = center.x + o * r - r * 0.1f, endX = center.x + o * r + r * 0.1f),
                                Offset(center.x + o * r - r * 0.1f, center.y - r), Size(r * 0.2f, r * 2),
                            )
                        }
                    }
                }
                for (sgn in listOf(-1f, 1f)) {
                    val tip = if (horizontal) center + Offset(sgn * r * 1.18f, 0f) else center + Offset(0f, sgn * r * 1.18f)
                    val a = if (horizontal) center + Offset(sgn * r * 1.0f, -r * 0.16f) else center + Offset(-r * 0.16f, sgn * r * 1.0f)
                    val b = if (horizontal) center + Offset(sgn * r * 1.0f, r * 0.16f) else center + Offset(r * 0.16f, sgn * r * 1.0f)
                    drawLine(Color.White.copy(alpha = 0.9f), a, tip, strokeWidth = r * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.9f), b, tip, strokeWidth = r * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            }
            CrushGame.BOMB -> {
                // Light leaking out of a cracked core, with a gold ring that breathes.
                val pulse = 0.5f + 0.5f * sin(twinkle * 6.28f * 2f)
                drawCircle(
                    Brush.radialGradient(listOf(Color(0xFFFFE696).copy(alpha = 0.95f), Color(0x00FFAA28)), center, r * 0.8f),
                    r * 0.8f, center,
                )
                for (k in 0 until 5) {
                    val a = k * 1.2566f + 0.3f
                    val p1 = center + Offset(cos(a) * r * 0.35f, sin(a) * r * 0.35f)
                    val p2 = center + Offset(cos(a + 0.25f) * r * 0.7f, sin(a + 0.25f) * r * 0.7f)
                    drawLine(Color(0xFFFFF0BE), center, p1, strokeWidth = r * 0.06f)
                    drawLine(Color(0xFFFFF0BE), p1, p2, strokeWidth = r * 0.06f)
                }
                drawCircle(Gold.copy(alpha = 0.35f + 0.4f * pulse), r * 1.12f, center, style = Stroke(r * 0.08f))
            }
        }
    }
}

private fun polygon(points: List<Offset>): Path = Path().apply {
    points.forEachIndexed { k, p -> if (k == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
    close()
}

/** Draws [block] clipped to a jewel's outline. */
private inline fun DrawScope.clipPolygon(points: List<Offset>, block: DrawScope.() -> Unit) {
    drawContext.canvas.save()
    drawContext.canvas.clipPath(polygon(points))
    block()
    drawContext.canvas.restore()
}

/** Each jewel's outline: 0 round (octagon), 1 emerald cut, 2 hexagon, 3 lozenge, 4 trillion. */
private fun gemOutline(kind: Int, c: Offset, r: Float): List<Offset> {
    if (kind == 1) {
        val w = 0.74f; val h = 0.92f; val k = 0.26f
        return listOf(
            -w + k to -h, w - k to -h, w to -h + k, w to h - k,
            w - k to h, -w + k to h, -w to h - k, -w to -h + k,
        ).map { (x, y) -> Offset(c.x + x * r, c.y + y * r) }
    }
    val n = when (kind) { 0 -> 8; 2 -> 6; 3 -> 4; else -> 3 }
    val start = if (kind == 0) PI / 8 else -PI / 2
    val rr = when (kind) { 3 -> 1.04f; 4 -> 1.18f; else -> 1f }
    val dy = if (kind == 4) 0.2f else 0f
    return List(n) { k ->
        val a = start + k * 2 * PI / n
        Offset(c.x + (cos(a) * rr * r).toFloat(), c.y + (sin(a) * rr * r).toFloat() + dy * r)
    }
}

/** A jewel for the games screen's card. */
internal fun DrawScope.drawJewelIcon(kind: Int, center: Offset, cell: Float) =
    drawGem(kind, CrushGame.NONE, 5, center, cell, 1f)
