package com.grandpyanne.daemrevise.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// crafted by Grandpyanne™
// Kapi's Park scenery: each area is a drawn scene. Everything starts grey; colour spreads out
// from every stop you've earned stars on (more stars, wider circle). A finished area is full colour.

/** A stop on the map, for colouring: where it is and how many stars it has. */
internal class StopLight(val at: Offset, val stars: Int)

/** Turns a colour grey; [sat] 0 = grey, 1 = full colour. */
private fun tint(c: Color, sat: Float): Color {
    val g = c.red * 0.3f + c.green * 0.59f + c.blue * 0.11f
    val grey = Color(g * 0.95f + 0.04f, g * 0.95f + 0.04f, g * 0.95f + 0.04f, c.alpha)
    return lerp(grey, c, sat.coerceIn(0f, 1f))
}

/** How coloured a point is: 1 inside a starred stop's circle, fading out at its edge. */
private fun satAt(p: Offset, stops: List<StopLight>, reach: Float, complete: Boolean): Float {
    if (complete) return 1f
    var best = 0f
    for (s in stops) {
        if (s.stars <= 0) continue
        val r = reach * (0.45f + 0.55f * s.stars / 3f)
        val d = hypot(p.x - s.at.x, p.y - s.at.y)
        val v = ((r - d) / (r * 0.45f) + 1f).coerceIn(0f, 1f)
        if (v > best) best = v
    }
    return best
}

/** The kinds of things that grow in each area. Repeats make an item more common. */
private val PROPS: Map<String, List<String>> = mapOf(
    "mechtech" to listOf("gear", "gear", "drum", "drum", "crate", "crate", "tree", "rock", "lamp", "bench", "bush"),
    "electronics" to listOf("bamboo", "bamboo", "bamboo", "bamboo", "pine", "mushroom", "firefly", "firefly", "lantern", "rock", "bush"),
    "maths2" to listOf("flower", "flower", "flower", "sunflower", "sunflower", "bush", "tree", "butterfly", "butterfly", "bench"),
    "propellers" to listOf("palm", "palm", "boat", "duck", "duck", "rock", "lamp", "shell", "bush"),
    "airfield" to listOf("cone", "cone", "hydrant", "barrier", "barrier", "tree", "lamp", "bench", "bush"),
    "airframe" to listOf("plane", "drum", "crate", "crate", "tyre", "tyre", "tree", "lamp", "bush"),
    "tof" to listOf("windsock", "cloud", "cloud", "tree", "bush", "bush", "flower", "plane", "balloon"),
)

/** Ground colours: grey to start, the area's colour where it has come alive. */
private val GROUND: Map<String, Color> = mapOf(
    "mechtech" to Color(0xFFCFA779),
    "electronics" to Color(0xFF7CC47F),
    "maths2" to Color(0xFFA6D86E),
    "propellers" to Color(0xFF9ED39A),
    "airfield" to Color(0xFF9FCF7E),
    "airframe" to Color(0xFF9DB3C9),
    "tof" to Color(0xFF8ED07E),
)

/**
 * Draws an area's scene behind its path: ground, colour splashes round starred stops, then
 * props placed away from the path, each one as colourful as its spot on the map.
 */
