package com.grandpyanne.daemrevise.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Grandpyanne™
/**
 * The textbooks ship inside the app (assets/books). Android's PDF renderer can only read
 * a real file, so the first time a book is opened it is copied to the app's private
 * storage. To save space, only the book opened most recently is kept; opening a
 * different book takes a few seconds the first time.
 */
object BookFiles {

    suspend fun file(context: Context, book: Book): File = withContext(Dispatchers.IO) {
        val target = File(File(context.filesDir, "books"), book.file.substringAfterLast('/'))
        val expected = assetSize(context, book.file)
        if (!target.exists() || (expected > 0 && target.length() != expected)) {
            target.parentFile?.mkdirs()
            val partial = File(target.path + ".part")
            context.assets.open(book.file).use { input ->
                partial.outputStream().use { output -> input.copyTo(output, 256 * 1024) }
            }
            if (!partial.renameTo(target)) {
                partial.copyTo(target, overwrite = true)
                partial.delete()
            }
        }
        // Keep just this one copy: remove other books copied earlier (and any half-finished copies).
        target.parentFile?.listFiles()?.forEach { other ->
            if (other.name != target.name) other.delete()
        }
        target
    }

    /** Size of the bundled PDF, or -1 if Android can't tell (then any existing copy is trusted). */
    private fun assetSize(context: Context, path: String): Long = try {
        context.assets.openFd(path).use { it.length }
    } catch (_: Exception) {
        -1L
    }
}
