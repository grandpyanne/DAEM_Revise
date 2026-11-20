package com.grandpyanne.daemrevise.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.BlocksGame
import com.grandpyanne.daemrevise.data.Piece
import com.grandpyanne.daemrevise.data.Progress
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

// crafted by Grandpyanne™

/** A Kapi Blocks theme: page, board frame, board well and empty-slot colour. Blocks keep their own colours. */
private class Skin(
    val name: String,
    val page: Color,
    val frame: Color,
    val well: Color,
    val line: Color,
    val wood: Boolean = false,
)

/** Themes rotate as the score climbs (a new one every [SKIN_EVERY] points), then loop round. */
private const val SKIN_EVERY = 600

/** Calm, grown-up themes: only the board and background change. */
private val Skins = listOf(
    Skin("Midnight", Color(0xFF0E1A30), Color(0xFF1B2A4C), Color(0xFF0F1C36), Color(0xFF1A2A4A)),
    Skin("Jade", Color(0xFF0B2420), Color(0xFF17463C), Color(0xFF0E2E28), Color(0xFF173F37)),
    Skin("Ember", Color(0xFF22140F), Color(0xFF4A2A1C), Color(0xFF2A1913), Color(0xFF3A241B)),
    Skin("Ocean", Color(0xFF0A1F2E), Color(0xFF15405A), Color(0xFF0C2A3D), Color(0xFF133950)),
    Skin("Walnut", Color(0xFF2A1A10), Color(0xFF6B4024), Color(0xFF3A2416), Color(0xFF4A2F1D), wood = true),
    Skin("Dusk", Color(0xFF1C1430), Color(0xFF3A2B5C), Color(0xFF231A3D), Color(0xFF30254F)),
)

/** Every shape keeps its own colour, from the same family as Kapi Jewels. */
private val BlockColors = listOf(
    Color(0xFF2F6BFF), Color(0xFF19B36B), Color(0xFFE5243F),
    Color(0xFFF2B320), Color(0xFF9B4DFF), Color(0xFF16B5C8), Color(0xFFFF7A45),
)

private val Gold = Color(0xFFFFC94D)

private fun skinIndexFor(score: Int) = (score / SKIN_EVERY) % Skins.size

private fun praise(lines: Int): String? = when {
    lines >= 4 -> "Brilliant!"
    lines == 3 -> "Excellent!"
    lines == 2 -> "Great!"
    else -> null
}

