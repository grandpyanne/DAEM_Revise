package com.grandpyanne.daemrevise.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Grandpyanne™ was here
/** A formula shown on a topic's Formulas tab. */
data class Formula(val name: String, val expr: String)

/** A flashcard: question on the front, answer on the back. */
data class Card(val front: String, val back: String)

/** One past-paper question and the sitting(s) it came from. */
data class Question(val text: String, val source: String)

/** One piece of a topic's revision notes. */
sealed interface NoteBlock {
    data class Heading(val text: String) : NoteBlock
    data class Paragraph(val text: String) : NoteBlock
    data class Bullets(val items: List<String>) : NoteBlock
    data class FormulaLine(val text: String) : NoteBlock
    data class Tip(val text: String) : NoteBlock
}

/** A multiple-choice question. [answer] is the index of the correct option. */
data class Mcq(val question: String, val options: List<String>, val answer: Int, val explanation: String)

/** A lecturer's PDF on the student's PC that covers this topic in full. */
data class PdfRef(val file: String, val folder: String, val where: String)

/**
 * A textbook bundled with the app. [file] is its PDF inside the app's assets; [units] are the units it serves.
 * [opens] maps a unit to the section the book opens at from that unit's shelf (page + description).
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val file: String,
    val units: List<String> = emptyList(),
    val opens: Map<String, BookRef> = emptyMap(),
)

/** Where to read a topic in a textbook. [page] is the PDF page (0 = not known). */
data class BookRef(val bookId: String, val where: String, val page: Int)

// Grandpyanne™
/** Where a topic sits in this term's teaching. */
enum class TopicStatus(val label: String) {
    COVERED("Covered"),
    SAFETY("Safety"),
    LATER("Later");

    companion object {
        fun from(text: String): TopicStatus = when (text) {
            "safety" -> SAFETY
            "later" -> LATER
            else -> COVERED
        }
    }
}

data class Topic(
    val id: String,
    val title: String,
    val section: String,
    /** Which half of a combined unit this topic belongs to ("" if the unit is not split). */
    val part: String,
    val summary: String,
    val status: TopicStatus,
    val notes: List<NoteBlock>,
    val mcqs: List<Mcq>,
    val pdfs: List<PdfRef>,
    val refs: List<BookRef>,
    val formulas: List<Formula>,
    val cards: List<Card>,
    val questions: List<Question>,
) {
    /** Number of different KNEC sittings that asked this topic. */
    val sittings: Int = questions
        .flatMap { SITTING.findAll(it.source).map { m -> m.value }.toList() }
        .toSet()
        .size

    /** Topics taught this term, plus the "just in case" ones. */
    val inTermScope: Boolean get() = status != TopicStatus.LATER

    private companion object {
        val SITTING = Regex("(June/July|Oct/Nov|March/April) \\d{4}")
    }
}

data class StudyUnit(
    val id: String,
    val name: String,
    val shortName: String,
    val note: String,
    val ready: Boolean,
    val topics: List<Topic>,
    /** For combined units (e.g. Airfield Safety II & Research Methods): the parts, shown as tabs. */
    val parts: List<String> = emptyList(),
) {
    val cardCount: Int get() = topics.sumOf { it.cards.size }
    val questionCount: Int get() = topics.sumOf { it.questions.size }
    val mcqCount: Int get() = topics.sumOf { it.mcqs.size }
}

/** One year of an option, listing its units by id (units can be shared between courses). */
data class StudyYear(val id: String, val name: String, val unitIds: List<String>)

/** An option within a course, e.g. Airframes & Engines or Avionics. */
data class CourseOption(val id: String, val name: String, val years: List<StudyYear>)

/** A course, e.g. the Diploma in Aeronautical Engineering. */
data class Course(val id: String, val name: String, val shortName: String, val options: List<CourseOption>)

/** Everything in content.json: all units once, plus which course/option/year uses them. */
data class Catalog(
    val courses: List<Course>,
    val unitsById: Map<String, StudyUnit>,
    val books: List<Book> = emptyList(),
) {
    val booksById: Map<String, Book> get() = books.associateBy { it.id }


    /** The course, option and year whose units the app shows (the first one for now). */
    val course: Course get() = courses.first()
    val option: CourseOption get() = course.options.first()
    val year: StudyYear get() = option.years.first()

    /** Units for the current year, in the order the catalog lists them. */
    val units: List<StudyUnit> get() = year.unitIds.mapNotNull { unitsById[it] }

    /** e.g. "DAEM · Airframes & Engines · Year 2" */
    val label: String get() = "${course.shortName} · ${option.name} · ${year.name}"
}

