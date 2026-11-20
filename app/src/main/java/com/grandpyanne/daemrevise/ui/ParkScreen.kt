package com.grandpyanne.daemrevise.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grandpyanne.daemrevise.R
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.StudyUnit
import com.grandpyanne.daemrevise.data.Topic
import kotlin.math.sin

// crafted by Grandpyanne™

/** One area of the park: a unit's look, its keepsake and what grows there. */
private class Area(
    val name: String,
    val emoji: String,
    val ground: Color,
    val keepsake: String,
    val keepsakeName: String,
    /** What appears at 25%, 50%, 75% and 100%. */
    val blooms: List<List<String>>,
    val bloomNames: List<String>,
)

private val Areas: Map<String, Area> = mapOf(
    "mechtech" to Area(
        "Workshop Yard", "🔧", Color(0xFFC98B4E), "🔧", "Golden Wrench",
        listOf(listOf("🌱", "🪨"), listOf("🌳", "🛢️"), listOf("🧰", "🪑", "🏮"), listOf("🏭", "⚙️")),
        listOf("the first grass", "trees and oil drums", "toolboxes and lamps", "the Workshop itself"),
    ),
    "electronics" to Area(
        "Circuit Forest", "🌳", Color(0xFF4FA35E), "💡", "Glow Bulb",
        listOf(listOf("🌱", "🍄"), listOf("🌲", "🌳"), listOf("🦉", "🏮", "🍄"), listOf("🗼", "✨")),
        listOf("the first shoots", "tall trees", "owls and lanterns", "the Signal Tower"),
    ),
    "maths2" to Area(
        "Maths Meadow", "🌼", Color(0xFF9CC75A), "📐", "Lucky Set Square",
        listOf(listOf("🌱", "🌾"), listOf("🌼", "🌸"), listOf("🦋", "🐝", "🪑"), listOf("🌻", "🌈")),
        listOf("fresh grass", "flowers", "butterflies and bees", "the Sunflower Ring"),
    ),
    "propellers" to Area(
        "Propeller Pier", "⚓", Color(0xFF4AA3C7), "🧢", "Propeller Cap",
        listOf(listOf("🌊", "🪨"), listOf("🐚", "🌴"), listOf("🦆", "🛶", "🏮"), listOf("⛵", "🌅")),
        listOf("the tide", "shells and palms", "ducks and boats", "the Sailing Boat"),
    ),
    "airfield" to Area(
        "Safety Station", "🚧", Color(0xFFE0A13A), "🦺", "Safety Vest",
        listOf(listOf("🌱", "🚧"), listOf("🌳", "🧯"), listOf("🚦", "🪑", "🏮"), listOf("🚒", "🛟")),
        listOf("the first grass", "trees and extinguishers", "signals and benches", "the Fire Engine"),
    ),
    "airframe" to Area(
        "The Hangar", "🛩️", Color(0xFF7B93B8), "🧣", "Pilot's Scarf",
        listOf(listOf("🌱", "🪨"), listOf("🌳", "🛞"), listOf("🧰", "🪑", "🏮"), listOf("🛩️", "🏗️")),
        listOf("the first grass", "trees and tyres", "toolboxes and lamps", "a plane in the Hangar"),
    ),
    "tof" to Area(
        "Runway Hill", "⛰️", Color(0xFF6CB4E8), "🕶️", "Aviator Shades",
        listOf(listOf("🌱", "🪁"), listOf("🌳", "🎏"), listOf("🎈", "🪑", "🏮"), listOf("✈️", "☁️")),
        listOf("the first grass", "trees and windsocks", "balloons and benches", "a plane taking off"),
    ),
    "business" to Area(
        "Market Square", "🏪", Color(0xFFB07CD8), "💼", "Lucky Briefcase",
        listOf(listOf("🌱", "📦"), listOf("🌳", "🛒"), listOf("🍉", "🪑", "🏮"), listOf("🎪", "🎉")),
        listOf("the first grass", "trees and carts", "fruit stalls and lamps", "the Carnival Tent"),
    ),
)

