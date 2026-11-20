package com.grandpyanne.daemrevise.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grandpyanne.daemrevise.R
import com.grandpyanne.daemrevise.data.BlocksGame
import com.grandpyanne.daemrevise.data.CrushGame
import com.grandpyanne.daemrevise.data.Piece
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Swipe
import kotlin.math.abs
import kotlin.math.roundToInt

private const val BLOCKS = "blocks"
private const val CRUSH = "crush"
private const val MAHJONG = "mahjong"

private val BlockColors = listOf(
    Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF22C55E),
    Color(0xFFF2B134), Color(0xFFA855F7), Color(0xFF14B8A6),
)

// built by Grandpyanne™
/**
 * Kapi's games: a reward for studying. Games are earned (see [Progress.gamesAvailable]),
 * at most five a day, shared between Kapi Blocks, Kapi Jewels and Kapi Mahjong. Time spent playing is
 * not counted as study time.
 */
@Composable
fun GamesScreen(progress: Progress, onBack: () -> Unit, onQuickFive: () -> Unit) {
    remember { progress.flush(); true } // bring today's minutes up to date
    var playing by remember { mutableStateOf<String?>(null) }
    var round by remember { mutableIntStateOf(0) }

    fun start(game: String) {
        // Today's free try first (unless dev mode is on), then an earned game.
        val ok = (!Progress.DEV_UNLOCK_GAMES && progress.useFreeTry(game)) || progress.useGame()
        if (ok) {
            round++
            playing = game
        }
    }

    val game = playing
    if (game != null) {
        GameSession(
            game = game,
            round = round,
            progress = progress,
            onPlayAgain = { start(game) },
            onExit = { playing = null },
        )
    } else {
        GamesHub(progress, onBack, onQuickFive, ::start)
    }
}

@Composable
private fun GamesHub(progress: Progress, onBack: () -> Unit, onQuickFive: () -> Unit, start: (String) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LoungeBackground()
        Column(Modifier.fillMaxSize()) {
            // Light text on the dark lounge, whatever the phone's theme.
            MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
                Box(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f))) {
                    Header(title = "Kapi's games", subtitle = "Earned by studying · up to 5 a day", onBack = onBack)
                }
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    val available = progress.gamesAvailable()
                    if (Progress.DEV_UNLOCK_GAMES) {
                        Text(
                            "🛠 DEV MODE: games unlocked. Turn off DEV_UNLOCK_GAMES in Progress.kt before sharing.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFD9480F).copy(alpha = 0.85f))
                                .padding(8.dp),
                        )
                    }
                    GateCard(progress, available, onQuickFive)
                    GameCard(
                        title = "Kapi Blocks",
                        line = "Fill rows and columns",
                        best = progress.bestGameScore(BLOCKS),
                        enabled = available > 0,
                        onPlay = { start(BLOCKS) },
                        freeTry = progress.freeTryLeft(BLOCKS),
                    ) { BlocksThumb() }
                    GameCard(
                        title = "Kapi Jewels",
                        line = "Level ${progress.gameLevel(CRUSH)} · match 3 or more",
                        best = progress.bestGameScore(CRUSH),
                        enabled = available > 0,
                        onPlay = { start(CRUSH) },
                        freeTry = progress.freeTryLeft(CRUSH),
                    ) { JewelsThumb() }
                    GameCard(
                        title = "Kapi Mahjong",
                        line = "Level ${progress.gameLevel(MAHJONG)} · clear the pairs",
                        best = progress.bestGameScore(MAHJONG),
                        enabled = available > 0,
                        onPlay = { start(MAHJONG) },
                        freeTry = progress.freeTryLeft(MAHJONG),
                    ) { MahjongThumb() }
                    Text(
                        "One free try of each game a day · after that, study to earn more.\nLeaving a game part-way still uses it up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LoungeMist,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private val LoungeMist = Color(0xFFA9A6C9)
private val LoungeGold = Color(0xFFFFC94D)

/** An evening-lounge backdrop: indigo into plum, a warm glow up top, and faint game shapes. */
@Composable
private fun LoungeBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF17153A), Color(0xFF24194A), Color(0xFF331F45))))
        drawRect(
            Brush.radialGradient(
                listOf(LoungeGold.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(size.width / 2f, size.height * 0.24f),
                radius = size.width * 0.55f,
            ),
        )
        val rnd = kotlin.random.Random(7)
        val ink = Color.White.copy(alpha = 0.055f)
        repeat(24) { k ->
            val c = Offset(rnd.nextFloat() * size.width, size.height * (0.12f + rnd.nextFloat() * 0.88f))
            val r = size.width * (0.024f + rnd.nextFloat() * 0.02f)
            val stroke = Stroke(width = 2.5f)
            rotate(rnd.nextFloat() * 40f - 20f, c) {
                when (k % 3) {
                    0 -> { // a gem (hexagon)
                        val path = androidx.compose.ui.graphics.Path()
                        for (j in 0 until 6) {
                            val a = j * Math.PI / 3 - Math.PI / 2
                            val p = c + Offset((kotlin.math.cos(a) * r).toFloat(), (kotlin.math.sin(a) * r).toFloat())
                            if (j == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                        }
                        path.close()
                        drawPath(path, ink, style = stroke)
                    }
                    1 -> drawRoundRect(ink, c - Offset(r * 0.7f, r), Size(r * 1.4f, r * 2f), CornerRadius(r * 0.2f), style = stroke) // a tile
                    else -> { // three blocks
                        drawRect(ink, c - Offset(r, r), Size(r * 0.95f, r * 0.95f), style = stroke)
                        drawRect(ink, c - Offset(0f, r), Size(r * 0.95f, r * 0.95f), style = stroke)
                        drawRect(ink, c - Offset(r, 0f), Size(r * 0.95f, r * 0.95f), style = stroke)
                    }
                }
            }
        }
    }
}