internal fun DrawScope.drawAreaScene(
    areaId: String,
    stops: List<StopLight>,
    pathX: (Float) -> Float,
    complete: Boolean,
    time: Float,
    unit: Float,
) {
    val ground = GROUND[areaId] ?: Color(0xFF8ED07E)
    val reach = unit * 150f
    // Base ground, grey.
    drawRect(tint(ground, if (complete) 1f else 0f))
    // Colour splashes round each starred stop.
    if (!complete) {
        stops.filter { it.stars > 0 }.forEach { s ->
            val r = reach * (0.45f + 0.55f * s.stars / 3f) * 1.3f
            drawCircle(
                Brush.radialGradient(
                    0f to ground, 0.6f to ground, 1f to ground.copy(alpha = 0f),
                    center = s.at,
                    radius = r,
                ),
                r,
                s.at,
            )
        }
    }
    // Water along the right edge at the pier.
    if (areaId == "propellers") {
        val path = Path()
        path.moveTo(size.width, 0f)
        var y = 0f
        while (y <= size.height) {
            path.lineTo(size.width * 0.86f + sin(y / (unit * 40f) + time * 6.28f) * unit * 8f, y)
            y += unit * 10f
        }
        path.lineTo(size.width, size.height)
        path.close()
        val sat = satAt(Offset(size.width * 0.9f, size.height / 2f), stops, reach * 3f, complete)
        drawPath(path, tint(Color(0xFF4FB3D9), sat))
    }
    // Runway stripes at the hill, tarmac at the hangar.
    if (areaId == "tof" || areaId == "airframe") {
        val x = size.width * 0.9f
        drawRect(tint(Color(0xFF5E6670), if (complete) 1f else 0.15f + 0.85f * satAt(Offset(x, size.height / 2f), stops, reach * 2f, complete)), Offset(x - unit * 18f, 0f), Size(unit * 36f, size.height))
        var y = unit * 10f
        while (y < size.height) {
            drawRect(Color.White.copy(alpha = 0.8f), Offset(x - unit * 2f, y), Size(unit * 4f, unit * 18f))
            y += unit * 34f
        }
    }

    // Little tufts of grass (or pebbles) all over, for texture.
    val tufts = kotlin.random.Random(areaId.hashCode() + 7)
    repeat((size.width * size.height / (unit * unit * 900f)).toInt().coerceIn(20, 400)) {
        val at = Offset(tufts.nextFloat() * size.width, tufts.nextFloat() * size.height)
        val sat = satAt(at, stops, reach, complete)
        val col = tint(if (areaId == "mechtech" || areaId == "airframe") Color(0xFFA9845A) else Color(0xFF4E9A4E), sat).copy(alpha = 0.5f)
        drawLine(col, at, at + Offset(-2f * unit, -5f * unit), strokeWidth = 1.4f * unit)
        drawLine(col, at, at + Offset(0f, -6f * unit), strokeWidth = 1.4f * unit)
        drawLine(col, at, at + Offset(2f * unit, -5f * unit), strokeWidth = 1.4f * unit)
    }

    // Props, placed at random (always the same for the area) but away from the path.
    val kinds = PROPS[areaId] ?: listOf("tree", "bush", "rock")
    val rnd = kotlin.random.Random(areaId.hashCode())
    val count = (size.height / (unit * 20f)).toInt().coerceIn(14, 110)
    repeat(count) {
        val y = rnd.nextFloat() * size.height
        val px = pathX(y)
        var x = rnd.nextFloat() * size.width
        val clear = unit * 70f
        if (kotlin.math.abs(x - px) < clear) x = if (px > size.width / 2f) (px - clear - rnd.nextFloat() * unit * 40f) else (px + clear + rnd.nextFloat() * unit * 40f)
        x = x.coerceIn(unit * 14f, size.width - unit * 14f)
        if (areaId == "propellers" && x > size.width * 0.82f) x = size.width * 0.12f + rnd.nextFloat() * size.width * 0.1f
        val kind = kinds[rnd.nextInt(kinds.size)]
        val s = unit * (1.15f + rnd.nextFloat() * 0.6f)
        val at = Offset(x, y)
        val sat = satAt(at, stops, reach, complete)
        drawProp(kind, at, s, sat, time, it)
    }
}