/** Units that are part of the park adventure (Business Plan sits this one out). */
internal fun parkUnits(units: List<StudyUnit>): List<StudyUnit> = units.filter { it.id != "business" }

private fun areaFor(unit: StudyUnit): Area =
    Areas[unit.id] ?: Area(unit.shortName, "🌳", Color(0xFF6DBE6D), "⭐", "Star", listOf(listOf("🌱"), listOf("🌳"), listOf("🪑"), listOf("🎉")), listOf("grass", "trees", "benches", "a party"))

/**
 * A landmark for every stop, so each topic has its own place in the park (the "memory palace"
 * trick: a topic tied to a spot and an object is easier to recall).
 */
private val LANDMARKS: Map<String, String> = mapOf(
    "mt-1" to "🪢", "mt-2" to "🏗️", "mt-3" to "🔩", "mt-4" to "🛢️", "mt-5" to "🔄", "mt-6" to "🌀", "mt-7" to "🌉",
    "mt-8" to "📐", "mt-9" to "🎡", "mt-10" to "⚙️", "mt-11" to "🕹️", "mt-12" to "⛓️", "mt-13" to "🏃", "mt-14" to "🪃",
    "ec-1" to "💎", "ec-2" to "🔊", "ec-3" to "🔌", "ec-4" to "〰️", "ec-5" to "🔢", "ec-6" to "🚪", "ec-7" to "🧩",
    "ec-8" to "💾", "ec-9" to "📟", "ec-10" to "🗺️", "ec-11" to "🧪", "ec-12" to "⚖️", "ec-13" to "⏱️",
    "mm-1" to "🔲", "mm-2" to "♾️", "mm-3" to "🧮", "mm-4" to "📈", "mm-5" to "🎢", "mm-6" to "🪄", "mm-7" to "🧭", "mm-8" to "🎲",
    "pp-1" to "🌬️", "pp-2" to "🎛️", "pp-3" to "🧊", "pp-4" to "🛠️", "pp-5" to "📜",
    "ar-1" to "⚖️", "ar-2" to "🛂", "ar-3" to "🚦", "ar-4" to "📋", "ar-5" to "🔬", "ar-6" to "🎯", "ar-7" to "📊",
    "as-1" to "💧", "as-2" to "💨", "as-3" to "🛬", "as-4" to "🛞", "as-5" to "🌡️", "as-6" to "⚡",
    "tf-1" to "🪁", "tf-2" to "🦅", "tf-3" to "🚀", "tf-4" to "🔁", "tf-5" to "🚁",
)

private val Grey = Color(0xFFB9B9B9)
private val Gold = Color(0xFFFFC93C)

/** The stop the park points you to next: the first topic with notes that lacks a star. */
internal fun nextStop(units: List<StudyUnit>, progress: Progress): Topic? =
    units.flatMap { it.topics }.firstOrNull { it.notes.isNotEmpty() && progress.topicStars(it) < 3 }
        ?: units.flatMap { it.topics }.firstOrNull { progress.topicStars(it) < 3 }

/**
 * Kapi's Park: a map from the entrance (bottom) to the hot spring (top). Each unit is an area,
 * each topic a stop with 3 stars. Areas bloom as their stars come in; a finished area gives
 * Kapi a keepsake; finishing every area opens the hot spring.
 */
