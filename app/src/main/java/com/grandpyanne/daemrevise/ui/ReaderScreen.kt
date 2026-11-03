package com.grandpyanne.daemrevise.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Book
import com.grandpyanne.daemrevise.data.BookFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

// crafted by Grandpyanne™
/**
 * An open PDF. Android's PdfRenderer can only draw one page at a time, so every
 * call goes through one background thread, in order.
 */
private class PdfDoc(file: File) {
    private val executor = Executors.newSingleThreadExecutor()
    private val thread = executor.asCoroutineDispatcher()
    private val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(fd)
    private var closed = false

    val pageCount: Int = renderer.pageCount

    /** Draws page [index] (0-based) [widthPx] pixels wide on a white background. */
    suspend fun render(index: Int, widthPx: Int): Bitmap? = withContext(thread) {
        if (closed) return@withContext null
        val page = renderer.openPage(index)
        try {
            val height = (widthPx.toLong() * page.height / page.width).toInt().coerceAtLeast(1)
            Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888).also {
                it.eraseColor(AndroidColor.WHITE)
                page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        } finally {
            page.close()
        }
    }

    /** Closes the book after any page that is still drawing. */
    fun close() {
        executor.execute {
            closed = true
            renderer.close()
            fd.close()
        }
        executor.shutdown()
    }
}

private val ZOOMS = listOf(1f, 1.5f, 2f)

/** Reads a bundled textbook, opening at [startPage] (1-based, as printed in the Books tab). */
@Composable
fun ReaderScreen(book: Book, startPage: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    var doc by remember { mutableStateOf<PdfDoc?>(null) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(book.id) {
        try {
            val file = BookFiles.file(context, book)
            doc = withContext(Dispatchers.IO) { PdfDoc(file) }
        } catch (_: Exception) {
            failed = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { doc?.close() }
    }

    val open = doc
    when {
        failed -> Column(Modifier.fillMaxSize()) {
            ReaderBar(book.title, "Couldn't open this book", onBack)
            Text(
                "Something went wrong opening the PDF. Try again, and if it keeps happening, send feedback from the home screen.",
                modifier = Modifier.padding(20.dp),
            )
        }
        open == null -> Column(Modifier.fillMaxSize()) {
            ReaderBar(book.title, "Opening…", onBack)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Getting the book ready…", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "The first time takes a few seconds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> BookPages(book, open, startPage, onBack)
    }
}

@Composable
private fun BookPages(book: Book, doc: PdfDoc, startPage: Int, onBack: () -> Unit) {
    val list = rememberLazyListState(initialFirstVisibleItemIndex = (startPage - 1).coerceIn(0, doc.pageCount - 1))
    val scope = rememberCoroutineScope()
    var zoom by remember { mutableFloatStateOf(1f) }
    var askPage by remember { mutableStateOf(false) }
    val current = list.firstVisibleItemIndex + 1

    Column(Modifier.fillMaxSize()) {
        ReaderBar(
            title = book.title,
            subtitle = "Page $current of ${doc.pageCount}",
            onBack = onBack,
        ) {
            TextButton(onClick = { zoom = ZOOMS[(ZOOMS.indexOf(zoom) - 1).coerceAtLeast(0)] }, enabled = zoom > ZOOMS.first()) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = { zoom = ZOOMS[(ZOOMS.indexOf(zoom) + 1).coerceAtMost(ZOOMS.lastIndex)] }, enabled = zoom < ZOOMS.last()) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = { askPage = true }) { Text("Go to") }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            val pageWidth = maxWidth * zoom
            val density = LocalDensity.current
            // Sharp enough for the screen, but capped so big pages don't use too much memory.
            val renderPx = with(density) { pageWidth.roundToPx() }.coerceAtMost(2000)
            Box(Modifier.fillMaxSize().horizontalScroll(rememberScrollState(), enabled = zoom > 1f)) {
                LazyColumn(
                    state = list,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .width(pageWidth)
                        .fillMaxSize(),
                ) {
                    items(count = doc.pageCount, key = { it }) { index ->
                        PageImage(doc, index, renderPx)
                    }
                }
            }
        }
    }

    if (askPage) {
        GoToPageDialog(
            pageCount = doc.pageCount,
            onGo = { page ->
                askPage = false
                scope.launch { list.scrollToItem(page - 1) }
            },
            onDismiss = { askPage = false },
        )
    }
}

@Composable
private fun PageImage(doc: PdfDoc, index: Int, widthPx: Int) {
    val bitmap by produceState<Bitmap?>(initialValue = null, doc, index, widthPx) {
        value = try {
            doc.render(index, widthPx)
        } catch (_: Exception) {
            null
        }
    }
    val image = bitmap
    if (image == null) {
        // Placeholder the shape of an A4 page while the page draws.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1.414f)
                .background(Color.White),
        ) {
            Text("${index + 1}", color = Color.LightGray, style = MaterialTheme.typography.headlineMedium)
        }
    } else {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Page ${index + 1}",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(image.width.toFloat() / image.height),
        )
    }
}

@Composable
private fun ReaderBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        actions()
    }
}

@Composable
private fun GoToPageDialog(pageCount: Int, onGo: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val page = text.toIntOrNull()?.takeIf { it in 1..pageCount }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Go to page") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { new -> text = new.filter { it.isDigit() }.take(5) },
                label = { Text("Page (1 – $pageCount)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = { page?.let(onGo) }, enabled = page != null) { Text("Go") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