/** Reads assets/content.json once and keeps it in memory. */
object ContentRepository {

    @Volatile
    private var cache: Catalog? = null

    fun catalog(context: Context): Catalog =
        cache ?: synchronized(this) {
            cache ?: load(context).also { cache = it }
        }

    /** Units the student is studying (current course, option and year). */
    fun units(context: Context): List<StudyUnit> = catalog(context).units

    private fun load(context: Context): Catalog {
        val text = context.assets.open("content.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        val units = root.getJSONArray("units").objects().map { u ->
            StudyUnit(
                id = u.getString("id"),
                name = u.getString("name"),
                shortName = u.getString("short"),
                note = u.optString("note"),
                ready = u.optBoolean("ready", false),
                topics = u.getJSONArray("topics").objects().map(::parseTopic),
                parts = u.optArray("parts").strings(),
            )
        }
        val courses = root.optArray("courses").objects().map { c ->
            Course(
                id = c.getString("id"),
                name = c.getString("name"),
                shortName = c.optString("short", c.getString("name")),
                options = c.getJSONArray("options").objects().map { o ->
                    CourseOption(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        years = o.getJSONArray("years").objects().map { y ->
                            StudyYear(y.getString("id"), y.getString("name"), y.getJSONArray("unitIds").strings())
                        },
                    )
                },
            )
        }.ifEmpty {
            // Older content files have no catalog: show every unit.
            listOf(Course("all", "All units", "All", listOf(CourseOption("all", "All", listOf(StudyYear("all", "All", units.map { it.id }))))))
        }
        val books = root.optArray("books").objects().map { b ->
            Book(
                id = b.getString("id"),
                title = b.getString("title"),
                author = b.optString("author"),
                file = b.getString("file"),
                units = b.optArray("units").strings(),
                opens = b.optJSONObject("opens")?.let { o ->
                    o.keys().asSequence().associateWith { unit ->
                        val r = o.getJSONObject(unit)
                        BookRef(b.getString("id"), r.optString("where"), r.optInt("page", 1))
                    }
                }.orEmpty(),
            )
        }
        return Catalog(courses, units.associateBy { it.id }, books)
    }

    private fun parseTopic(t: JSONObject) = Topic(
        id = t.getString("id"),
        title = t.getString("title"),
        section = t.optString("section"),
        part = t.optString("part"),
        summary = t.optString("summary"),
        status = TopicStatus.from(t.optString("status")),
        notes = t.optArray("notes").objects().map(::parseNote),
        mcqs = t.optArray("mcqs").objects().map { m ->
            Mcq(
                question = m.getString("q"),
                options = m.getJSONArray("options").strings(),
                answer = m.getInt("answer"),
                explanation = m.optString("explain"),
            )
        },
        pdfs = t.optArray("pdfs").objects()
            .map { PdfRef(it.getString("file"), it.optString("folder"), it.optString("where")) },
        refs = t.optArray("refs").objects()
            .map { BookRef(it.getString("book"), it.optString("where"), it.optInt("page", 0)) },
        formulas = t.optArray("formulas").objects()
            .map { Formula(it.getString("name"), it.getString("expr")) },
        cards = t.optArray("cards").objects()
            .map { Card(it.getString("front"), it.getString("back")) },
        questions = t.optArray("questions").objects()
            .map { Question(it.getString("q"), it.optString("src")) },
    )

    private fun parseNote(n: JSONObject): NoteBlock = when (n.getString("type")) {
        "h" -> NoteBlock.Heading(n.getString("text"))
        "bullets" -> NoteBlock.Bullets(n.getJSONArray("items").strings())
        "formula" -> NoteBlock.FormulaLine(n.getString("text"))
        "tip" -> NoteBlock.Tip(n.getString("text"))
        else -> NoteBlock.Paragraph(n.getString("text"))
    }

    private fun JSONObject.optArray(key: String): JSONArray = optJSONArray(key) ?: JSONArray()
    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
}
