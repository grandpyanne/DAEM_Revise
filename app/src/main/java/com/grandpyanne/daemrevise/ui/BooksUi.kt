package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grandpyanne.daemrevise.data.Book
import com.grandpyanne.daemrevise.data.BookRef

/** The "Books" tab on a topic: where to read it in the textbooks, opening right at the page. */
@Composable
fun TopicBooks(
    refs: List<BookRef>,
    books: Map<String, Book>,
    accent: Color,
    onOpen: (book: Book, page: Int) -> Unit,
) {
    // One group per book, in the order the refs list them.
    val groups = refs.groupBy { it.bookId }.mapNotNull { (id, list) -> books[id]?.let { it to list } }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (groups.isEmpty()) {
            Text(
                "None of the textbooks in the app cover this topic. Your lecturer's notes (Notes tab) are the best source here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "Try it the textbook way: read the section, cover the worked example, attempt it yourself, then compare.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            groups.forEach { (book, bookRefs) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📘 ${book.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        bookRefs.forEach { ref ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent.copy(alpha = 0.08f))
                                    .clickable { onOpen(book, ref.page.coerceAtLeast(1)) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(ref.where, style = MaterialTheme.typography.bodyMedium)
                                    if (ref.page > 0) {
                                        Text("Page ${ref.page}", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                FilledTonalButton(
                                    onClick = { onOpen(book, ref.page.coerceAtLeast(1)) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                ) { Text("Read") }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Every textbook in the app, grouped under the unit it belongs to. A book used by two units shows in both. */
@Composable
fun LibraryScreen(books: List<Book>, unitOrder: List<String>, onBack: () -> Unit, onOpen: (Book, Int) -> Unit) {
    // Units in the same order as on the home screen.
    val mentioned = books.flatMap { it.units }.toSet()
    val units = unitOrder.filter { it in mentioned } + mentioned.filter { it !in unitOrder }
    val others = books.filter { it.units.isEmpty() }

    Column(Modifier.fillMaxSize()) {
        Header(title = "Textbooks", subtitle = "${books.size} books · read them right here, no internet needed", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            item {
                Text(
                    "Tip: a topic's Books tab opens the book at the exact page for that topic.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            units.forEach { unitName ->
                item(key = "unit:$unitName") { UnitHeading(unitName) }
                items(books.filter { unitName in it.units }, key = { "$unitName:${it.id}" }) { book ->
                    // A book shared by several units opens at this unit's section; others open at the start.
                    val section = if (book.units.size > 1) book.opens[unitName] else null
                    BookCard(book, section, onOpen = { onOpen(book, section?.page ?: 1) })
                }
            }
            if (others.isNotEmpty()) {
                item(key = "unit:other") { UnitHeading("Other") }
                items(others, key = { "other:${it.id}" }) { book -> BookCard(book, null, onOpen = { onOpen(book, 1) }) }
            }
        }
    }
}

// Grandpyanne™ was here
@Composable
private fun UnitHeading(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun BookCard(book: Book, section: BookRef?, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📘", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (section != null) {
                    Text(
                        "Opens at: ${section.where} · page ${section.page}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (book.units.size > 1) {
                    Text(
                        "Used in: " + book.units.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
