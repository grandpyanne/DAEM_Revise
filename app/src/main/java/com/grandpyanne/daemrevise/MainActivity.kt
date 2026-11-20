package com.grandpyanne.daemrevise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.grandpyanne.daemrevise.data.ContentRepository
import com.grandpyanne.daemrevise.data.CrashLog
import com.grandpyanne.daemrevise.data.Feedback
import com.grandpyanne.daemrevise.data.Progress
import com.grandpyanne.daemrevise.data.Settings
import com.grandpyanne.daemrevise.ui.AppColors
import com.grandpyanne.daemrevise.ui.CrashScreen
import com.grandpyanne.daemrevise.ui.DaemReviseTheme
import com.grandpyanne.daemrevise.ui.FlashcardScreen
import com.grandpyanne.daemrevise.ui.GamesScreen
import com.grandpyanne.daemrevise.ui.HomeScreen
import com.grandpyanne.daemrevise.ui.LibraryScreen
import com.grandpyanne.daemrevise.ui.ParkScreen
import com.grandpyanne.daemrevise.ui.QuizScreen
import com.grandpyanne.daemrevise.ui.ReaderScreen
import com.grandpyanne.daemrevise.ui.SearchScreen
import com.grandpyanne.daemrevise.ui.TopicScreen
import com.grandpyanne.daemrevise.ui.UnitScreen
import com.grandpyanne.daemrevise.ui.WelcomeScreen
import com.grandpyanne.daemrevise.widget.StudyWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var progress: Progress
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progress = Progress.get(applicationContext)
        settings = Settings(applicationContext)
        enableEdgeToEdge()
        setContent {
            DaemReviseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(Modifier.safeDrawingPadding()) {
                        // If the app crashed last time, show what happened before anything else.
                        var crash by remember { mutableStateOf(CrashLog.read(this@MainActivity)) }
                        val report = crash
                        if (report != null) {
                            CrashScreen(
                                report = report,
                                onSend = { Feedback.sendReport(this@MainActivity, report) },
                                onContinue = {
                                    CrashLog.clear(this@MainActivity)
                                    crash = null
                                },
                            )
                        } else {
                            App(progress, settings)
                        }
                    }
                }
            }
        }
    }

    // Study time counts while the app is on screen.
    override fun onResume() {
        super.onResume()
        progress.startSession()
    }

    override fun onPause() {
        progress.endSession()
        // Show the new minutes and streak on the home-screen widget.
        // A widget problem must never close the app, so errors here are ignored.
        lifecycleScope.launch { runCatching { StudyWidget.refresh(applicationContext) } }
        super.onPause()
    }
}

// — Grandpyanne™
/** Every screen the app can show. The newest entry in the back stack is on screen. */
sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Library : Screen
    data object Welcome : Screen
    data class Reader(val bookId: String, val page: Int) : Screen
    data class UnitPage(val unitId: String) : Screen
    data class Topic(val unitId: String, val topicId: String) : Screen

    /** topicId = null means "this term's topics in the whole unit". */
    data class Flashcards(val unitId: String, val topicId: String?) : Screen

    /** topicId = null means a 10-question quiz across this term's topics. */
    data class Quiz(val unitId: String, val topicId: String?) : Screen

    /** 5 quick MCQs from every unit (due ones first). */
    data object QuickFive : Screen

    /** Kapi's games, unlocked by studying. */
    data object Games : Screen

    /** Kapi's Park: the map of every topic, with stars and blooming areas. */
    data object Park : Screen
}

/** A screen as a short string, so the back stack survives Android closing the app in the background. */
private fun Screen.encode(): String = when (this) {
    Screen.Home -> "home"
    Screen.Search -> "search"
    Screen.Library -> "library"
    Screen.Welcome -> "welcome"
    Screen.QuickFive -> "quick"
    Screen.Games -> "games"
    Screen.Park -> "park"
    is Screen.Reader -> "reader|$bookId|$page"
    is Screen.UnitPage -> "unit|$unitId"
    is Screen.Topic -> "topic|$unitId|$topicId"
    is Screen.Flashcards -> "cards|$unitId|${topicId.orEmpty()}"
    is Screen.Quiz -> "quiz|$unitId|${topicId.orEmpty()}"
}

/** The reverse of [encode]; null for anything that no longer exists. */
private fun decodeScreen(s: String, unitIds: Set<String>): Screen? {
    val p = s.split("|")
    fun unit() = p.getOrNull(1)?.takeIf { it in unitIds }
    return when (p[0]) {
        "home" -> Screen.Home
        "search" -> Screen.Search
        "library" -> Screen.Library
        "welcome" -> Screen.Welcome
        "quick" -> Screen.QuickFive
        "games" -> Screen.Games
        "park" -> Screen.Park
        "reader" -> p.getOrNull(2)?.toIntOrNull()?.let { Screen.Reader(p[1], it) }
        "unit" -> unit()?.let { Screen.UnitPage(it) }
        "topic" -> unit()?.let { u -> p.getOrNull(2)?.let { Screen.Topic(u, it) } }
        "cards" -> unit()?.let { Screen.Flashcards(it, p.getOrNull(2)?.ifEmpty { null }) }
        "quiz" -> unit()?.let { Screen.Quiz(it, p.getOrNull(2)?.ifEmpty { null }) }
        else -> null
    }
}