@Composable
internal fun BlocksPlay(round: Int, progress: Progress, onGameOver: (Int) -> Unit) {
    val game = remember(round) { BlocksGame() }
    // Bumped after every move so the board redraws (the game itself is plain Kotlin).
    var tick by remember(round) { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val lift = with(density) { 70.dp.toPx() }

    var gridOrigin by remember { mutableStateOf(Offset.Zero) }
    var cellPx by remember { mutableStateOf(0f) }
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }
    val slotOrigins = remember { Array(3) { mutableStateOf(Offset.Zero) } }
    var dragging by remember(round) { mutableStateOf<Int?>(null) }
    var pointer by remember { mutableStateOf(Offset.Zero) }

    // Theme follows the score; colours fade into the next one.
    var skinIndex by remember(round) { mutableIntStateOf(0) }
    var themeShifts by remember(round) { mutableIntStateOf(0) }
    // No shape fits: offer the one rescue (new shapes) before ending.
    var stuck by remember(round) { mutableStateOf(false) }
    val skin = Skins[skinIndex]
    val page by animateColorAsState(skin.page, tween(1200), label = "page")
    val frame by animateColorAsState(skin.frame, tween(1200), label = "frame")
    val well by animateColorAsState(skin.well, tween(1200), label = "well")
    val line by animateColorAsState(skin.line, tween(1200), label = "line")
    val best = progress.bestGameScore("blocks")

    // Effects: clear flash + particles, landing bounce, floating cheer.
    var clearing by remember(round) { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var clearColors by remember(round) { mutableStateOf<List<Color>>(emptyList()) }
    val clearAnim = remember { Animatable(1f) }
    var landed by remember(round) { mutableStateOf<List<Int>>(emptyList()) }
    val landAnim = remember { Animatable(1f) }
    var cheer by remember(round) { mutableStateOf<String?>(null) }
    var cheerTick by remember(round) { mutableIntStateOf(0) }
    LaunchedEffect(tick) {
        if (tick == 0) return@LaunchedEffect
        landAnim.snapTo(0f)
        landAnim.animateTo(1f, tween(260))
    }
    LaunchedEffect(tick) {
        if (clearing.isNotEmpty()) {
            clearAnim.snapTo(0f)
            clearAnim.animateTo(1f, tween(560))
            clearing = emptyList()
        }
    }
    val shownScore by animateIntAsState(game.score.also { tick }, tween(450), label = "score")

    /** Top-left of the dragged piece (root coordinates): above the finger so it stays visible. */
    fun pieceTopLeft(p: Piece) = pointer - Offset(p.cols * cellPx / 2f, p.rows * cellPx + lift)

    /** The board cell the dragged piece would land on, if it fits there. */
    fun target(p: Piece): Pair<Int, Int>? {
        if (cellPx <= 0f) return null
        val tl = pieceTopLeft(p) - gridOrigin
        val r = (tl.y / cellPx).roundToInt()
        val c = (tl.x / cellPx).roundToInt()
        return if (game.fits(p, r, c)) r to c else null
    }

    fun colorOf(v: Int) = BlockColors[(v - 1).mod(BlockColors.size)]

    Box(
        Modifier
            .fillMaxSize()
            .background(page)
            .onGloballyPositioned { layerOrigin = it.positionInRoot() },
    ) {
        if (skin.wood) {
            Canvas(Modifier.fillMaxSize()) {
                val rnd = kotlin.random.Random(11)
                repeat(40) {
                    val y = rnd.nextFloat() * size.height
                    drawLine(Color.Black.copy(alpha = 0.05f + rnd.nextFloat() * 0.05f), Offset(0f, y), Offset(size.width, y + rnd.nextFloat() * 30f - 15f), strokeWidth = 1f + rnd.nextFloat() * 3f)
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            HowTo(
                progress, "blocks",
                listOf(
                    "• Drag a shape from the bottom onto the board.",
                    "• Fill a whole row or column to clear it. Clear several at once for big points.",
                    "• Clear on back-to-back moves for a streak bonus. Empty the whole board for a Perfect clear: +${BlocksGame.PERFECT_BONUS}.",
                    "• The board's theme changes every $SKIN_EVERY points, and after great moves (a perfect clear, 3+ lines at once, long streaks).",
                    "• Stuck? Once a game you can swap your shapes for new ones.",
                    "• Shapes can't be turned. When none of them fit, the game ends.",
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("♛", fontSize = 22.sp, color = Gold)
                Spacer(Modifier.size(6.dp))
                Text(
                    "${maxOf(best, game.score.also { tick })}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    skin.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC9D3EA),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
            Text(
                "$shownScore",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(Color(0x66000000), Offset(0f, 4f), 10f),
                ),
            )
            Text(
                if (game.streak > 1) "Streak ×${game.streak}" else " ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Gold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (game.streak > 1) Gold.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            )
            Spacer(Modifier.weight(1f))

            val held = dragging?.let { game.hand[it] }
            val spot = held?.let { target(it) }
            val lines = if (held != null && spot != null) game.linesIfPlaced(held, spot.first, spot.second) else null
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(frame)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(well)
                        .padding(6.dp)
                        .onGloballyPositioned {
                            gridOrigin = it.positionInRoot()
                            cellPx = it.size.width / 8f
                        },
                ) {
                    tick
                    val cell = size.width / 8f
                    val land = landAnim.value
                    // Empty slots.
                    for (r in 0 until 8) for (c in 0 until 8) {
                        drawRoundRect(line, Offset(c * cell + cell * 0.05f, r * cell + cell * 0.05f), Size(cell * 0.9f, cell * 0.9f), CornerRadius(cell * 0.16f))
                    }
                    // Rows/columns about to clear glow gold.
                    if (lines != null) {
                        lines.first.forEach { r -> drawRect(Gold.copy(alpha = 0.16f), Offset(0f, r * cell), Size(size.width, cell)) }
                        lines.second.forEach { c -> drawRect(Gold.copy(alpha = 0.16f), Offset(c * cell, 0f), Size(cell, size.height)) }
                    }
                    for (r in 0 until 8) for (c in 0 until 8) {
                        val v = game.cell(r, c)
                        if (v == 0) continue
                        val i = r * 8 + c
                        val lit = lines != null && (r in lines.first || c in lines.second)
                        val bounce = if (i in landed && land < 1f) 1f + 0.12f * sin(land * PI.toFloat()) else 1f
                        drawGlossBlock(if (lit) lerp(colorOf(v), Color.White, 0.3f) else colorOf(v), Offset(c * cell, r * cell), cell, bounce)
                    }
                    // Ghost of where the held shape will land.
                    if (held != null && spot != null) {
                        held.cells.forEach { (dr, dc) ->
                            drawGlossBlock(colorOf(held.color).copy(alpha = 0.4f), Offset((spot.second + dc) * cell, (spot.first + dr) * cell), cell, 1f)
                        }
                    }
                    // Cleared cells flash white, shrink away and burst into bits.
                    val t = clearAnim.value
                    if (clearing.isNotEmpty() && t < 1f) {
                        clearing.forEachIndexed { k, (i, _) ->
                            val base = clearColors.getOrElse(k) { Color.White }
                            val at = Offset((i % 8) * cell, (i / 8) * cell)
                            val shrink = (1f - t * 1.6f).coerceAtLeast(0f)
                            if (shrink > 0f) {
                                drawGlossBlock(lerp(Color.White, base, (t * 3f).coerceAtMost(1f)), at, cell, shrink)
                            }
                            drawBurst(at + Offset(cell / 2, cell / 2), base, cell, t, seed = i * 31 + tick)
                        }
                    }
                }
                FloatingCheer(cheer, cheerTick)
                if (stuck) {
                    RescuePanel(
                        title = "No room for these shapes",
                        offer = "🔄 New shapes (once)",
                        onRescue = {
                            game.newHand()
                            stuck = false
                            tick++
                            if (game.isOver) onGameOver(game.score)
                        },
                        onEnd = {
                            stuck = false
                            onGameOver(game.score)
                        },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
            ) {
                for (i in 0 until 3) {
                    val p = game.hand[i]
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .onGloballyPositioned { slotOrigins[i].value = it.positionInRoot() }
                            .pointerInput(round, i, tick) {
                                if (p == null) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { at ->
                                        dragging = i
                                        pointer = slotOrigins[i].value + at
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        pointer += amount
                                    },
                                    onDragEnd = {
                                        val spotNow = target(p)
                                        dragging = null
                                        if (spotNow != null) {
                                            val cleared = game.place(i, spotNow.first, spotNow.second)
                                            if (cleared >= 0) {
                                                landed = game.lastPlaced
                                                if (cleared > 0) {
                                                    clearColors = game.lastCleared.map { colorOf(it.second) }
                                                    clearing = game.lastCleared
                                                    cheer = when {
                                                        game.lastPerfect -> "Perfect clear"
                                                        else -> praise(cleared) ?: if (game.streak > 1) "Streak ×${game.streak}" else "Nice"
                                                    }
                                                    cheerTick++
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                } else {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                // Great moves earn a new look too: a perfect clear, 3+ lines at once, or every 4th streak.
                                                if (game.lastPerfect || cleared >= 3 || (game.streak > 0 && game.streak % 4 == 0)) themeShifts++
                                                val newSkin = (skinIndexFor(game.score) + themeShifts) % Skins.size
                                                if (newSkin != skinIndex) {
                                                    skinIndex = newSkin
                                                    cheer = "${Skins[newSkin].name} theme"
                                                    cheerTick++
                                                }
                                                tick++
                                                if (game.isOver) {
                                                    if (game.rescueLeft) stuck = true else onGameOver(game.score)
                                                }
                                            }
                                        }
                                    },
                                    onDragCancel = { dragging = null },
                                )
                            },
                    ) {
                        if (p != null && dragging != i) {
                            PieceView(p, colorOf(p.color), cellDp = 22f, alpha = if (game.fitsAnywhere(p)) 1f else 0.3f)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Drag a shape onto the board · fill a row or column to clear it",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8592B3),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        // The piece under your finger, drawn at board size and slightly lifted.
        val held = dragging?.let { game.hand[it] }
        if (held != null && cellPx > 0f) {
            val tl = pieceTopLeft(held) - layerOrigin
            val cellDp = with(density) { cellPx.toDp().value }
            Box(
                Modifier
                    .offset { IntOffset(tl.x.roundToInt(), tl.y.roundToInt()) }
                    .graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    },
            ) {
                PieceView(held, colorOf(held.color), cellDp = cellDp, alpha = 1f)
            }
        }
    }
}

@Composable
private fun PieceView(p: Piece, color: Color, cellDp: Float, alpha: Float) {
    Canvas(Modifier.size((p.cols * cellDp).dp, (p.rows * cellDp).dp)) {
        val cell = size.width / p.cols
        p.cells.forEach { (r, c) -> drawGlossBlock(color.copy(alpha = alpha), Offset(c * cell, r * cell), cell, 1f) }
    }
}

/** A rounded, glossy block: lit from above, with a soft shine across the top and a crisp edge. */
private fun DrawScope.drawGlossBlock(color: Color, at: Offset, cell: Float, zoom: Float) {
    val a = color.alpha
    val s = cell * zoom
    val inset = s * 0.06f
    val o = at + Offset((cell - s) / 2f + inset, (cell - s) / 2f + inset)
    val w = s - 2 * inset
    val radius = CornerRadius(s * 0.16f)
    // Shadow just below.
    drawRoundRect(Color.Black.copy(alpha = 0.35f * a), o + Offset(0f, s * 0.05f), Size(w, w), radius)
    // Body.
    drawRoundRect(
        Brush.verticalGradient(
            0f to lerp(color, Color.White, 0.28f).copy(alpha = a),
            0.5f to color.copy(alpha = a),
            1f to lerp(color, Color.Black, 0.3f).copy(alpha = a),
            startY = o.y, endY = o.y + w,
        ),
        o, Size(w, w), radius,
    )
    // Shine across the top.
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f * a), Color.White.copy(alpha = 0.04f * a)), startY = o.y, endY = o.y + s * 0.35f),
        o + Offset(s * 0.08f, s * 0.07f), Size(w - s * 0.16f, s * 0.26f), CornerRadius(s * 0.11f),
    )
    drawRoundRect(lerp(color, Color.Black, 0.45f).copy(alpha = a), o, Size(w, w), radius, style = Stroke(width = maxOf(1.5f, s * 0.02f)))
}

/** Block colours and drawing for the games screen's card. */
internal val BlockIconColors: List<Color> get() = BlockColors
internal fun DrawScope.drawBlockIcon(color: Color, at: Offset, cell: Float) = drawGlossBlock(color, at, cell, 1f)
