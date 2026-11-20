package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Progress
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

// crafted by Grandpyanne™
// Blueprint-style line drawings for topics, drawn faintly behind the topic's screens
// (notes, flashcards, quiz). Only shown while Progress.DEV_TOPIC_ART is on.

/** Topics with a hand-coded drawing below; every other topic uses its SKETCHES entry. */
private val TOPIC_ART: Set<String> = setOf("mt-6", "mt-7", "mt-10", "ec-6", "tf-2", "ec-3", "mm-6", "as-1")

/** True if [topicId] has a drawing and the switch is on. */
fun hasTopicArt(topicId: String?): Boolean = Progress.DEV_TOPIC_ART && topicId != null &&
    (topicId in TOPIC_ART || topicId in SKETCHES)

/**
 * Draws the topic's blueprint sketch faintly behind this element, in the bottom-right corner
 * (where quiz and flashcard screens usually have space). Nothing when off or no drawing yet.
 */
@Composable
fun Modifier.topicArt(topicId: String?): Modifier {
    if (!hasTopicArt(topicId)) return this
    val ink = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    val measurer = rememberTextMeasurer()
    return this.drawBehind {
        val box = minOf(size.width * 0.6f, 240.dp.toPx())
        translate(size.width - box - 8.dp.toPx(), size.height - box - 24.dp.toPx()) {
            drawTopicArt(topicId!!, ink, box, measurer)
        }
    }
}

