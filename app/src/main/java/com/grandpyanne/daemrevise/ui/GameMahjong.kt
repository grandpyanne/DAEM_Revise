package com.grandpyanne.daemrevise.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grandpyanne.daemrevise.data.MahjongGame
import com.grandpyanne.daemrevise.data.Progress

// crafted by Grandpyanne™

/**
 * Classic tile faces, drawn by the app: circles 1–9 (kinds 0–8), bamboo 1–9 (9–17),
 * and the red 中 and green 發 honour tiles (18, 19). 20 kinds.
 */
private const val FACE_KINDS = 20

private val TileFace = Color(0xFFFBF8EF)
private val TileFaceLow = Color(0xFFEDE7D6)
private val TileEdge = Color(0xFF2FA84F)
private val TileEdgeDark = Color(0xFF1E7A36)
private val Felt = listOf(Color(0xFF0C3F37), Color(0xFF125548), Color(0xFF0C3F37))

@Composable
internal fun MahjongPlay(round: Int, progress: Progress, onGameOver: (Int) -> Unit) {
    val startLevel = remember(round) { progress.gameLevel("mahjong") }
    var level by remember(round) { mutableIntStateOf(startLevel) }
    var runScore by remember(round) { mutableIntStateOf(0) }
    // Levels cleared in this game (up to LEVELS_PER_GAME, then it's Finish).
    var levelsDone by remember(round) { mutableIntStateOf(0) }
    val game = remember(round, level) { MahjongGame(level) }
    var tick by remember(round, level) { mutableIntStateOf(0) }
    val haptics = LocalHapticFeedback.current
    var shakeAt by remember(round, level) { mutableStateOf<Int?>(null) }
    var hintTiles by remember(round, level) { mutableStateOf<List<Int>>(emptyList()) }
    // The tile that just went up, and the pair that just cleared from the tray (for animations).
    var arrived by remember(round, level) { mutableStateOf<Int?>(null) }
    val arriveAnim = remember { Animatable(1f) }
    var clearedPair by remember(round, level) { mutableStateOf<Pair<Int, Int>?>(null) }
    var clearedTraySlots by remember(round, level) { mutableStateOf<List<Int>>(emptyList()) }
    val clearAnim = remember { Animatable(1f) }
    var cheer by remember(round, level) { mutableStateOf<String?>(null) }
    var cheerTick by remember(round, level) { mutableIntStateOf(0) }
    // "won", "lost" or null.
    var card by remember(round, level) { mutableStateOf<String?>(null) }
    val total = runScore + game.score.also { tick }
    val shownScore by animateIntAsState(total, tween(400), label = "score")
    val best = progress.bestGameScore("mahjong")

    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow by pulse.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "glow")
    val wig by pulse.animateFloat(-1f, 1f, infiniteRepeatable(tween(70), RepeatMode.Reverse), label = "wig")

    LaunchedEffect(shakeAt) {
        if (shakeAt != null) {
            kotlinx.coroutines.delay(300)
            shakeAt = null
        }
    }
    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        if (arrived != null) {
            arriveAnim.snapTo(0f)
            arriveAnim.animateTo(1f, tween(220))
        }
        if (clearedPair != null) {
            clearAnim.snapTo(0f)
            clearAnim.animateTo(1f, tween(360))
            clearedPair = null
            clearedTraySlots = emptyList()
        }
        when {
            game.won -> {
                progress.setGameLevel("mahjong", level + 1)
                cheer = "Level $level cleared"
                cheerTick++
                kotlinx.coroutines.delay(700)
                card = "won"
            }
            game.lost -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                kotlinx.coroutines.delay(400)
                card = "lost"
            }
            game.tray.size == MahjongGame.TRAY_SIZE - 1 -> {
                cheer = "Careful: 1 slot left!"
                cheerTick++
            }
        }
    }

    fun tap(i: Int) {
        if (card != null || game.state[i] != MahjongGame.ON_BOARD || clearedPair != null) return
        if (!game.isFree(i)) {
            shakeAt = i
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            return
        }
        val trayBefore = game.tray.toList()
        if (game.pick(i)) {
            hintTiles = emptyList()
            val cleared = game.lastCleared
            if (cleared != null) {
                // The partner's slot in the tray, and the new tile's would-be slot, both burst.
                clearedTraySlots = listOf(trayBefore.indexOf(cleared.first), trayBefore.size)
                clearedPair = cleared
                arrived = null
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (game.streak >= 3) {
                    cheer = "Streak ×${game.streak}"
                    cheerTick++
                }
            } else {
                arrived = i
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            tick++
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(Felt)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            HowTo(
                progress, "mahjong",
                listOf(
                    "• Tap a free tile and it flies up into the tray at the top.",
                    "• Two matching tiles in the tray clear. If all 4 tray slots fill with no match, the game is over!",
                    "• Free tiles have nothing lying on top and an open side (left or right). Shaded tiles can't be picked yet.",
                    "• Clear every tile to finish the level, and the next level follows in the same game (up to 3 a game).",
                    "• Help: ↩ Undo, 💡 Hint (3), 🔀 Shuffle (2), and once a game you can send the tray back.",
                ),
            )
            // Top bar: best score, level name.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                Text("♛", fontSize = 22.sp, color = Color(0xFFFFC94D))
                Spacer(Modifier.size(4.dp))
                Text("${maxOf(best, total)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFC93C))
                Spacer(Modifier.weight(1f))
                Text(
                    "Level $level · ${MahjongGame.layoutName(level)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$shownScore", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFE27A))
                Spacer(Modifier.size(10.dp))
                Text("${game.tilesLeft.also { tick }} tiles left", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 8.dp))
            }

            // The tray: 4 wooden slots.
            TrayBar(
                tiles = game.tray.toList().also { tick },
                faceOf = { game.face[it] % FACE_KINDS },
                arrived = arrived,
                arriveT = arriveAnim.value,
                clearing = clearedPair?.let { (a, _) -> listOf(game.face[a] % FACE_KINDS) } ?: emptyList(),
                clearingSlots = clearedTraySlots,
                clearT = clearAnim.value,
            )
            Spacer(Modifier.height(8.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().weight(1f)) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val depth: Dp = 6.dp
                    val lift: Dp = 7.dp // how far each layer sits up-left
                    val byWidth = (maxWidth - depth - lift * game.layers) / (game.width / 2f)
                    val byHeight = (maxHeight - depth - lift * game.layers) / (game.height / 2f * 1.3f)
                    val tileW: Dp = if (byWidth < byHeight) byWidth else byHeight
                    val tileH: Dp = tileW * 1.3f
                    val boardW = tileW * (game.width / 2f) + depth + lift * game.layers
                    val boardH = tileH * (game.height / 2f) + depth + lift * game.layers
                    val left0 = (maxWidth - boardW) / 2 + lift * game.layers
                    val top0 = (maxHeight - boardH) / 2 + lift * game.layers
                    Box(Modifier.fillMaxSize()) {
                        tick
                        game.slots.forEachIndexed { i, slot ->
                            if (game.state[i] == MahjongGame.ON_BOARD) {
                                val x = left0 + tileW * (slot.x / 2f) - lift * slot.layer
                                val y = top0 + tileH * (slot.y / 2f) - lift * slot.layer
                                val isHint = i in hintTiles
                                MahjongTile(
                                    face = game.face[i] % FACE_KINDS,
                                    width = tileW,
                                    height = tileH,
                                    depth = depth,
                                    layer = slot.layer,
                                    free = game.isFree(i),
                                    selected = false,
                                    glow = if (isHint) glow else 0f,
                                    modifier = Modifier
                                        .offset(x, y)
                                        .graphicsLayer { if (shakeAt == i) translationX = 7f * wig }
                                        .pointerInput(round, level, i) { detectTapGestures { tap(i) } },
                                )
                            }
                        }
                    }
                }
                FloatingCheer(cheer, cheerTick)
                when (card) {
                    "won" -> RescuePanel(
                        title = if (levelsDone + 1 < LEVELS_PER_GAME) {
                            "Level $level cleared\nScore ${game.score}\nLevel ${levelsDone + 1} of $LEVELS_PER_GAME this game"
                        } else {
                            "Level $level cleared\nScore ${game.score}\nThat's $LEVELS_PER_GAME levels: game complete"
                        },
                        offer = if (levelsDone + 1 < LEVELS_PER_GAME) "Next level ▶" else null,
                        onRescue = {
                            levelsDone += 1
                            runScore += game.score
                            level += 1
                        },
                        onEnd = { onGameOver(runScore + game.score) },
                        endLabel = "Finish",
                    )
                    "lost" -> RescuePanel(
                        title = "The tray is full!",
                        offer = if (game.rescueLeft) "↩ Send the tray back (once)" else null,
                        onRescue = {
                            if (game.rescue()) {
                                card = null
                                tick++
                            }
                        },
                        onEnd = { onGameOver(runScore + game.score) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundButton("🔀", game.shufflesLeft.also { tick }, enabled = game.shufflesLeft > 0 && card == null) {
                    if (game.shuffle()) {
                        hintTiles = emptyList()
                        cheer = "Shuffled"
                        cheerTick++
                        tick++
                    }
                }
                RoundButton("💡", game.hintsLeft.also { tick }, enabled = game.hintsLeft > 0 && card == null) {
                    hintTiles = game.useHint() ?: emptyList()
                    tick++
                }
                RoundButton("↩", null, enabled = game.canUndo.also { tick } && card == null) {
                    if (game.undo() != null) {
                        hintTiles = emptyList()
                        arrived = null
                        tick++
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

/** The 4-slot wooden tray. A tile drops in when it arrives; a matched pair bursts away. */
@Composable
private fun TrayBar(
    tiles: List<Int>,
    faceOf: (Int) -> Int,
    arrived: Int?,
    arriveT: Float,
    clearing: List<Int>,
    clearingSlots: List<Int>,
    clearT: Float,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(WoodLight, Wood)))
            .border(3.dp, WoodDark, RoundedCornerShape(14.dp))
            .padding(8.dp),
    ) {
        for (k in 0 until MahjongGame.TRAY_SIZE) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(58.dp, 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3B1F0D)),
            ) {
                val tile = tiles.getOrNull(k)
                if (tile != null) {
                    val t = if (tile == arrived) arriveT else 1f
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = 0.4f + 0.6f * t
                                scaleY = 0.4f + 0.6f * t
                                translationY = -60f * (1f - t)
                                alpha = t
                            }
                            .size(52.dp, 64.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Brush.verticalGradient(listOf(TileFace, TileFaceLow)))
                            .border(2.dp, TileEdge, RoundedCornerShape(7.dp)),
                    ) {
                        FaceArt(faceOf(tile), Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 5.dp))
                    }
                } else if (k in clearingSlots && clearing.isNotEmpty() && clearT < 1f) {
                    // The matched pair: pop and fade.
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = 1f + 0.5f * clearT
                                scaleY = 1f + 0.5f * clearT
                                alpha = 1f - clearT
                            }
                            .size(52.dp, 64.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFFFF4B8))
                            .border(2.dp, Color(0xFFE09B00), RoundedCornerShape(7.dp)),
                    ) {
                        FaceArt(clearing.first(), Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 5.dp))
                    }
                }
            }
        }
    }
}