@Composable
fun ParkScreen(
    allUnits: List<StudyUnit>,
    progress: Progress,
    onBack: () -> Unit,
    onOpenTopic: (StudyUnit, Topic) -> Unit,
) {
    val units = parkUnits(allUnits)
    val total = units.sumOf { progress.unitStars(it) }
    val max = units.sumOf { it.topics.size * 3 }
    val next = nextStop(units, progress)
    val finished = units.filter { progress.unitStars(it) == it.topics.size * 3 }

    // Welcome-back stars and keepsake reveals, shown one after another.
    val popups = remember { mutableStateListOf<ParkPopup>() }
    LaunchedEffect(Unit) {
        val seen = progress.parkSeenStars()
        when {
            seen < 0 -> popups.add(ParkPopup.Welcome(total, max))
            total > seen -> popups.add(ParkPopup.NewStars(total - seen, total))
        }
        progress.setParkSeenStars(total)
        finished.filter { !progress.keepsakeShown(it.id) }.forEach { popups.add(ParkPopup.Keepsake(it.id, areaFor(it))) }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val i = units.indexOfFirst { u -> u.topics.any { it.id == next?.id } }
        if (i >= 0) listState.scrollToItem(i + 1)
    }

    val clock = rememberInfiniteTransition(label = "park")
    val pulse by clock.animateFloat(0f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")
    val drift by clock.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "drift")

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF8FD3FF), Color(0xFFCFF1FF), Color(0xFFE8F8E0))))) {
        Column(Modifier.fillMaxSize()) {
            Header(title = "Kapi's Park 🏞", subtitle = "⭐ $total of $max stars · ${finished.size} of ${units.size} areas complete", onBack = onBack)
            // Kapi's keepsake collection.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Image(painterResource(R.drawable.mascot_happy), contentDescription = "Kapi", modifier = Modifier.size(42.dp, 35.dp))
                Text("Keepsakes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B4F8F))
                Spacer(Modifier.weight(1f))
                units.forEach { u ->
                    val got = u in finished
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (got) Gold.copy(alpha = 0.35f) else Color(0x22000000)),
                    ) {
                        Text(if (got) areaFor(u).keepsake else "🔒", fontSize = if (got) 16.sp else 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { Entrance() }
                itemsIndexed(units, key = { _, u -> u.id }) { _, unit ->
                    AreaView(
                        unit = unit,
                        area = areaFor(unit),
                        progress = progress,
                        next = next,
                        pulse = pulse,
                        drift = drift,
                        onOpen = { onOpenTopic(unit, it) },
                    )
                }
                item { HotSpring(open = finished.size == units.size, pulse = pulse, drift = drift) }
            }
        }
        // Clouds drifting over the top of the map.
        Canvas(Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter)) {
            repeat(3) { k ->
                val x = ((drift + k * 0.37f) % 1f) * (size.width + 200f) - 100f
                val y = size.height * (0.3f + 0.2f * k)
                drawCircle(Color.White.copy(alpha = 0.35f), 26f, Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.35f), 34f, Offset(x + 30f, y - 8f))
                drawCircle(Color.White.copy(alpha = 0.35f), 24f, Offset(x + 62f, y))
            }
        }
        popups.firstOrNull()?.let { p ->
            PopupCard(p) {
                if (p is ParkPopup.Keepsake) progress.markKeepsakeShown(p.unitId)
                popups.removeAt(0)
            }
        }
    }
}

private sealed interface ParkPopup {
    data class Welcome(val total: Int, val max: Int) : ParkPopup
    data class NewStars(val gained: Int, val total: Int) : ParkPopup
    data class Keepsake(val unitId: String, val area: Area) : ParkPopup
}

