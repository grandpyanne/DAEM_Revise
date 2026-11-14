package com.grandpyanne.daemrevise.data

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

// built by Grandpyanne™
/**
 * Everything the app remembers about your studying, saved on the phone
 * (SharedPreferences) so it survives closing the app:
 *  - flashcards you know and past-paper questions you have practised
 *  - quiz answers and best quiz score per topic
 *  - minutes studied per day (counted while the app is open)
 *
 * [version] changes on every save; screens read it so they redraw with fresh numbers.
 */
class Progress private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    var version by mutableIntStateOf(0)
        private set

    private fun saved() {
        version++
    }

    /** Reading [version] makes any screen that asks about progress redraw after a save. */
    private fun observe() {
        check(version >= 0)
    }

    // ---- flashcards (spaced repetition, like Anki) -------------------------

    /**
     * Where a card stands in its review schedule, or null if it has never been studied.
     * Cards marked "Got it" in the old flashcards (before spaced repetition) start as
     * learned and due today, so nobody loses progress.
     */
    fun cardState(topicId: String, index: Int): CardState? {
        observe()
        CardState.decode(prefs.getString(srsKey(topicId, index), null))?.let { return it }
        return if (prefs.getBoolean(cardKey(topicId, index), false)) {
            CardState(interval = 1, reps = 1, due = Srs.today(), learning = false)
        } else {
            null
        }
    }

    /** Records a rating and returns the card's new state. */
    fun rateCard(topicId: String, index: Int, rating: Rating): CardState {
        val today = Srs.today()
        val next = Srs.next(cardState(topicId, index) ?: CardState(due = today), rating, today)
        prefs.edit()
            .putString(srsKey(topicId, index), next.encode())
            .putBoolean(cardKey(topicId, index), next.graduated)
            .apply()
        saved()
        return next
    }

    /** A card counts as known once it has graduated from learning. */
    fun isCardKnown(topicId: String, index: Int): Boolean = cardState(topicId, index)?.graduated == true

    fun knownCards(topic: Topic): Int = topic.cards.indices.count { isCardKnown(topic.id, it) }

    fun knownCards(unit: StudyUnit): Int = unit.topics.sumOf { knownCards(it) }

    /** Cards that need reviewing today (studied before and now due). New cards are not counted. */
    fun dueCards(topics: List<Topic>): Int {
        val today = Srs.today()
        return topics.sumOf { t -> t.cards.indices.count { i -> cardState(t.id, i)?.let { it.due <= today } == true } }
    }

    /** Cards never studied yet. */
    fun newCards(topics: List<Topic>): Int = topics.sumOf { t -> t.cards.indices.count { cardState(t.id, it) == null } }

    /** The next day something comes due after today, and how many cards, or null if nothing is scheduled. */
    fun nextReview(topics: List<Topic>): Pair<Long, Int>? {
        val today = Srs.today()
        val days = topics.flatMap { t -> t.cards.indices.mapNotNull { cardState(t.id, it)?.due } }.filter { it > today }
        val first = days.minOrNull() ?: return null
        return first to days.count { it == first }
    }

    // ---- quiz questions (spaced repetition) ---------------------------------

    /** Where a quiz question stands in its review schedule, or null if never answered. */
    fun questionState(topicId: String, index: Int): CardState? {
        observe()
        return CardState.decode(prefs.getString(quizSrsKey(topicId, index), null))
    }

    /** Right answer counts as Good, wrong as Again (it comes back first next time). */
    fun rateQuestion(topicId: String, index: Int, right: Boolean): CardState {
        val today = Srs.today()
        val rating = if (right) Rating.GOOD else Rating.AGAIN
        val next = Srs.next(questionState(topicId, index) ?: CardState(due = today), rating, today)
        prefs.edit().putString(quizSrsKey(topicId, index), next.encode()).apply()
        saved()
        return next
    }

    /** Quiz questions answered before and due again today (missed ones included). */
    fun dueQuestions(topics: List<Topic>): Int {
        val today = Srs.today()
        return topics.sumOf { t -> t.mcqs.indices.count { i -> questionState(t.id, i)?.let { it.due <= today } == true } }
    }

    // ---- past-paper questions --------------------------------------------

    fun isQuestionDone(topicId: String, index: Int): Boolean {
        observe()
        return prefs.getBoolean(questionKey(topicId, index), false)
    }

    fun toggleQuestionDone(topicId: String, index: Int) {
        val key = questionKey(topicId, index)
        prefs.edit().putBoolean(key, !prefs.getBoolean(key, false)).apply()
        saved()
    }

    fun doneQuestions(topic: Topic): Int = topic.questions.indices.count { isQuestionDone(topic.id, it) }

    fun doneQuestions(unit: StudyUnit): Int = unit.topics.sumOf { doneQuestions(it) }

    // ---- quizzes ----------------------------------------------------------

    /** Call once per answered MCQ. */
    fun recordAnswer(correct: Boolean) {
        val day = dayKey(0)
        val edit = prefs.edit()
            .putInt("mcq:total", prefs.getInt("mcq:total", 0) + 1)
            .putInt("mcq:day:$day", prefs.getInt("mcq:day:$day", 0) + 1)
        if (correct) {
            edit.putInt("mcq:right", prefs.getInt("mcq:right", 0) + 1)
                .putInt("mcq:dayRight:$day", prefs.getInt("mcq:dayRight:$day", 0) + 1)
        }
        edit.apply()
        saved()
    }

    /** Remembers the best percentage for a topic (or "unit:<id>" for a unit quiz). */
    fun recordQuizScore(key: String, percent: Int) {
        if (percent > bestQuizScore(key)) {
            prefs.edit().putInt("quizBest:$key", percent).apply()
            saved()
        }
    }

    /** Best quiz percentage, or -1 if never taken. */
    fun bestQuizScore(key: String): Int {
        observe()
        return prefs.getInt("quizBest:$key", -1)
    }

    fun answersToday(): Int {
        observe()
        return prefs.getInt("mcq:day:${dayKey(0)}", 0)
    }

    /** Percentage of all MCQs answered correctly, or -1 before the first answer. */
    fun overallAccuracy(): Int {
        observe()
        val total = prefs.getInt("mcq:total", 0)
        if (total == 0) return -1
        return (100.0 * prefs.getInt("mcq:right", 0) / total).roundToInt()
    }

    // ---- study time -------------------------------------------------------

    /** When the current session started, or -1 when the app is not on screen. */
    private var sessionStart = -1L

    /** Called when the app comes to the front. */
    fun startSession() {
        if (studyPaused) return
        sessionStart = SystemClock.elapsedRealtime()
    }

    /** True while a game is open: game time is play, not study, so it is not counted. */
    private var studyPaused = false

    /** Stops the study clock (opening a game). */
    fun pauseStudy() {
        endSession()
        studyPaused = true
    }

    /** Starts the study clock again (leaving a game). */
    fun resumeStudy() {
        studyPaused = false
        startSession()
    }

    /** Called when the app goes to the background. */
    fun endSession() {
        if (sessionStart < 0L) return
        val seconds = ((SystemClock.elapsedRealtime() - sessionStart) / 1000).toInt()
        sessionStart = -1L
        // Ignore anything over 3 hours in one go (phone left on a screen).
        if (seconds in 1..3 * 3600) addSeconds(seconds)
    }

    /** Saves the time so far without ending the session, so the home screen stays current. */
    fun flush() {
        if (sessionStart >= 0L) {
            endSession()
            startSession()
        }
    }

    private fun addSeconds(seconds: Int) {
        val key = "time:${dayKey(0)}"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + seconds).apply()
        saved()
    }

    /** Minutes studied on the day [daysAgo] days before today (0 = today). */
    fun minutesOn(daysAgo: Int): Int {
        observe()
        return prefs.getInt("time:${dayKey(daysAgo)}", 0) / 60
    }

    /**
     * Minutes for this calendar week, Sunday first and Saturday last.
     * Days still to come are null.
     */
    fun thisWeek(): List<Int?> {
        val today = todayIndexInWeek()
        return (0..6).map { day -> if (day > today) null else minutesOn(today - day) }
    }

    /** Where today sits in [thisWeek]: 0 = Sunday … 6 = Saturday. */
    fun todayIndexInWeek(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY

    /** Consecutive days (ending today, or yesterday if today has no study yet) with 5+ minutes. */
    fun streakDays(): Int {
        var start = if (minutesOn(0) >= STREAK_MINUTES) 0 else 1
        var count = 0
        while (minutesOn(start) >= STREAK_MINUTES) {
            count++
            start++
        }
        return count
    }

    // ---- study score ------------------------------------------------------

    /**
     * Today's study score out of 100:
     *  50 points for time (full marks at [DAILY_GOAL_MINUTES]),
     *  30 points for today's quiz accuracy (15 if no quiz yet today),
     *  20 points for the streak (full at 7 days).
     */
    fun studyScore(): Int {
        val timePart = 50.0 * min(1.0, minutesOn(0).toDouble() / DAILY_GOAL_MINUTES)
        val answered = answersToday()
        val accuracyPart = if (answered == 0) {
            15.0
        } else {
            30.0 * prefs.getInt("mcq:dayRight:${dayKey(0)}", 0) / answered
        }
        val streakPart = 20.0 * min(1.0, streakDays() / 7.0)
        return (timePart + accuracyPart + streakPart).roundToInt()
    }

    // ---- games (Kapi Blocks, Kapi Jewels, Kapi Mahjong) ---------------------------------------
    // Plays are earned by studying: every GAME_MINUTES minutes of study, or a Quick 5 with
    // 4 or more right (up to MAX_QUICK_GAMES of those). At most MAX_GAMES_PER_DAY a day, shared between the games.

    /** Records a finished Quick 5. Returns true if it earned a game. */
    fun recordQuickFive(correct: Int): Boolean {
        if (correct < QUICK_ACE) return false
        val key = "aces:${dayKey(0)}"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
        saved()
        return true
    }

    /** Games earned today (study time + aced Quick 5s), capped. */
    fun gamesEarnedToday(): Int {
        observe()
        val fromTime = minutesOn(0) / GAME_MINUTES
        val fromAces = min(MAX_QUICK_GAMES, prefs.getInt("aces:${dayKey(0)}", 0))
        return min(MAX_GAMES_PER_DAY, fromTime + fromAces)
    }

    fun gamesUsedToday(): Int {
        observe()
        return prefs.getInt("games:${dayKey(0)}", 0)
    }

    /** Games ready to play right now. */
    fun gamesAvailable(): Int =
        if (DEV_UNLOCK_GAMES) MAX_GAMES_PER_DAY else (gamesEarnedToday() - gamesUsedToday()).coerceAtLeast(0)

    /** True once today's games are all earned and played. */
    fun gamesDoneForToday(): Boolean = gamesUsedToday() >= MAX_GAMES_PER_DAY

    /** How far the study clock is toward the next game, 0..1 (no numbers shown: Kapi keeps it a secret). */
    fun progressToNextGame(): Float = (minutesOn(0) % GAME_MINUTES) / GAME_MINUTES.toFloat()

    /** Each game gets one free try a day, before any study, so you know what's waiting. */
    fun freeTryLeft(game: String): Boolean {
        observe()
        return !prefs.getBoolean("freeTry:$game:${dayKey(0)}", false)
    }

    fun useFreeTry(game: String): Boolean {
        if (!freeTryLeft(game)) return false
        prefs.edit().putBoolean("freeTry:$game:${dayKey(0)}", true).apply()
        saved()
        return true
    }

    /** Uses up one game. Returns false if none was available. */
    fun useGame(): Boolean {
        if (DEV_UNLOCK_GAMES) return true
        if (gamesAvailable() <= 0) return false
        val key = "games:${dayKey(0)}"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
        saved()
        return true
    }

    /** The level a game is on (Crush and Mahjong), starting at 1. */
    fun gameLevel(game: String): Int {
        observe()
        return prefs.getInt("gameLevel:$game", 1)
    }

    fun setGameLevel(game: String, level: Int) {
        prefs.edit().putInt("gameLevel:$game", level.coerceAtLeast(1)).apply()
        saved()
    }

    /** True once the how-to card for [key] has been dismissed. */
    fun tipSeen(key: String): Boolean = prefs.getBoolean("tip:$key", false)

    fun markTipSeen(key: String) {
        prefs.edit().putBoolean("tip:$key", true).apply()
    }

    fun bestGameScore(game: String): Int {
        observe()
        return prefs.getInt("gameBest:$game", 0)
    }

    /** Saves the score; returns true if it is a new best. */
    fun recordGameScore(game: String, score: Int): Boolean {
        if (score <= bestGameScore(game)) return false
        prefs.edit().putInt("gameBest:$game", score).apply()
        saved()
        return true
    }

    // ---- Kapi's Park ----------------------------------------------------------
    // Each topic is a stop in the park with up to 3 stars, all from things you already do:
    // read the notes, score 70%+ on its quiz, learn half its flashcards.

    fun notesRead(topicId: String): Boolean {
        observe()
        return prefs.getBoolean("notesRead:$topicId", false)
    }

    fun setNotesRead(topicId: String, read: Boolean) {
        prefs.edit().putBoolean("notesRead:$topicId", read).apply()
        saved()
    }

    /** The three stars for a topic: notes read, quiz 70%+, half the flashcards learned. */
    fun topicStarList(t: Topic): List<Boolean> {
        val practised = doneQuestions(t) > 0
        val quiz = if (t.mcqs.isEmpty()) practised else bestQuizScore(t.id) >= PARK_QUIZ_PASS
        val cards = if (t.cards.isEmpty()) practised else knownCards(t) * 2 >= t.cards.size
        return listOf(notesRead(t.id), quiz, cards)
    }

    fun topicStars(t: Topic): Int = topicStarList(t).count { it }

    fun unitStars(u: StudyUnit): Int = u.topics.sumOf { topicStars(it) }

    /** Stars counted at the last park visit (-1 before the first visit). */
    fun parkSeenStars(): Int = prefs.getInt("park:seenStars", -1)

    fun setParkSeenStars(n: Int) {
        prefs.edit().putInt("park:seenStars", n).apply()
    }

    /** True once the keepsake for a finished area has been revealed. */
    fun keepsakeShown(unitId: String): Boolean = prefs.getBoolean("park:keepsake:$unitId", false)

    fun markKeepsakeShown(unitId: String) {
        prefs.edit().putBoolean("park:keepsake:$unitId", true).apply()
    }

    // ---- reset ------------------------------------------------------------

    fun resetUnit(unit: StudyUnit) {
        val editor = prefs.edit()
        unit.topics.forEach { topic ->
            topic.cards.indices.forEach {
                editor.remove(cardKey(topic.id, it))
                editor.remove(srsKey(topic.id, it))
            }
            topic.mcqs.indices.forEach { editor.remove(quizSrsKey(topic.id, it)) }
            topic.questions.indices.forEach { editor.remove(questionKey(topic.id, it)) }
            editor.remove("quizBest:${topic.id}")
            editor.remove("notesRead:${topic.id}")
        }
        editor.remove("quizBest:unit:${unit.id}")
        editor.apply()
        saved()
    }

    private fun cardKey(topicId: String, index: Int) = "card:$topicId:$index"
    private fun srsKey(topicId: String, index: Int) = "srs:$topicId:$index"
    private fun quizSrsKey(topicId: String, index: Int) = "qsrs:$topicId:$index"
    private fun questionKey(topicId: String, index: Int) = "q:$topicId:$index"

    /** "2026-10-28" style key for a day, counted back from today. */
    private fun dayKey(daysAgo: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        return String.format(
            Locale.US, "%04d-%02d-%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
        )
    }

    companion object {
        /** Labels for [thisWeek]. */
        val WEEK_LABELS = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

        @Volatile
        private var instance: Progress? = null

        /** The one shared Progress, used by the app and the home-screen widget. */
        fun get(context: Context): Progress =
            instance ?: synchronized(this) {
                instance ?: Progress(context.applicationContext).also { instance = it }
            }

        const val DAILY_GOAL_MINUTES = 60
        const val STREAK_MINUTES = 5

        /**
         * DEVELOPER SWITCH: true = all games unlocked, nothing used up (for building and testing).
         * Set back to false before sending the app to anyone.
         */
        const val DEV_UNLOCK_GAMES = true

        /**
         * DEVELOPER SWITCH: true = faint blueprint drawings behind topic screens (notes, flashcards,
         * quiz) for topics that have one. Still being tried out; keep false for the Play Store build.
         */
        const val DEV_TOPIC_ART = true

        /** Kapi's lucky number 🤫 */
        const val GAME_MINUTES = 27
        const val MAX_GAMES_PER_DAY = 5
        /** At most this many of a day's games can come from Quick 5s; the rest need study time. */
        const val MAX_QUICK_GAMES = 2
        const val QUICK_ACE = 4

        /** Quiz score that earns a topic's quiz star in Kapi's Park. */
        const val PARK_QUIZ_PASS = 70

        fun scoreLabel(score: Int): String = when {
            score >= 85 -> "Excellent"
            score >= 65 -> "Good"
            score >= 40 -> "Fair"
            else -> "Getting started"
        }
    }
}