/** One prop. [s] is its scale (1 = normal), [sat] how colourful it is. */
private fun DrawScope.drawProp(kind: String, at: Offset, s: Float, sat: Float, time: Float, seed: Int) {
    fun c(hex: Long) = tint(Color(hex), sat)
    val shadow = Color(0x22000000)
    when (kind) {
        "tree" -> {
            drawOval(shadow, at + Offset(-16f * s, 14f * s), Size(32f * s, 8f * s))
            drawRect(c(0xFF8A5A2E), at + Offset(-3f * s, -4f * s), Size(6f * s, 18f * s))
            drawCircle(c(0xFF3F9A4A), 16f * s, at + Offset(0f, -14f * s))
            drawCircle(c(0xFF57B85F), 11f * s, at + Offset(-7f * s, -20f * s))
            drawCircle(c(0xFF4BAA54), 10f * s, at + Offset(8f * s, -18f * s))
        }
        "pine" -> {
            drawRect(c(0xFF7A4E28), at + Offset(-3f * s, 4f * s), Size(6f * s, 10f * s))
            for (k in 0 until 3) {
                val p = Path().apply {
                    val top = at.y - (28f - k * 9f) * s
                    moveTo(at.x, top)
                    lineTo(at.x + (12f + k * 3f) * s, top + 16f * s)
                    lineTo(at.x - (12f + k * 3f) * s, top + 16f * s)
                    close()
                }
                drawPath(p, c(if (k % 2 == 0) 0xFF2F7D46 else 0xFF3C8F52))
            }
        }
        "bamboo" -> {
            for (k in -1..1) {
                val x = at.x + k * 8f * s
                val h = (34f + (seed % 3) * 6f + k * 4f) * s
                drawRect(c(0xFF7DBF66), Offset(x - 2.5f * s, at.y - h), Size(5f * s, h + 10f * s))
                var y = at.y - h + 8f * s
                while (y < at.y + 8f * s) {
                    drawRect(c(0xFF4F8F42), Offset(x - 2.5f * s, y), Size(5f * s, 1.6f * s))
                    y += 10f * s
                }
                drawOval(c(0xFF5C9E4C), Offset(x, at.y - h - 2f * s), Size(12f * s, 4f * s))
                drawOval(c(0xFF5C9E4C), Offset(x - 12f * s, at.y - h + 6f * s), Size(12f * s, 4f * s))
            }
        }
        "bush" -> {
            drawCircle(c(0xFF4FA35E), 8f * s, at + Offset(-6f * s, 0f))
            drawCircle(c(0xFF5DB86B), 9f * s, at + Offset(3f * s, -3f * s))
            drawCircle(c(0xFF4FA35E), 7f * s, at + Offset(10f * s, 1f * s))
        }
        "rock" -> {
            drawOval(c(0xFF9A958E), at + Offset(-12f * s, -7f * s), Size(24f * s, 14f * s))
            drawOval(c(0xFFB5B0A8), at + Offset(-7f * s, -6f * s), Size(10f * s, 5f * s))
        }
        "flower" -> {
            val petal = listOf(0xFFFF7EB6, 0xFFFFD166, 0xFFB69CFF, 0xFFFF8A65)[seed % 4]
            drawLine(c(0xFF4F8F42), at, at + Offset(0f, 12f * s), strokeWidth = 2f * s)
            for (k in 0 until 5) {
                val a = k * 1.2566f
                drawCircle(c(petal), 4f * s, at + Offset(cos(a) * 4.5f * s, sin(a) * 4.5f * s))
            }
            drawCircle(c(0xFFFFE27A), 3f * s, at)
        }
        "sunflower" -> {
            drawLine(c(0xFF4F8F42), at, at + Offset(0f, 22f * s), strokeWidth = 3f * s)
            drawOval(c(0xFF5C9E4C), at + Offset(1f * s, 10f * s), Size(9f * s, 4f * s))
            for (k in 0 until 10) {
                val a = k * 0.628f
                drawCircle(c(0xFFFFC233), 4f * s, at + Offset(cos(a) * 8f * s, sin(a) * 8f * s))
            }
            drawCircle(c(0xFF7A4E28), 6f * s, at)
        }
        "butterfly" -> {
            val flap = 0.6f + 0.4f * sin(time * 6.28f * 12f + seed)
            val p = at + Offset(sin(time * 6.28f * 2f + seed) * 10f * s, cos(time * 6.28f * 3f + seed) * 6f * s)
            drawOval(c(0xFFB04DFF), p + Offset(-9f * s * flap, -6f * s), Size(9f * s * flap, 8f * s))
            drawOval(c(0xFFFF7EB6), p + Offset(0f, -6f * s), Size(9f * s * flap, 8f * s))
            drawRect(c(0xFF3B2A1E), p + Offset(-0.8f * s, -6f * s), Size(1.6f * s, 9f * s))
        }
        "mushroom" -> {
            drawRect(c(0xFFF4E9D8), at + Offset(-3f * s, -4f * s), Size(6f * s, 10f * s))
            drawArc(c(0xFFE0483C), 180f, 180f, true, at + Offset(-10f * s, -12f * s), Size(20f * s, 16f * s))
            drawCircle(Color.White.copy(alpha = sat * 0.9f + 0.1f), 1.8f * s, at + Offset(-4f * s, -8f * s))
            drawCircle(Color.White.copy(alpha = sat * 0.9f + 0.1f), 1.5f * s, at + Offset(3f * s, -9f * s))
        }
        "firefly" -> {
            val glow = (sin(time * 6.28f * 6f + seed * 1.7f) + 1f) / 2f
            val p = at + Offset(sin(time * 6.28f + seed) * 8f * s, cos(time * 6.28f * 2f + seed) * 6f * s)
            if (sat > 0.2f) drawCircle(Color(0xFFFFF27A).copy(alpha = 0.35f * glow * sat), 9f * s, p)
            drawCircle(c(0xFFFFF27A).copy(alpha = 0.5f + 0.5f * glow), 2.5f * s, p)
        }
        "lantern", "lamp" -> {
            drawRect(c(0xFF4A4A4A), at + Offset(-1.5f * s, -26f * s), Size(3f * s, 38f * s))
            drawRect(c(0xFF3A3A3A), at + Offset(-6f * s, -32f * s), Size(12f * s, 8f * s))
            val lit = sat > 0.5f
            drawRect(if (lit) Color(0xFFFFD27A) else c(0xFFBDBDBD), at + Offset(-4f * s, -30f * s), Size(8f * s, 5f * s))
            if (lit) drawCircle(Color(0xFFFFD27A).copy(alpha = 0.25f), 16f * s, at + Offset(0f, -28f * s))
        }
        "bench" -> {
            drawRect(c(0xFF8A5A2E), at + Offset(-14f * s, -6f * s), Size(28f * s, 4f * s))
            drawRect(c(0xFF8A5A2E), at + Offset(-14f * s, -12f * s), Size(28f * s, 3f * s))
            drawRect(c(0xFF4A4A4A), at + Offset(-12f * s, -2f * s), Size(3f * s, 8f * s))
            drawRect(c(0xFF4A4A4A), at + Offset(9f * s, -2f * s), Size(3f * s, 8f * s))
        }
        "gear" -> {
            val col = c(0xFF8E9AA6)
            val spin = time * 360f * (if (seed % 2 == 0) 1f else -1f)
            for (k in 0 until 8) {
                val a = (k * 45f + spin) * (PI.toFloat() / 180f)
                drawCircle(col, 3.5f * s, at + Offset(cos(a) * 11f * s, sin(a) * 11f * s))
            }
            drawCircle(col, 10f * s, at)
            drawCircle(c(0xFF5E6670), 3.5f * s, at)
        }
        "drum" -> {
            drawRoundRect(c(0xFF2D7DD2), at + Offset(-8f * s, -18f * s), Size(16f * s, 22f * s), CornerRadius(3f * s))
            drawRect(c(0xFF1F5C9E), at + Offset(-8f * s, -12f * s), Size(16f * s, 2f * s))
            drawRect(c(0xFF1F5C9E), at + Offset(-8f * s, -4f * s), Size(16f * s, 2f * s))
        }
        "crate" -> {
            drawRect(c(0xFFC98B4E), at + Offset(-10f * s, -16f * s), Size(20f * s, 18f * s))
            drawRect(c(0xFF8A5A2E), at + Offset(-10f * s, -16f * s), Size(20f * s, 18f * s), style = Stroke(2f * s))
            drawLine(c(0xFF8A5A2E), at + Offset(-10f * s, -16f * s), at + Offset(10f * s, 2f * s), strokeWidth = 2f * s)
        }
        "palm" -> {
            val trunk = Path().apply {
                moveTo(at.x - 2f * s, at.y + 8f * s)
                quadraticBezierTo(at.x + 6f * s, at.y - 14f * s, at.x + 2f * s, at.y - 34f * s)
                lineTo(at.x + 6f * s, at.y - 34f * s)
                quadraticBezierTo(at.x + 10f * s, at.y - 14f * s, at.x + 3f * s, at.y + 8f * s)
                close()
            }
            drawPath(trunk, c(0xFFA87B4E))
            for (k in 0 until 5) {
                val a = PI.toFloat() * (1.05f + k * 0.22f)
                drawOval(c(0xFF3F9A4A), at + Offset(4f * s + cos(a) * 12f * s - 9f * s, -34f * s + sin(a) * 6f * s - 3f * s), Size(18f * s, 6f * s))
            }
        }
        "boat" -> {
            val bob = sin(time * 6.28f * 4f + seed) * 2f * s
            val hull = Path().apply {
                moveTo(at.x - 14f * s, at.y - 4f * s + bob)
                lineTo(at.x + 14f * s, at.y - 4f * s + bob)
                lineTo(at.x + 9f * s, at.y + 4f * s + bob)
                lineTo(at.x - 9f * s, at.y + 4f * s + bob)
                close()
            }
            drawPath(hull, c(0xFFE0483C))
            drawLine(c(0xFF5A3A1A), at + Offset(0f, -4f * s + bob), at + Offset(0f, -24f * s + bob), strokeWidth = 1.6f * s)
            val sail = Path().apply {
                moveTo(at.x + 1f * s, at.y - 23f * s + bob)
                lineTo(at.x + 12f * s, at.y - 7f * s + bob)
                lineTo(at.x + 1f * s, at.y - 7f * s + bob)
                close()
            }
            drawPath(sail, c(0xFFFFFFFF))
        }
        "duck" -> {
            val bob = sin(time * 6.28f * 3f + seed) * 1.5f * s
            drawOval(c(0xFFFFD84D), at + Offset(-9f * s, -5f * s + bob), Size(18f * s, 10f * s))
            drawCircle(c(0xFFFFD84D), 5f * s, at + Offset(7f * s, -8f * s + bob))
            drawOval(c(0xFFFF8A00), at + Offset(11f * s, -9f * s + bob), Size(5f * s, 2.5f * s))
            drawCircle(c(0xFF3B2A1E), 1f * s, at + Offset(8f * s, -9f * s + bob))
        }
        "shell" -> {
            drawArc(c(0xFFFFB6A3), 180f, 180f, true, at + Offset(-7f * s, -6f * s), Size(14f * s, 12f * s))
            for (k in -1..1) drawLine(c(0xFFE58C8A), at, at + Offset(k * 5f * s, -5f * s), strokeWidth = 1f * s)
        }
        "cone" -> {
            val p = Path().apply {
                moveTo(at.x, at.y - 16f * s)
                lineTo(at.x + 7f * s, at.y + 2f * s)
                lineTo(at.x - 7f * s, at.y + 2f * s)
                close()
            }
            drawPath(p, c(0xFFFF7A1A))
            drawRect(c(0xFFFFFFFF), at + Offset(-4.5f * s, -7f * s), Size(9f * s, 3f * s))
            drawRect(c(0xFFFF7A1A), at + Offset(-9f * s, 2f * s), Size(18f * s, 3f * s))
        }
        "hydrant" -> {
            drawRoundRect(c(0xFFE0483C), at + Offset(-5f * s, -16f * s), Size(10f * s, 18f * s), CornerRadius(3f * s))
            drawCircle(c(0xFFE0483C), 5f * s, at + Offset(0f, -16f * s))
            drawRect(c(0xFFB8322A), at + Offset(-8f * s, -10f * s), Size(16f * s, 4f * s))
        }
        "barrier" -> {
            drawRect(c(0xFF4A4A4A), at + Offset(-14f * s, -6f * s), Size(3f * s, 12f * s))
            drawRect(c(0xFF4A4A4A), at + Offset(11f * s, -6f * s), Size(3f * s, 12f * s))
            for (k in 0 until 4) drawRect(c(if (k % 2 == 0) 0xFFE0483C else 0xFFFFFFFF), at + Offset(-14f * s + k * 7f * s, -12f * s), Size(7f * s, 6f * s))
        }
        "plane" -> {
            val col = c(0xFFE8EEF5)
            drawRoundRect(col, at + Offset(-16f * s, -4f * s), Size(32f * s, 8f * s), CornerRadius(4f * s))
            val wing = Path().apply {
                moveTo(at.x - 2f * s, at.y)
                lineTo(at.x + 6f * s, at.y)
                lineTo(at.x - 4f * s, at.y + 14f * s)
                lineTo(at.x - 9f * s, at.y + 14f * s)
                close()
            }
            drawPath(wing, c(0xFFB9C7D6))
            drawRect(c(0xFF2D7DD2), at + Offset(-16f * s, -4f * s), Size(32f * s, 2f * s))
            drawRect(c(0xFFB9C7D6), at + Offset(-16f * s, -11f * s), Size(5f * s, 8f * s))
        }
        "tyre" -> {
            drawCircle(c(0xFF2F2F2F), 9f * s, at)
            drawCircle(c(0xFF8E9AA6), 4f * s, at)
        }
        "windsock" -> {
            drawRect(c(0xFF6B6B6B), at + Offset(-1.5f * s, -30f * s), Size(3f * s, 40f * s))
            val sway = sin(time * 6.28f * 5f + seed) * 3f * s
            for (k in 0 until 4) {
                drawRect(
                    c(if (k % 2 == 0) 0xFFFF7A1A else 0xFFFFFFFF),
                    at + Offset(1.5f * s + k * 6f * s, -30f * s + k * 0.8f * s + sway * k / 3f),
                    Size(6f * s, 8f * s - k * 1.2f * s),
                )
            }
        }
        "cloud" -> {
            val drift = sin(time * 6.28f + seed) * 6f * s
            val col = Color.White.copy(alpha = 0.55f + 0.35f * sat)
            drawCircle(col, 8f * s, at + Offset(-8f * s + drift, 0f))
            drawCircle(col, 11f * s, at + Offset(2f * s + drift, -3f * s))
            drawCircle(col, 7f * s, at + Offset(12f * s + drift, 1f * s))
        }
        "balloon" -> {
            val rise = sin(time * 6.28f * 2f + seed) * 4f * s
            drawLine(c(0xFF6B6B6B), at + Offset(0f, 8f * s + rise), at + Offset(0f, 18f * s + rise), strokeWidth = 1f * s)
            drawOval(c(listOf(0xFFFF7EB6, 0xFF4FD8FF, 0xFFFFC233)[seed % 3]), at + Offset(-7f * s, -9f * s + rise), Size(14f * s, 17f * s))
        }
    }
}
