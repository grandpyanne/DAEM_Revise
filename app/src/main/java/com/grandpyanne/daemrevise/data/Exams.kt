package com.grandpyanne.daemrevise.data

import java.util.Calendar
import java.util.TimeZone

// Grandpyanne™
/** One paper in the exam timetable. Times are Kenya time (Africa/Nairobi). */
data class ExamPaper(
    val title: String,
    /** The unit to open when the paper is tapped (StudyUnit.id). */
    val unitId: String,
    val startMillis: Long,
    val endMillis: Long,
    val venue: String,
)

/**
 * The end-of-term timetable (EASA, Term 1 2026/2027, November 2026), stage 2M (May 2025 intake,
 * Airframes & Engines). The card shows until a week after the last paper, then hides itself.
 * For the next exams, replace the list (and nothing else).
 */
object Exams {
    private val NAIROBI: TimeZone = TimeZone.getTimeZone("Africa/Nairobi")

    private fun at(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance(NAIROBI).apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    private fun paper(title: String, unitId: String, day: Int, morning: Boolean, venue: String) = ExamPaper(
        title = title,
        unitId = unitId,
        startMillis = at(2026, 11, day, if (morning) 9 else 14),
        endMillis = at(2026, 11, day, if (morning) 12 else 17),
        venue = venue,
    )

    val papers: List<ExamPaper> = listOf(
        paper("Engineering Maths II", "maths2", 23, morning = true, venue = "B 17"),
        paper("ASP II & Research Methods", "airfield", 23, morning = false, venue = "B 17"),
        paper("Control Systems & Electronics", "electronics", 24, morning = true, venue = "B 13"),
        paper("Aircraft Propeller Systems", "propellers", 24, morning = false, venue = "B 13"),
        paper("Airframe Systems I", "airframe", 26, morning = true, venue = "B 13"),
        paper("Theory of Flight", "tof", 26, morning = false, venue = "B 13"),
        paper("Aircraft Mechanical Technology I", "mechtech", 27, morning = true, venue = "B 19"),
    )

    /** How long the card stays after the last paper ends. */
    const val KEEP_AFTER_MILLIS: Long = 7L * 24 * 60 * 60 * 1000

    /** Show the card now? Only for the course these papers belong to, until a week after the last one. */
    fun visible(now: Long, unitIds: Set<String>): Boolean =
        papers.isNotEmpty() &&
            papers.all { it.unitId in unitIds } &&
            now < papers.maxOf { it.endMillis } + KEEP_AFTER_MILLIS
}