/**
 * One tile, like the sample: ivory face, thick green edge underneath (bottom and right),
 * and a soft shadow that grows with the layer, so stacked tiles read clearly.
 * Tiles you can't pick yet are shaded.
 */
@Composable
private fun MahjongTile(
    face: Int,
    width: Dp,
    height: Dp,
    depth: Dp,
    layer: Int,
    free: Boolean,
    selected: Boolean,
    glow: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(modifier.size(width + depth, height + depth)) {
        // Green edge (the tile's thickness), with a shadow beneath it.
        Box(
            Modifier
                .offset(depth, depth)
                .size(width, height)
                .shadow((3 + layer * 3).dp, shape)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(TileEdge, TileEdgeDark))),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width, height)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        if (selected) listOf(Color(0xFFFFF4B8), Color(0xFFFFD84D)) else listOf(TileFace, TileFaceLow),
                    ),
                )
                .border(
                    width = if (selected || glow > 0f) 3.dp else 1.dp,
                    color = when {
                        selected -> Color(0xFFE09B00)
                        glow > 0f -> Color(0xFF22C55E).copy(alpha = glow)
                        else -> Color(0xFFD8D0BC)
                    },
                    shape = shape,
                ),
        ) {
            FaceArt(face, Modifier.fillMaxSize().padding(horizontal = width * 0.09f, vertical = height * 0.07f))
            if (!free) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1B3A2F).copy(alpha = 0.28f)),
                )
            }
        }
    }
}

