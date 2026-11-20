package com.grandpyanne.daemrevise.data

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spaced repetition, the way Anki does it (the SM-2 algorithm).
 *
 * Every time you see a card you rate how well you remembered it. Cards you know well
 * come back after longer and longer gaps (1 day, 3 days, a week, a month…), just before
 * you would forget them; cards you miss come back the same session and again tomorrow.
 * That is how it beats the forgetting curve with the least study time.
 */
enum class Rating { AGAIN, HARD, GOOD, EASY }

/**
 * Where one flashcard stands.
 * @param ease how easy the card is for you; intervals grow by this factor (starts at 2.5, never below 1.3)
 * @param interval days until the next review once the card has graduated
 * @param reps successful reviews so far (0 = never answered "Good" or better)
 * @param lapses times you forgot it after it had graduated
 * @param due the day (see [Srs.today]) the card is next due
 * @param learning true while the card is still being learnt (new, or forgotten) and repeats within a session
 */
data class CardState(
    val ease: Double = 2.5,
    val interval: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val due: Long = 0,
    val learning: Boolean = true,
) {
    /** Graduated cards are the ones counted as "known". */
    val graduated: Boolean get() = !learning && reps > 0

    fun encode(): String = "$ease,$interval,$reps,$lapses,$due,${if (learning) 1 else 0}"

    companion object {
        fun decode(text: String?): CardState? = try {
            val p = text!!.split(",")
            CardState(p[0].toDouble(), p[1].toInt(), p[2].toInt(), p[3].toInt(), p[4].toLong(), p[5] == "1")
        } catch (_: Exception) {
            null
        }
    }
}

// crafted by Grandpyanne™
object Srs {

    const val MIN_EASE = 1.3
    const val MAX_INTERVAL = 365

    /** New cards introduced per session, like Anki's "new cards per day". */
    const val NEW_PER_SESSION = 15

    /** Today's date as a day number, in local time, so cards turn due at midnight. */
    fun today(nowMillis: Long = System.currentTimeMillis()): Long {
        val offset = java.util.TimeZone.getDefault().getOffset(nowMillis)
        return Math.floorDiv(nowMillis + offset, 86_400_000L)
    }

    /** The card's new state after you rate it today. */
    fun next(s: CardState, rating: Rating, today: Long): CardState {
        if (s.learning) {
            return when (rating) {
                // Still learning: it comes back in this session (see [requeueGap]).
                Rating.AGAIN, Rating.HARD -> s.copy(due = today)
                // Graduates. A forgotten card restarts at 1 day; a new one at 1 day (Good) or 4 days (Easy).
                Rating.GOOD -> s.copy(interval = 1, reps = s.reps + 1, due = today + 1, learning = false)
                Rating.EASY -> {
                    val days = if (s.lapses > 0) 2 else 4
                    s.copy(interval = days, reps = s.reps + 1, due = today + days, learning = false,
                        ease = s.ease + 0.15)
                }
            }
        }
        val i = s.interval
        // Hard < Good < Easy, always at least a day apart.
        val good = max(i + 1, (i * s.ease).roundToInt())
        val hard = min(good - 1, max(i + 1, (i * 1.2).roundToInt())).coerceAtLeast(1)
        val easy = max(good + 1, (i * s.ease * 1.3).roundToInt())
        return when (rating) {
            Rating.AGAIN -> s.copy(
                lapses = s.lapses + 1,
                ease = max(MIN_EASE, s.ease - 0.2),
                interval = 1,
                due = today,
                learning = true,
            )
            Rating.HARD -> grow(s.copy(ease = max(MIN_EASE, s.ease - 0.15)), hard, today)
            Rating.GOOD -> grow(s, good, today)
            Rating.EASY -> grow(s.copy(ease = s.ease + 0.15), easy, today)
        }
    }

    private fun grow(s: CardState, days: Int, today: Long): CardState {
        val d = min(MAX_INTERVAL, days)
        return s.copy(interval = d, reps = s.reps + 1, due = today + d, learning = false)
    }

    /** For learning cards rated Again/Hard: how many cards later it comes back in this session. */
    fun requeueGap(rating: Rating): Int = if (rating == Rating.AGAIN) 3 else 6

    /** What each button shows, like Anki: "<1m", "<10m", "1d", "4d", "2mo"… */
    fun preview(s: CardState, rating: Rating, today: Long): String {
        if (s.learning && (rating == Rating.AGAIN || rating == Rating.HARD)) {
            return if (rating == Rating.AGAIN) "<1m" else "<10m"
        }
        if (!s.learning && rating == Rating.AGAIN) return "<1m"
        return formatDays(next(s, rating, today).interval)
    }

    fun formatDays(days: Int): String = when {
        days < 30 -> "${days}d"
        days < 365 -> "${(days / 30.0).roundToInt()}mo"
        else -> "1y"
    }
}