/** The celebration cards: first visit, stars earned since last time, a keepsake unlocked. */
@Composable
private fun PopupCard(p: ParkPopup, onDismiss: () -> Unit) {
    val burst = remember(p) { Animatable(0f) }
    LaunchedEffect(p) { burst.animateTo(1f, tween(1600, easing = LinearEasing)) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
    ) {
        // Sparkles bursting out behind the card.
        Canvas(Modifier.fillMaxSize()) {
            val t = burst.value
            val c = Offset(size.width / 2f, size.height / 2f)
            val rnd = kotlin.random.Random(5)
            repeat(40) {
                val a = rnd.nextFloat() * 6.28f
                val d = (120f + rnd.nextFloat() * 420f) * t
                val col = listOf(Gold, Color(0xFFFF7EB6), Color(0xFF7CE0FF), Color(0xFF9CFF7C))[it % 4]
                rotate(t * 360f + it * 9f, c + Offset(kotlin.math.cos(a) * d, kotlin.math.sin(a) * d)) {
                    drawRect(col.copy(alpha = 1f - t * 0.8f), c + Offset(kotlin.math.cos(a) * d - 6f, kotlin.math.sin(a) * d - 3f), Size(12f, 6f))
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(28.dp)
                .shadow(18.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFF6D8), Color(0xFFFFE08A))))
                .border(3.dp, Color.White, RoundedCornerShape(26.dp))
                .padding(24.dp),
        ) {
            val s = burst.value.coerceAtMost(0.25f) / 0.25f
            when (p) {
                is ParkPopup.Welcome -> {
                    Image(painterResource(R.drawable.mascot_happy), contentDescription = null, modifier = Modifier.size(110.dp, 92.dp))
                    Text("Welcome to Kapi's Park!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF5A3A0A), textAlign = TextAlign.Center)
                    Text(
                        "Every topic is a stop with 3 stars:\n📖 read its notes · ✅ score 70%+ on its quiz · 🃏 learn half its flashcards.\n\nStars make each area bloom. Finish an area and Kapi gets a keepsake. Finish them all and the hot spring opens ♨️",
                        fontSize = 14.sp, color = Color(0xFF5A3A0A), textAlign = TextAlign.Center,
                    )
                    Text("You have ⭐ ${p.total} of ${p.max} already.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A3A0A))
                }
                is ParkPopup.NewStars -> {
                    Text("+${p.gained} ⭐", fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color(0xFFE08A00), modifier = Modifier.graphicsLayer { scaleX = 0.5f + 0.5f * s; scaleY = 0.5f + 0.5f * s })
                    Text("since your last visit!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A3A0A))
                    Text("The park is growing. You're at ⭐ ${p.total} now.", fontSize = 14.sp, color = Color(0xFF5A3A0A), textAlign = TextAlign.Center)
                }
                is ParkPopup.Keepsake -> {
                    Text("New keepsake!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8A5A00))
                    Text(p.area.keepsake, fontSize = 72.sp, modifier = Modifier.graphicsLayer { scaleX = s; scaleY = s; rotationZ = (1f - s) * -90f })
                    Text(p.area.keepsakeName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF5A3A0A))
                    Text("You finished ${p.area.name} ${p.area.emoji}. Kapi's wearing it with pride!", fontSize = 14.sp, color = Color(0xFF5A3A0A), textAlign = TextAlign.Center)
                }
            }
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FA84F))) {
                Text(if (p is ParkPopup.Welcome) "Let's go!" else "Yay!", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Entrance() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8B5A2B))
            .border(3.dp, Color(0xFF5A3714), RoundedCornerShape(20.dp))
            .padding(14.dp),
    ) {
        Text("🎪  Park entrance  🎪", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("Scroll up through the areas. Tap a stop to study it.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
    }
}

/**
 * Kapi's Hot Spring, the top of the park: an illustrated scene with Kapi soaking (orange on his
 * head), steam rising from the water. Shown greyed out with a lock until every area is complete.
 */
@Composable
private fun HotSpring(open: Boolean, pulse: Float, drift: Float) {
    val grey = remember { androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) } }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF3E5F3B)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(360f / 220f)
                .background(
                    Brush.verticalGradient(
                        if (open) listOf(Color(0xFF5B4B8A), Color(0xFFE58C8A), Color(0xFFF6C59A))
                        else listOf(Color(0xFF7A7A7A), Color(0xFFA8A8A8), Color(0xFFC4C4C4)),
                    ),
                ),
        ) {
            Image(
                painter = painterResource(R.drawable.hot_spring),
                contentDescription = "Kapi soaking in the hot spring",
                colorFilter = if (open) null else androidx.compose.ui.graphics.ColorFilter.colorMatrix(grey),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (open) 1f else 0.75f },
            )
            // Steam drifting up from the water.
            Canvas(Modifier.fillMaxSize()) {
                repeat(7) { k ->
                    val t = (drift * 4f + k / 7f) % 1f
                    val x = size.width * (0.3f + 0.4f * (k / 6f)) + sin(t * 9f + k) * size.width * 0.02f
                    val y = size.height * (0.72f - 0.55f * t)
                    val r = size.width * (0.025f + 0.04f * t)
                    drawCircle(Color.White.copy(alpha = (if (open) 0.45f else 0.25f) * (1f - t)), r, Offset(x, y))
                }
            }
            if (!open) {
                Text(
                    "🔒",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { scaleX = 1f + 0.06f * pulse; scaleY = 1f + 0.06f * pulse },
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
        ) {
            Text("♨️ Kapi's Hot Spring", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                if (open) "You did it. Every area, every star. Kapi is soaking in peace 🧡"
                else "Opens when every area is complete. Kapi's saving the orange for you 🍊",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** One area: a banner with its bloom progress, then the winding path of stops. */
@Composable
private fun AreaView(
    unit: StudyUnit,
    area: Area,
    progress: Progress,
    next: Topic?,
    pulse: Float,
    drift: Float,
    onOpen: (Topic) -> Unit,
) {
    val stars = progress.unitStars(unit)
    val max = unit.topics.size * 3
    val frac = if (max == 0) 0f else stars / max.toFloat()
    val stage = when {
        frac >= 1f -> 4
        frac >= 0.75f -> 3
        frac >= 0.5f -> 2
        frac >= 0.25f -> 1
        else -> 0
    }
    val ground = lerp(Grey, area.ground, (0.25f + frac * 0.75f).coerceAtMost(1f))
    val spacing: Dp = 112.dp
    val bottomRoom: Dp = 104.dp // the first stop's stars, name and "Next stop" tag fit above the area's edge
    val topRoom: Dp = 70.dp // room for Kapi above the last stop
    val pathH: Dp = spacing * (unit.topics.size - 1).coerceAtLeast(0) + bottomRoom + topRoom

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(ground),
    ) {
        // Banner (top of the area, since the map is climbed upward).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.18f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(area.emoji, fontSize = 26.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(area.name, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(unit.shortName, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Text("⭐ $stars/$max", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { frac },
                color = Gold,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            )
            Spacer(Modifier.height(4.dp))
            val nextThreshold = listOf(0.25f, 0.5f, 0.75f, 1f).firstOrNull { frac < it }
            Text(
                if (nextThreshold == null) {
                    "Area complete! Keepsake: ${area.keepsake} ${area.keepsakeName}"
                } else {
                    val need = kotlin.math.ceil(nextThreshold * max - stars).toInt().coerceAtLeast(1)
                    val what = area.bloomNames[listOf(0.25f, 0.5f, 0.75f, 1f).indexOf(nextThreshold)]
                    if (nextThreshold == 1f) "$need ⭐ to colour it all in and win the ${area.keepsake} keepsake" else "$need ⭐ until $what"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }

        // The path. Stops go upward: the first topic is at the bottom.
        BoxWithConstraints(Modifier.fillMaxWidth().height(pathH)) {
            val w = maxWidth
            fun xFor(k: Int): Dp = w / 2 + (w * 0.26f) * sin(k * 1.25f + 0.4f)
            fun yFor(k: Int): Dp = pathH - bottomRoom - spacing * k
            // The scene behind the path: grey to start, colour spreading out from starred stops.
            val starsPerStop = unit.topics.map { progress.topicStars(it) }
            Canvas(Modifier.fillMaxSize()) {
                val pts = unit.topics.indices.map { k -> Offset(xFor(k).toPx(), yFor(k).toPx()) }
                drawAreaScene(
                    areaId = unit.id,
                    stops = pts.mapIndexed { k, p -> StopLight(p, starsPerStop[k]) },
                    pathX = { y ->
                        // The path's x at height y, between the stops above and below.
                        val below = pts.lastOrNull { it.y >= y }
                        val above = pts.firstOrNull { it.y <= y }
                        when {
                            below == null -> above?.x ?: size.width / 2f
                            above == null -> below.x
                            below.y == above.y -> below.x
                            else -> below.x + (above.x - below.x) * (below.y - y) / (below.y - above.y)
                        }
                    },
                    complete = stage == 4,
                    time = drift,
                    unit = 1.dp.toPx(),
                )
            }
            if (stage == 0 && stars == 0) {
                Text(
                    "Grey for now. Earn a star on any stop and colour spreads from it 🎨",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A4A4A),
                    modifier = Modifier
                        .offset(12.dp, 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            // The winding path itself.
            Canvas(Modifier.fillMaxSize()) {
                val path = Path()
                unit.topics.indices.forEach { k ->
                    val p = Offset(xFor(k).toPx(), yFor(k).toPx())
                    if (k == 0) {
                        path.moveTo(p.x, size.height)
                        path.lineTo(p.x, p.y)
                    } else {
                        val prev = Offset(xFor(k - 1).toPx(), yFor(k - 1).toPx())
                        val midY = (prev.y + p.y) / 2f
                        path.cubicTo(prev.x, midY, p.x, midY, p.x, p.y)
                    }
                }
                val last = unit.topics.lastIndex
                if (last >= 0) path.lineTo(xFor(last).toPx(), 0f)
                drawPath(path, Color(0xFFF3E2B8), style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round))
                drawPath(
                    path,
                    Color(0xFFD9C08A),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 16f), drift * 340f)),
                )
            }
            // Stops.
            unit.topics.forEachIndexed { k, topic ->
                val got = progress.topicStarList(topic)
                val n = got.count { it }
                val isNext = topic.id == next?.id
                val stopSize = 58.dp
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(xFor(k) - 60.dp, yFor(k) - stopSize / 2)
                        .width(120.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // The stop's landmark (memory palace): faded until you've studied here.
                        Text(
                            LANDMARKS[topic.id] ?: area.emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .offset(if (xFor(k) > w / 2) (-50).dp else 50.dp, 4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (n > 0) 0.85f else 0.45f))
                                .padding(3.dp)
                                .graphicsLayer { alpha = if (n > 0) 1f else 0.4f },
                        )
                        if (isNext) {
                            Box(
                                Modifier
                                    .size(stopSize + (14 * pulse).dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.45f * (1f - pulse * 0.5f))),
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(stopSize)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        n == 3 -> Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFF0A800)))
                                        n > 0 -> Brush.verticalGradient(listOf(lerp(area.ground, Color.White, 0.3f), area.ground))
                                        else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFE6E6E6)))
                                    },
                                )
                                .border(3.dp, if (isNext) Gold else Color.White, CircleShape)
                                .clickable { onOpen(topic) },
                        ) {
                            Text(
                                if (n == 3) "👑" else "${k + 1}",
                                fontSize = if (n == 3) 24.sp else 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (n > 0) Color.White else Color(0xFF6B6B6B),
                            )
                        }
                        if (isNext) {
                            Image(
                                painterResource(R.drawable.mascot_happy),
                                contentDescription = "Kapi is here",
                                modifier = Modifier
                                    .size(54.dp, 45.dp)
                                    .offset(if (xFor(k) > w / 2) 46.dp else (-46).dp, (-30).dp - (4 * pulse).dp),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        got.forEach { Text(if (it) "⭐" else "☆", fontSize = 13.sp, color = Color(0xFF6B5A2A)) }
                    }
                    Text(
                        topic.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    if (isNext) {
                        Text(
                            "▶ Next stop",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE08A00))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