/** Draws one classic face, scaled to fit [modifier]'s size. */
@Composable
private fun FaceArt(kind: Int, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier) { drawMahjongFace(kind, measurer) }
}

private val InkBlue = Color(0xFF1F5FBF)
private val InkGreen = Color(0xFF2E8B3D)
private val InkRed = Color(0xFFC8323C)
private val DotInks = listOf(InkBlue, InkGreen, InkRed)

/** Where the circles sit on tiles 1–9 (fractions of the face). */
private val DotSpots: List<List<Pair<Float, Float>>> = listOf(
    listOf(0.5f to 0.5f),
    listOf(0.5f to 0.27f, 0.5f to 0.73f),
    listOf(0.25f to 0.22f, 0.5f to 0.5f, 0.75f to 0.78f),
    listOf(0.3f to 0.27f, 0.7f to 0.27f, 0.3f to 0.73f, 0.7f to 0.73f),
    listOf(0.28f to 0.22f, 0.72f to 0.22f, 0.5f to 0.5f, 0.28f to 0.78f, 0.72f to 0.78f),
    listOf(0.3f to 0.2f, 0.7f to 0.2f, 0.3f to 0.5f, 0.7f to 0.5f, 0.3f to 0.8f, 0.7f to 0.8f),
    listOf(0.22f to 0.14f, 0.5f to 0.22f, 0.78f to 0.3f, 0.3f to 0.6f, 0.7f to 0.6f, 0.3f to 0.86f, 0.7f to 0.86f),
    listOf(0.3f to 0.14f, 0.7f to 0.14f, 0.3f to 0.38f, 0.7f to 0.38f, 0.3f to 0.62f, 0.7f to 0.62f, 0.3f to 0.86f, 0.7f to 0.86f),
    listOf(0.22f to 0.18f, 0.5f to 0.18f, 0.78f to 0.18f, 0.22f to 0.5f, 0.5f to 0.5f, 0.78f to 0.5f, 0.22f to 0.82f, 0.5f to 0.82f, 0.78f to 0.82f),
)