/** A blueprint strip for the top of a topic's page: grid paper with the topic's sketch. */
@Composable
fun BlueprintBanner(topicId: String) {
    if (!hasTopicArt(topicId)) return
    val ink = MaterialTheme.colorScheme.primary
    val measurer = rememberTextMeasurer()
    androidx.compose.foundation.Canvas(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ink.copy(alpha = 0.05f))
            .border(1.dp, ink.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
    ) {
        // Grid paper.
        val step = 12.dp.toPx()
        var x = step
        while (x < size.width) {
            drawLine(ink.copy(alpha = 0.06f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = step
        while (y < size.height) {
            drawLine(ink.copy(alpha = 0.06f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
        val box = size.height * 0.92f
        translate((size.width - box) / 2f, (size.height - box) / 2f) {
            drawTopicArt(topicId, ink.copy(alpha = 0.55f), box, measurer)
        }
    }
}

/** Draws [topicId]'s sketch in a [box]×[box] square (its own coordinates run 0..200). */
private fun DrawScope.drawTopicArt(topicId: String, ink: Color, box: Float, measurer: TextMeasurer?) {
    val k = box / 200f
    fun p(x: Float, y: Float) = Offset(x * k, y * k)
    val line = Stroke(width = 2.2f * k, cap = StrokeCap.Round)
    val thin = Stroke(width = 1.2f * k, cap = StrokeCap.Round)
    val dashed = Stroke(width = 1.2f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * k, 5f * k)))
    fun l(x1: Float, y1: Float, x2: Float, y2: Float, w: Float = 2.2f) =
        drawLine(ink, p(x1, y1), p(x2, y2), strokeWidth = w * k, cap = StrokeCap.Round)
    fun arrow(x1: Float, y1: Float, x2: Float, y2: Float) {
        l(x1, y1, x2, y2, 1.4f)
        val a = kotlin.math.atan2(y2 - y1, x2 - x1)
        l(x2, y2, x2 - 7f * cos(a - 0.45f), y2 - 7f * sin(a - 0.45f), 1.4f)
        l(x2, y2, x2 - 7f * cos(a + 0.45f), y2 - 7f * sin(a + 0.45f), 1.4f)
    }
    fun dim(x1: Float, y1: Float, x2: Float, y2: Float) {
        // A dimension line with arrows at both ends.
        arrow((x1 + x2) / 2, (y1 + y2) / 2, x1, y1)
        arrow((x1 + x2) / 2, (y1 + y2) / 2, x2, y2)
    }
    fun path(block: Path.() -> Unit) = Path().apply(block)

    when (topicId) {
        // Springs: a helical coil spring between two plates, with pitch and load.
        "mt-6" -> {
            l(60f, 20f, 140f, 20f, 3f)
            l(60f, 180f, 140f, 180f, 3f)
            val coil = path {
                moveTo(100f * k, 20f * k)
                var y = 30f
                var right = true
                while (y < 170f) {
                    quadraticBezierTo((if (right) 140f else 60f) * k, (y + 5f) * k, 100f * k, (y + 10f) * k)
                    y += 10f
                    right = !right
                }
                lineTo(100f * k, 180f * k)
            }
            drawPath(coil, ink, style = line)
            arrow(100f, 0f, 100f, 16f)
            l(150f, 50f, 165f, 50f, 1f); l(150f, 70f, 165f, 70f, 1f)
            dim(160f, 50f, 160f, 70f)
            dim(60f, 192f, 140f, 192f)
        }
        // Beams: a simply supported beam, a point load, a UDL, and the bending-moment curve below.
        "mt-7" -> {
            drawRect(ink, p(15f, 70f), Size(170f * k, 12f * k), style = line)
            drawPath(path { moveTo(25f * k, 82f * k); lineTo(15f * k, 98f * k); lineTo(35f * k, 98f * k); close() }, ink, style = line)
            drawPath(path { moveTo(175f * k, 82f * k); lineTo(165f * k, 98f * k); lineTo(185f * k, 98f * k); close() }, ink, style = line)
            drawCircle(ink, 3f * k, p(170f, 101f), style = thin); drawCircle(ink, 3f * k, p(180f, 101f), style = thin)
            arrow(70f, 30f, 70f, 68f)
            for (x in 110..170 step 12) arrow(x.toFloat(), 50f, x.toFloat(), 68f)
            l(110f, 50f, 170f, 50f, 1.4f)
            l(15f, 150f, 185f, 150f, 1f)
            val bmd = path {
                moveTo(25f * k, 150f * k)
                cubicTo(60f * k, 120f * k, 110f * k, 112f * k, 130f * k, 118f * k)
                cubicTo(150f * k, 124f * k, 168f * k, 140f * k, 175f * k, 150f * k)
            }
            drawPath(bmd, ink, style = dashed)
            l(70f, 150f, 70f, 124f, 1f)
        }
        // Gear trains: a driver and a driven gear meshing, with pitch circles.
        "mt-10" -> {
            fun gear(cx: Float, cy: Float, r: Float, teeth: Int, phase: Float) {
                val g = path {
                    val n = teeth * 4
                    for (i in 0..n) {
                        val a = phase + i * 2 * PI.toFloat() / n
                        val rr = if ((i / 2) % 2 == 0) r + 7f else r - 2f
                        val x = (cx + cos(a) * rr) * k
                        val y = (cy + sin(a) * rr) * k
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(g, ink, style = line)
                drawCircle(ink, r * 0.82f * k, p(cx, cy), style = dashed)
                drawCircle(ink, 7f * k, p(cx, cy), style = line)
                l(cx - 14f, cy, cx + 14f, cy, 1f); l(cx, cy - 14f, cx, cy + 14f, 1f)
            }
            gear(68f, 92f, 48f, 12, 0f)
            gear(146f, 92f, 28f, 7, 0.22f)
            drawArc(ink, -60f, 120f, false, p(30f, 54f), Size(76f * k, 76f * k), style = thin)
            arrow(98f, 60f, 104f, 72f)
        }
        // Logic gates: an AND gate and an OR gate feeding a NOT gate.
        "ec-6" -> {
            drawPath(path {
                moveTo(30f * k, 30f * k); lineTo(60f * k, 30f * k)
                arcTo(Rect(p(35f, 30f), Size(50f * k, 50f * k)), -90f, 180f, false)
                lineTo(30f * k, 80f * k); close()
            }, ink, style = line)
            l(10f, 42f, 30f, 42f); l(10f, 68f, 30f, 68f); l(85f, 55f, 110f, 55f)
            drawPath(path {
                moveTo(30f * k, 110f * k)
                quadraticBezierTo(45f * k, 135f * k, 30f * k, 160f * k)
                quadraticBezierTo(75f * k, 160f * k, 92f * k, 135f * k)
                quadraticBezierTo(75f * k, 110f * k, 30f * k, 110f * k)
                close()
            }, ink, style = line)
            l(10f, 122f, 37f, 122f); l(10f, 148f, 37f, 148f); l(92f, 135f, 110f, 135f)
            l(110f, 55f, 110f, 85f); l(110f, 135f, 110f, 105f)
            drawPath(path { moveTo(125f * k, 75f * k); lineTo(125f * k, 115f * k); lineTo(160f * k, 95f * k); close() }, ink, style = line)
            drawCircle(ink, 5f * k, p(165f, 95f), style = line)
            l(110f, 85f, 125f, 85f); l(110f, 105f, 125f, 105f); l(170f, 95f, 192f, 95f)
        }
        // Flaps & stalling: an aerofoil with a deflected flap, airflow lines and the angle of attack.
        "tf-2" -> {
            val wing = path {
                moveTo(20f * k, 100f * k)
                cubicTo(30f * k, 70f * k, 90f * k, 66f * k, 140f * k, 86f * k)
                lineTo(140f * k, 100f * k)
                cubicTo(100f * k, 104f * k, 50f * k, 110f * k, 20f * k, 100f * k)
                close()
            }
            drawPath(wing, ink, style = line)
            drawPath(path {
                moveTo(144f * k, 90f * k); lineTo(182f * k, 118f * k); lineTo(178f * k, 124f * k); lineTo(144f * k, 102f * k); close()
            }, ink, style = line)
            for (i in 0 until 3) {
                val y = 50f - i * 16f
                drawPath(path {
                    moveTo(0f, (y + 40f) * k)
                    cubicTo(50f * k, (y + 10f) * k, 120f * k, (y + 8f) * k, 200f * k, (y + 40f + i * 6f) * k)
                }, ink, style = thin)
            }
            drawPath(path { moveTo(0f, 140f * k); cubicTo(60f * k, 128f * k, 130f * k, 130f * k, 200f * k, 160f * k) }, ink, style = thin)
            l(0f, 118f, 60f, 118f, 1f)
            drawArc(ink, 180f, 22f, false, p(-20f, 78f), Size(80f * k, 80f * k), style = thin)
        }
        // Rectifier: transformer, diode and load, with the half-wave output below.
        "ec-3" -> {
            for (i in 0 until 4) drawArc(ink, 90f, 180f, false, p(36f, 30f + i * 14f), Size(14f * k, 14f * k), style = line)
            for (i in 0 until 4) drawArc(ink, -90f, 180f, false, p(54f, 30f + i * 14f), Size(14f * k, 14f * k), style = line)
            l(52f, 26f, 52f, 90f, 1f); l(56f, 26f, 56f, 90f, 1f)
            l(61f, 30f, 100f, 30f)
            drawPath(path { moveTo(100f * k, 20f * k); lineTo(100f * k, 40f * k); lineTo(120f * k, 30f * k); close() }, ink, style = line)
            l(120f, 20f, 120f, 40f)
            l(120f, 30f, 170f, 30f); l(170f, 30f, 170f, 46f)
            drawRect(ink, p(163f, 46f), Size(14f * k, 30f * k), style = line)
            l(170f, 76f, 170f, 86f); l(61f, 86f, 170f, 86f)
            l(15f, 160f, 190f, 160f, 1f); l(20f, 115f, 20f, 175f, 1f)
            drawPath(path {
                moveTo(20f * k, 160f * k)
                var x = 20f
                while (x < 188f) {
                    val phase = ((x - 20f) / 56f) % 2f
                    val y = if (phase < 1f) 160f - 34f * sin(phase * PI.toFloat()) else 160f
                    lineTo(x * k, y * k)
                    x += 2f
                }
            }, ink, style = line)
        }
        // Laplace: f(t) on time axes, the transform arrow, then poles and a zero on the s-plane.
        "mm-6" -> {
            l(10f, 90f, 90f, 90f, 1.2f); l(15f, 20f, 15f, 95f, 1.2f)
            drawPath(path {
                moveTo(15f * k, 90f * k)
                var t = 0f
                while (t <= 70f) {
                    val y = 90f - 60f * exp(-t / 20f) * cos(t / 5f).let { (it + 1f) / 2f }
                    lineTo((15f + t) * k, y * k)
                    t += 1f
                }
            }, ink, style = line)
            arrow(95f, 60f, 125f, 60f)
            l(105f, 52f, 105f, 46f, 1f); l(105f, 46f, 112f, 46f, 1f)
            l(130f, 120f, 195f, 120f, 1.2f); l(165f, 80f, 165f, 180f, 1.2f)
            fun cross(x: Float, y: Float) { l(x - 5f, y - 5f, x + 5f, y + 5f); l(x - 5f, y + 5f, x + 5f, y - 5f) }
            cross(145f, 102f); cross(145f, 138f)
            drawCircle(ink, 5f * k, p(178f, 120f), style = line)
            drawArc(ink, 90f, 180f, false, p(125f, 100f), Size(40f * k, 40f * k), style = dashed)
        }
        // Hydraulics: reservoir, pump, pipes, and an actuating cylinder with its piston.
        "as-1" -> {
            drawRect(ink, p(15f, 120f), Size(50f * k, 40f * k), style = line)
            l(18f, 132f, 62f, 132f, 1f)
            drawCircle(ink, 16f * k, p(40f, 75f), style = line)
            drawPath(path { moveTo(34f * k, 85f * k); lineTo(46f * k, 85f * k); lineTo(40f * k, 63f * k); close() }, ink, style = line)
            l(40f, 91f, 40f, 120f)
            l(40f, 59f, 40f, 30f); l(40f, 30f, 120f, 30f); l(120f, 30f, 120f, 70f)
            drawRect(ink, p(90f, 70f), Size(90f * k, 40f * k), style = line)
            drawRect(ink, p(122f, 72f), Size(8f * k, 36f * k), style = line)
            l(130f, 90f, 196f, 90f, 3f)
            l(170f, 110f, 170f, 150f); l(170f, 150f, 65f, 150f)
            arrow(70f, 30f, 95f, 30f)
            arrow(150f, 150f, 120f, 150f)
            drawRect(ink, p(100f, 74f), Size(20f * k, 32f * k), style = dashed)
        }
        else -> SKETCHES[topicId]?.let { drawSketch(it, ink, ink, k, measurer) }
    }
}

/** Grid-paper background, [step] apart. */
private fun DrawScope.gridPaper(ink: Color, step: Float) {
    var x = step
    while (x < size.width) {
        drawLine(ink, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = step
    while (y < size.height) {
        drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

/**
 * A labelled diagram inside the notes: grid paper, the drawing, and a caption under it.
 * Always shown (diagrams are study material); only the topic sketches are developer-only.
 */
@Composable
fun NoteFigure(figureId: String) {
    val fig = FIGURES[figureId] ?: return
    val ink = MaterialTheme.colorScheme.primary
    val text = MaterialTheme.colorScheme.onSurface
    val measurer = rememberTextMeasurer()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ink.copy(alpha = 0.05f))
            .border(1.dp, ink.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.Canvas(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .aspectRatio(200f / fig.first),
        ) {
            gridPaper(ink.copy(alpha = 0.06f), 12.dp.toPx())
            drawSketch(fig.third, ink.copy(alpha = 0.85f), text, size.width / 200f, measurer)
        }
        Text(
            fig.second,
            style = MaterialTheme.typography.bodySmall,
            color = text.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Draws one sketch written in the drawing language of tools/sketches.py (commands split by ';',
 * coordinates on a 200-wide grid, scaled by [k]). Text needs a [measurer]; without one it is skipped.
 */
internal fun DrawScope.drawSketch(src: String, ink: Color, textInk: Color, k: Float, measurer: TextMeasurer?) {
    fun p(x: Float, y: Float) = Offset(x * k, y * k)
    fun stroke(w: Float, dashed: Boolean = false) = Stroke(
        width = w * k, cap = StrokeCap.Round, join = StrokeJoin.Round,
        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6f * k, 5f * k)) else null,
    )
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, w: Float, dashed: Boolean = false) =
        drawLine(
            ink, p(x1, y1), p(x2, y2), strokeWidth = w * k, cap = StrokeCap.Round,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6f * k, 5f * k)) else null,
        )
    fun head(x1: Float, y1: Float, x2: Float, y2: Float) {
        val a = kotlin.math.atan2(y2 - y1, x2 - x1)
        line(x2, y2, x2 - 7f * cos(a - 0.45f), y2 - 7f * sin(a - 0.45f), 1.4f)
        line(x2, y2, x2 - 7f * cos(a + 0.45f), y2 - 7f * sin(a + 0.45f), 1.4f)
    }
    for (raw in src.split(';')) {
        val cmd = raw.trim()
        if (cmd.isEmpty()) continue
        val op = cmd[0]
        if (op == 'T' || op == 't') {
            if (measurer == null) continue
            val parts = cmd.split(Regex("\\s+"), limit = 5)
            if (parts.size < 5) continue
            val x = parts[1].toFloat(); val y = parts[2].toFloat(); val sz = parts[3].toFloat()
            val layout = measurer.measure(
                parts[4],
                TextStyle(color = textInk, fontSize = (sz * k).toSp(), fontWeight = FontWeight.SemiBold),
            )
            val left = if (op == 'T') x * k - layout.size.width / 2f else x * k
            drawText(layout, topLeft = Offset(left, y * k - layout.size.height / 2f))
            continue
        }
        val n = cmd.substring(1).trim().split(Regex("\\s+")).mapNotNull { it.toFloatOrNull() }
        fun at(i: Int) = n.getOrElse(i) { 0f }
        when (op) {
            'L' -> line(at(0), at(1), at(2), at(3), 2.2f)
            'l' -> line(at(0), at(1), at(2), at(3), 1.2f)
            'W' -> line(at(0), at(1), at(2), at(3), 3.4f)
            '-' -> line(at(0), at(1), at(2), at(3), 1.2f, dashed = true)
            'A' -> { line(at(0), at(1), at(2), at(3), 1.4f); head(at(0), at(1), at(2), at(3)) }
            'D' -> {
                line(at(0), at(1), at(2), at(3), 1.2f)
                head(at(2), at(3), at(0), at(1)); head(at(0), at(1), at(2), at(3))
            }
            'R' -> drawRect(ink, p(at(0), at(1)), Size(at(2) * k, at(3) * k), style = stroke(2.2f))
            'r' -> drawRect(ink, p(at(0), at(1)), Size(at(2) * k, at(3) * k), style = stroke(1.2f, true))
            'O' -> drawCircle(ink, at(2) * k, p(at(0), at(1)), style = stroke(2.2f))
            'o' -> drawCircle(ink, at(2) * k, p(at(0), at(1)), style = stroke(1.2f, true))
            'F' -> drawCircle(ink, at(2) * k, p(at(0), at(1)))
            'E' -> drawOval(ink, p(at(0) - at(2), at(1) - at(3)), Size(2 * at(2) * k, 2 * at(3) * k), style = stroke(2.2f))
            'a' -> drawArc(
                ink, at(3), at(4), false, p(at(0) - at(2), at(1) - at(2)),
                Size(2 * at(2) * k, 2 * at(2) * k), style = stroke(2.2f),
            )
            'P', 'G', 'H' -> {
                if (n.size < 4) continue
                val path = Path().apply {
                    moveTo(at(0) * k, at(1) * k)
                    var i = 2
                    while (i + 1 < n.size) { lineTo(n[i] * k, n[i + 1] * k); i += 2 }
                    if (op != 'P') close()
                }
                if (op == 'H') drawPath(path, ink.copy(alpha = ink.alpha * 0.25f))
                else drawPath(path, ink, style = stroke(2.2f))
            }
            'B', 'b' -> {
                val path = Path().apply {
                    moveTo(at(0) * k, at(1) * k)
                    cubicTo(at(2) * k, at(3) * k, at(4) * k, at(5) * k, at(6) * k, at(7) * k)
                }
                drawPath(path, ink, style = if (op == 'B') stroke(2.2f) else stroke(1.2f, true))
            }
            'Z' -> {
                val x0 = at(0); val y0 = at(1); val dx = at(2) - x0; val dy = at(3) - y0
                val cnt = at(4).toInt()
                val len = kotlin.math.hypot(dx, dy).coerceAtLeast(0.001f)
                val px = -dy / len * 6f; val py = dx / len * 6f
                val path = Path().apply {
                    moveTo(x0 * k, y0 * k)
                    for (i in 1..cnt) {
                        val t = i.toFloat() / (cnt + 1)
                        val sgn = if (i % 2 == 1) 1f else -1f
                        lineTo((x0 + dx * t + px * sgn) * k, (y0 + dy * t + py * sgn) * k)
                    }
                    lineTo(at(2) * k, at(3) * k)
                }
                drawPath(path, ink, style = stroke(2.2f))
            }
            'K' -> {
                val x0 = at(0); val y = at(1); val cnt = at(3).toInt().coerceAtLeast(1)
                val w = (at(2) - x0) / cnt
                for (i in 0 until cnt) {
                    drawArc(
                        ink, 180f, 180f, false, p(x0 + i * w, y - w / 2f),
                        Size(w * k, w * k), style = stroke(2.2f),
                    )
                }
            }
            'g' -> {
                val x = at(0); val y = at(1)
                line(x - 10f, y, x + 10f, y, 1.8f)
                line(x - 6f, y + 4f, x + 6f, y + 4f, 1.8f)
                line(x - 2f, y + 8f, x + 2f, y + 8f, 1.8f)
            }
        }
    }
}