// built by Grandpyanne™
@Composable
private fun App(progress: Progress, settings: Settings) {
    val context = LocalContext.current
    val catalog = remember { ContentRepository.catalog(context) }
    val units = catalog.units
    // First launch starts with the welcome tour. Saveable, so coming back to the app after
    // Android closed it in the background returns to the same screen (e.g. mid-quiz).
    val unitIds = remember(units) { units.map { it.id }.toSet() }
    val backStack = rememberSaveable(
        saver = listSaver<SnapshotStateList<Screen>, String>(
            save = { stack -> stack.map { it.encode() } },
            restore = { saved ->
                mutableStateListOf<Screen>().apply {
                    addAll(saved.mapNotNull { decodeScreen(it, unitIds) }.filter { s ->
                        // Drop anything whose topic has gone since it was saved.
                        when (s) {
                            is Screen.Topic -> units.first { it.id == s.unitId }.topics.any { it.id == s.topicId }
                            is Screen.Quiz -> s.topicId == null || units.first { it.id == s.unitId }.topics.any { it.id == s.topicId }
                            is Screen.Flashcards -> s.topicId == null || units.first { it.id == s.unitId }.topics.any { it.id == s.topicId }
                            else -> true
                        }
                    })
                    if (firstOrNull() != Screen.Home) add(0, Screen.Home)
                }
            },
        ),
    ) {
        mutableStateListOf<Screen>(Screen.Home).apply { if (!settings.welcomeSeen) add(Screen.Welcome) }
    }

    fun open(screen: Screen) = backStack.add(screen)
    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    fun accentOf(unitId: String) = AppColors.unitAccent(units.indexOfFirst { it.id == unitId })

    BackHandler(enabled = backStack.size > 1) {
        // Backing out of the welcome tour counts as skipping it.
        if (backStack.last() == Screen.Welcome) settings.finishWelcome(settings.name)
        back()
    }


    when (val screen = backStack.last()) {
        Screen.Home -> HomeScreen(
            units = units,
            courseLabel = catalog.label,
            progress = progress,
            onOpenUnit = { open(Screen.UnitPage(it.id)) },
            onSearch = { open(Screen.Search) },
            onLibrary = { open(Screen.Library) },
            name = settings.name,
            onShowTour = { open(Screen.Welcome) },
            onQuickFive = { open(Screen.QuickFive) },
            onGames = { open(Screen.Games) },
            onPark = { open(Screen.Park) },
        )

        Screen.Park -> ParkScreen(
            allUnits = units,
            progress = progress,
            onBack = ::back,
            onOpenTopic = { unit, topic -> open(Screen.Topic(unit.id, topic.id)) },
        )

        Screen.QuickFive -> {
            val topics = remember(units) { units.flatMap { u -> u.topics.filter { it.mcqs.isNotEmpty() } } }
            QuizScreen(
                title = "Quick 5 ⚡",
                topics = topics,
                scoreKey = "quick5",
                limit = 5,
                progress = progress,
                onBack = ::back,
                quick = true,
                onGames = {
                    back()
                    if (backStack.last() != Screen.Games) open(Screen.Games)
                },
            )
        }

        Screen.Games -> GamesScreen(
            progress = progress,
            onBack = ::back,
            onQuickFive = { open(Screen.QuickFive) },
        )

        Screen.Welcome -> WelcomeScreen(
            initialName = settings.name,
            onDone = { name ->
                settings.finishWelcome(name)
                back()
            },
        )

        Screen.Library -> LibraryScreen(
            books = catalog.books,
            unitOrder = units.map { it.shortName },
            onBack = ::back,
            onOpen = { book, page -> open(Screen.Reader(book.id, page)) },
        )

        is Screen.Reader -> {
            val book = catalog.booksById.getValue(screen.bookId)
            ReaderScreen(book = book, startPage = screen.page, onBack = ::back)
        }

        Screen.Search -> SearchScreen(
            units = units,
            onBack = ::back,
            onOpenTopic = { unit, topic -> open(Screen.Topic(unit.id, topic.id)) },
        )

        is Screen.UnitPage -> {
            val unit = units.first { it.id == screen.unitId }
            UnitScreen(
                unit = unit,
                accent = accentOf(unit.id),
                progress = progress,
                onBack = ::back,
                onOpenTopic = { open(Screen.Topic(unit.id, it.id)) },
                onUnitQuiz = { open(Screen.Quiz(unit.id, null)) },
                onStudyAllCards = { open(Screen.Flashcards(unit.id, null)) },
            )
        }

        is Screen.Topic -> {
            val unit = units.first { it.id == screen.unitId }
            val topic = unit.topics.first { it.id == screen.topicId }
            TopicScreen(
                topic = topic,
                accent = accentOf(unit.id),
                progress = progress,
                books = catalog.booksById,
                onOpenBook = { book, page -> open(Screen.Reader(book.id, page)) },
                onBack = ::back,
                onQuiz = { open(Screen.Quiz(unit.id, topic.id)) },
                onStudyCards = { open(Screen.Flashcards(unit.id, topic.id)) },
            )
        }

        is Screen.Flashcards -> {
            val unit = units.first { it.id == screen.unitId }
            val topics = if (screen.topicId == null) {
                unit.topics
            } else {
                unit.topics.filter { it.id == screen.topicId }
            }
            val title = if (screen.topicId == null) "${unit.shortName} flashcards" else topics.first().title
            FlashcardScreen(
                title = title,
                topics = topics,
                progress = progress,
                onBack = ::back,
            )
        }

        is Screen.Quiz -> {
            val unit = units.first { it.id == screen.unitId }
            if (screen.topicId == null) {
                QuizScreen(
                    title = "${unit.shortName} quiz",
                    topics = unit.topics,
                    scoreKey = "unit:${unit.id}",
                    limit = 10,
                    progress = progress,
                    onBack = ::back,
                )
            } else {
                val topic = unit.topics.first { it.id == screen.topicId }
                QuizScreen(
                    title = topic.title,
                    topics = listOf(topic),
                    scoreKey = topic.id,
                    limit = topic.mcqs.size,
                    progress = progress,
                    onBack = ::back,
                )
            }
        }
    }
}