/** Kapi under a soft spotlight: ready, napping (with a mystery progress bar), or done for today. */
@Composable
private fun GateCard(progress: Progress, available: Int, onQuickFive: () -> Unit) {
    val awake = available > 0
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Canvas(Modifier.size(170.dp, 26.dp)) {
                drawOval(Color.Black.copy(alpha = 0.28f), Offset(0f, size.height * 0.2f), Size(size.width, size.height * 0.8f))
            }
            Image(
                painter = painterResource(if (awake) R.drawable.mascot_happy else R.drawable.mascot_sleepy),
                contentDescription = "Kapi",
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(116.dp, 96.dp),
            )
        }
        val title: String
        val body: String
        when {
            awake -> {
                title = if (available == 1) "1 game ready" else "$available games ready"
                body = "Pick one. You earned it."
            }
            progress.gamesDoneForToday() -> {
                title = "That's today's games"
                body = "Kapi's gone to the hot spring ♨️ New games tomorrow."
            }
            else -> {
                title = "The games are napping"
                body = "Keep studying and they'll wake up. How long? Kapi won't say… but Kapi does have a lucky number. Acing a Quick 5 wakes them too (twice a day)."
            }
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC9C4E6), textAlign = TextAlign.Center)
        if (!awake && !progress.gamesDoneForToday()) {
            LinearProgressIndicator(
                progress = { progress.progressToNextGame() },
                color = LoungeGold,
                trackColor = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
            )
            OutlinedButton(onClick = onQuickFive) { Text("Try a Quick 5 ⚡", color = Color.White) }
        }
    }
}

