package com.grandpyanne.daemrevise.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// crafted by Grandpyanne™
/** Small cache so scrolling back to a question doesn't decode its diagram again. */
private object FigureCache {
    private val cache = android.util.LruCache<String, ImageBitmap>(24)

    suspend fun load(context: Context, name: String): ImageBitmap? {
        cache.get(name)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("figures/$name").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            }.getOrNull()
        }?.also { cache.put(name, it) }
    }
}

@Composable
private fun rememberFigure(name: String): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, name) { value = FigureCache.load(context, name) }
    return bitmap
}

/**
 * The diagrams that go with a past-paper question, as printed in the KNEC paper.
 * They sit on white (like the paper) so they read the same in dark mode. Tap one to zoom.
 */
@Composable
fun QuestionFigures(files: List<String>, modifier: Modifier = Modifier) {
    if (files.isEmpty()) return
    var open by remember { mutableStateOf<String?>(null) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        files.forEach { name ->
            val bitmap = rememberFigure(name)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { open = name }
                    .padding(8.dp),
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Diagram from the past paper",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .aspectRatio(bitmap.width.toFloat() / bitmap.height, matchHeightConstraintsFirst = false),
                    )
                } else {
                    Box(Modifier.fillMaxWidth().height(120.dp))
                }
            }
        }
        Text(
            "Tap the diagram to zoom",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    open?.let { name -> FigureViewer(name, onClose = { open = null }) }
}

/** Full-screen diagram: pinch to zoom, drag to move, double-tap to reset. */
@Composable
private fun FigureViewer(name: String, onClose: () -> Unit) {
    val bitmap = rememberFigure(name)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset = if (scale == 1f) Offset.Zero else offset + pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { scale = 1f; offset = Offset.Zero })
                },
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Diagram from the past paper",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                )
            }
            TextButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Text("Close ✕", color = Color(0xFF1E3A5F))
            }
            Text(
                "Pinch to zoom · double-tap to reset",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
        }
    }
}