/** Where the sticks sit on bamboo 1–9. */
private val StickSpots: List<List<Pair<Float, Float>>> = listOf(
    listOf(0.5f to 0.5f),
    listOf(0.5f to 0.28f, 0.5f to 0.72f),
    listOf(0.5f to 0.28f, 0.3f to 0.72f, 0.7f to 0.72f),
    listOf(0.3f to 0.28f, 0.7f to 0.28f, 0.3f to 0.72f, 0.7f to 0.72f),
    listOf(0.25f to 0.28f, 0.75f to 0.28f, 0.5f to 0.5f, 0.25f to 0.72f, 0.75f to 0.72f),
    listOf(0.22f to 0.28f, 0.5f to 0.28f, 0.78f to 0.28f, 0.22f to 0.72f, 0.5f to 0.72f, 0.78f to 0.72f),
    listOf(0.5f to 0.17f, 0.22f to 0.5f, 0.5f to 0.5f, 0.78f to 0.5f, 0.22f to 0.83f, 0.5f to 0.83f, 0.78f to 0.83f),
    listOf(0.2f to 0.28f, 0.4f to 0.28f, 0.6f to 0.28f, 0.8f to 0.28f, 0.2f to 0.72f, 0.4f to 0.72f, 0.6f to 0.72f, 0.8f to 0.72f),
    listOf(0.22f to 0.17f, 0.5f to 0.17f, 0.78f to 0.17f, 0.22f to 0.5f, 0.5f to 0.5f, 0.78f to 0.5f, 0.22f to 0.83f, 0.5f to 0.83f, 0.78f to 0.83f),
)

private fun DrawScope.drawMahjongFace(kind: Int, measurer: androidx.compose.ui.text.TextMeasurer) {
    val w = size.width
    val h = size.height
    val m = minOf(w, h)
    when {
        kind < 9 -> {
            val n = kind + 1
            val r = m * when { n == 1 -> 0.3f; n <= 4 -> 0.15f; n <= 6 -> 0.12f; else -> 0.1f }
            DotSpots[kind].forEachIndexed { i, (fx, fy) ->
                val c = Offset(fx * w, fy * h)
                val ink = if (n == 1) InkRed else DotInks[i % 3]
                drawCircle(ink, r, c)
                drawCircle(Color(0xFFFBF8EF), r * 0.62f, c)
                drawCircle(ink, r * 0.38f, c)
            }
        }
        kind < 18 -> {
            val n = kind - 8
            val sw = w * (if (n == 1) 0.2f else 0.11f)
            val sh = h * when { n == 1 -> 0.6f; n <= 2 -> 0.32f; n <= 6 -> 0.36f; else -> 0.26f }
            StickSpots[n - 1].forEachIndexed { i, (fx, fy) ->
                val red = n == 1 || (n == 5 && i == 2) || (n == 7 && i == 0) || (n == 9 && i % 3 == 1)
                val ink = if (red) InkRed else InkGreen
                val tl = Offset(fx * w - sw / 2f, fy * h - sh / 2f)
                drawRoundRect(ink, tl, Size(sw, sh), CornerRadius(sw / 2f))
                drawRect(Color.White.copy(alpha = 0.35f), tl + Offset(sw * 0.35f, sh * 0.06f), Size(sw * 0.3f, sh * 0.88f))
                drawRect(Color.Black.copy(alpha = 0.25f), Offset(tl.x, fy * h - sh * 0.03f), Size(sw, sh * 0.06f))
            }
        }
        else -> {
            val style = TextStyle(
                color = if (kind == 18) InkRed else InkGreen,
                fontSize = (h * 0.62f).toSp(),
                fontWeight = FontWeight.Bold,
            )
            val layout = measurer.measure(if (kind == 18) "中" else "發", style)
            drawText(layout, topLeft = Offset((w - layout.size.width) / 2f, (h - layout.size.height) / 2f))
        }
    }
}

/** A small tile for the games screen's card. */
@Composable
internal fun MahjongIconTile(kind: Int, width: Dp, height: Dp, modifier: Modifier = Modifier) =
    MahjongTile(face = kind, width = width, height = height, depth = 3.dp, layer = 0, free = true, selected = false, glow = 0f, modifier = modifier)