/** One game: a small picture of it, its name, level and best score, and a gold Play button. */
@Composable
private fun GameCard(
    title: String,
    line: String,
    best: Int,
    enabled: Boolean,
    onPlay: () -> Unit,
    freeTry: Boolean = false,
    thumb: @Composable () -> Unit,
) {
    val canPlay = enabled || freeTry
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.25f)),
        ) { thumb() }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(line, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC9C4E6))
            Text(if (best > 0) "Best $best" else "No score yet", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = LoungeGold)
        }
        Spacer(Modifier.size(8.dp))
        Button(
            onClick = onPlay,
            enabled = canPlay,
            colors = ButtonDefaults.buttonColors(
                containerColor = LoungeGold,
                contentColor = Color(0xFF1A1A2E),
                disabledContainerColor = Color.White.copy(alpha = 0.10f),
                disabledContentColor = Color.White.copy(alpha = 0.6f),
            ),
        ) {
            Text(
                when {
                    freeTry && !Progress.DEV_UNLOCK_GAMES -> "Free try"
                    enabled -> "Play"
                    else -> "🔒"
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BlocksThumb() {
    Canvas(Modifier.size(64.dp)) {
        val cell = size.width / 3f
        val colors = BlockIconColors
        val cells = listOf(0 to 0 to 0, 0 to 1 to 0, 1 to 1 to 1, 1 to 2 to 1, 1 to 0 to 2, 2 to 0 to 3, 2 to 1 to 3, 2 to 2 to 3)
        cells.forEach { (rc, k) -> drawBlockIcon(colors[k], Offset(rc.second * cell, rc.first * cell), cell) }
    }
}

@Composable
private fun JewelsThumb() {
    Canvas(Modifier.size(70.dp)) {
        val cell = size.width * 0.5f
        drawJewelIcon(2, Offset(size.width * 0.28f, size.height * 0.3f), cell)
        drawJewelIcon(0, Offset(size.width * 0.74f, size.height * 0.34f), cell * 0.92f)
        drawJewelIcon(1, Offset(size.width * 0.5f, size.height * 0.74f), cell)
    }
}

@Composable
private fun MahjongThumb() {
    Box(Modifier.size(70.dp)) {
        MahjongIconTile(18, 34.dp, 44.dp, Modifier.offset(6.dp, 6.dp))
        MahjongIconTile(4, 34.dp, 44.dp, Modifier.offset(28.dp, 20.dp))
    }
}

/** One game being played, plus the game-over panel. Pauses the study clock while open. */
@Composable
private fun GameSession(game: String, round: Int, progress: Progress, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    DisposableEffect(Unit) {
        progress.pauseStudy()
        onDispose { progress.resumeStudy() }
    }
    var finalScore by remember(round) { mutableStateOf<Int?>(null) }
    var newBest by remember(round) { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    BackHandler {
        if (finalScore == null) confirmLeave = true else onExit()
    }

    fun finish(score: Int) {
        if (finalScore == null) {
            newBest = progress.recordGameScore(game, score)
            finalScore = score
        }
    }

    Column(Modifier.fillMaxSize()) {
        Header(
            title = when (game) {
                BLOCKS -> "Kapi Blocks"
                CRUSH -> "Kapi Jewels"
                else -> "Kapi Mahjong"
            },
            subtitle = "Best: ${progress.bestGameScore(game)}",
            onBack = { if (finalScore == null) confirmLeave = true else onExit() },
        )
        Box(Modifier.fillMaxSize()) {
            when (game) {
                BLOCKS -> BlocksPlay(round = round, progress = progress, onGameOver = ::finish)
                CRUSH -> CrushPlay(round = round, progress = progress, onGameOver = ::finish)
                else -> MahjongPlay(round = round, progress = progress, onGameOver = ::finish)
            }
            finalScore?.let { score ->
                GameOverPanel(
                    score = score,
                    best = progress.bestGameScore(game),
                    newBest = newBest,
                    gamesLeft = progress.gamesAvailable(),
                    onPlayAgain = onPlayAgain,
                    onDone = onExit,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave this game?") },
            text = { Text("The game ends here and it still counts as one of today's games.") },
            confirmButton = { TextButton(onClick = { confirmLeave = false; onExit() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Keep playing") } },
        )
    }
}

@Composable
private fun GameOverPanel(
    score: Int,
    best: Int,
    newBest: Boolean,
    gamesLeft: Int,
    onPlayAgain: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        Text(if (newBest) "New best! 🏆" else "Game over", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("$score", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Best: $best", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (gamesLeft > 0) {
            Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text(if (gamesLeft == 1) "Play again (1 game left)" else "Play again ($gamesLeft games left)") }
        } else {
            Text(
                "That was your last game for now. Back to the books, Kapi believes in you 📚",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

// ---- shared look (used by GameBlocks.kt, GameCrush.kt, GameMahjong.kt) ------------

/** A how-to card shown until it's dismissed once. */
@Composable
internal fun HowTo(progress: Progress, key: String, lines: List<String>) {
    var show by remember { mutableStateOf(!progress.tipSeen(key)) }
    if (show) Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(12.dp),
    ) {
        Text("How to play", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        lines.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        TextButton(
            onClick = { progress.markTipSeen(key); show = false },
            modifier = Modifier.align(Alignment.End),
        ) { Text("Got it") }
    }
}

/** A short burst of text ("+30", "Great!") that pops, floats up and fades. */
@Composable
internal fun FloatingCheer(text: String?, trigger: Int, modifier: Modifier = Modifier) {
    val anim = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (text != null && trigger > 0) {
            anim.snapTo(0f)
            anim.animateTo(1f, tween(1000))
        }
    }
    val t = anim.value
    if (text != null && t < 1f) {
        Text(
            text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = androidx.compose.ui.graphics.Shadow(Color(0xAA000000), Offset(0f, 4f), 10f),
            ),
            modifier = modifier.graphicsLayer {
                alpha = if (t < 0.7f) 1f else (1f - t) / 0.3f
                translationY = -110f * t
                val pop = if (t < 0.15f) 0.5f + t / 0.15f * 0.7f else 1.2f - 0.2f * ((t - 0.15f) / 0.85f)
                scaleX = pop
                scaleY = pop
            },
        )
    }
}

/** Little squares flying out of a cleared cell. [t] goes 0..1. */
internal fun DrawScope.drawBurst(center: Offset, color: Color, cell: Float, t: Float, seed: Int) {
    if (t >= 1f) return
    val rnd = kotlin.random.Random(seed)
    repeat(5) {
        val angle = rnd.nextFloat() * 6.2832f
        val speed = cell * (0.8f + rnd.nextFloat() * 1.2f)
        val p = center + Offset(kotlin.math.cos(angle) * speed * t, kotlin.math.sin(angle) * speed * t + cell * 0.6f * t * t)
        val s = cell * 0.16f * (1f - t)
        drawRect(color.copy(alpha = 1f - t), p - Offset(s / 2, s / 2), Size(s, s))
    }
}

/** Crush and Mahjong: one game can run through up to this many levels, then it's Finish. */
internal const val LEVELS_PER_GAME = 3

// ---- wooden pieces shared by the games ------------------------------------------

internal val Wood = Color(0xFF7A3F1C)
internal val WoodLight = Color(0xFFB0662F)
internal val WoodDark = Color(0xFF4A230C)

/** Rainbow gradient for big scores, as in the block puzzle samples. */
internal val RainbowText = Brush.linearGradient(
    listOf(Color(0xFF3CF2B3), Color(0xFFFFE066), Color(0xFFFF7EB6), Color(0xFFB69CFF), Color(0xFF4FD8FF)),
)

/** A round wooden button with an optional count badge, like the sample. */
@Composable
internal fun RoundButton(icon: String, count: Int?, enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(70.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .size(62.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(WoodLight, Wood)))
                .border(3.dp, Color(0xFF4A230C), CircleShape)
                .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
                .pointerInput(enabled) { detectTapGestures { if (enabled) onClick() } },
        ) {
            Text(icon, fontSize = 26.sp)
        }
        if (count != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9363E))
                    .border(2.dp, Color.White, CircleShape),
            ) {
                Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}


/** "Keep going?" card shown over a game: one rescue, or end the game. */
@Composable
internal fun RescuePanel(title: String, offer: String?, onRescue: () -> Unit, onEnd: () -> Unit, endLabel: String = "End game") {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(20.dp)
            .shadow(12.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(WoodLight, Wood)))
            .border(3.dp, WoodDark, RoundedCornerShape(22.dp))
            .padding(20.dp),
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        if (offer != null) {
            Button(
                onClick = onRescue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FA84F)),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(offer, fontWeight = FontWeight.Bold) }
        }
        OutlinedButton(onClick = onEnd, modifier = Modifier.fillMaxWidth()) { Text(endLabel, color = Color.White) }
    }
}
