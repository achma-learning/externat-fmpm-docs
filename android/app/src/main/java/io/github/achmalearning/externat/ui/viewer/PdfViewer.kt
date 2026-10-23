package io.github.achmalearning.externat.ui.viewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.DocRef
import io.github.achmalearning.externat.data.PdfFiles
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val MAX_ZOOM = 4f
private const val DOUBLE_TAP_ZOOM = 2.5f

private sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val document: PdfDocument) : LoadState
    data class Failed(val error: Throwable) : LoadState
}

/** Inverse les couleurs (option « pages sombres » en thème sombre). */
private val InvertFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f,
        )
    )
)

/**
 * Visionneuse PDF : pages dans une LazyColumn, rendues en arrière-plan à la largeur affichée × densité
 * (puis re-rendues plus fines quand on zoome), pincer pour zoomer, double tap, indicateur et saut de page.
 */
@Composable
fun PdfViewer(
    doc: DocRef,
    darkPages: Boolean,
    onOpenExternally: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    var state by remember(doc.assetPath) { mutableStateOf<LoadState>(LoadState.Loading) }

    LaunchedEffect(doc.assetPath) {
        state = try {
            LoadState.Ready(PdfDocument.open(PdfFiles.ensureLocal(context, doc.assetPath)))
        } catch (e: Exception) {
            LoadState.Failed(e)
        }
    }
    val ready = state as? LoadState.Ready
    DisposableEffect(ready) {
        onDispose { ready?.let { r -> kotlinx.coroutines.MainScope().launch { r.document.close() } } }
    }

    when (val s = state) {
        LoadState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is LoadState.Failed -> ViewerError(onOpenExternally, modifier)
        is LoadState.Ready -> PdfPages(s.document, doc.key, darkPages, modifier, contentPadding)
    }
}

@Composable
private fun ViewerError(onOpenExternally: () -> Unit, modifier: Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.viewer_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenExternally) { Text(stringResource(R.string.action_open_with)) }
    }
}

@Composable
private fun PdfPages(
    document: PdfDocument,
    docKey: String,
    darkPages: Boolean,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    val cache = remember(document) { PageCache.forRuntime() }
    DisposableEffect(cache) { onDispose { cache.clear() } }

    val listState = rememberSaveable(docKey, saver = LazyListState.Saver) { LazyListState() }
    var scale by rememberSaveable(docKey) { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable(docKey) { mutableFloatStateOf(0f) }
    var showJump by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val inverted = darkPages && io.github.achmalearning.externat.ui.theme.LocalDarkTheme.current

    BoxWithConstraints(modifier.fillMaxSize().clipToBounds().background(MaterialTheme.colorScheme.surfaceVariant)) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        fun clampOffset(value: Float, s: Float): Float {
            val limit = (s - 1f) * widthPx / 2f
            return value.coerceIn(-limit, limit)
        }

        fun zoomTo(newScale: Float, centroid: Offset) {
            val s0 = scale
            val s1 = newScale.coerceIn(1f, MAX_ZOOM)
            if (s1 == s0) return
            // Garde le point sous les doigts immobile : axe vertical via le scroll, horizontal via offsetX.
            listState.dispatchRawDelta(centroid.y / s0 - centroid.y / s1)
            val contentX = (centroid.x - offsetX - widthPx / 2f) / s0 + widthPx / 2f
            offsetX = clampOffset(centroid.x - widthPx / 2f - (contentX - widthPx / 2f) * s1, s1)
            scale = s1
        }

        // Le zoom s'applique à toute la colonne. Les détecteurs ci-dessous sont hors du graphicsLayer
        // (coordonnées écran) ; le scroll interne de la LazyColumn est dedans (coordonnées non zoomées),
        // donc le défilement suit le doigt quel que soit le zoom.
        // La marge du bas compense la partie de la colonne masquée par l'agrandissement.
        val bottomExtra = with(density) { (heightPx * (1f - 1f / scale)).toDp() }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pressed = event.changes.count { it.pressed }
                            if (pressed >= 2) {
                                val centroid = event.calculateCentroid(useCurrent = true)
                                zoomTo(scale * event.calculateZoom(), centroid)
                                val pan = event.calculatePan()
                                offsetX = clampOffset(offsetX + pan.x, scale)
                                listState.dispatchRawDelta(-pan.y / scale)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            } else if (pressed == 1 && scale > 1f) {
                                val dx = event.changes.first().positionChange().x
                                offsetX = clampOffset(offsetX + dx, scale)
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tap ->
                        if (scale > 1.05f) {
                            zoomTo(1f, tap)
                            offsetX = 0f
                        } else {
                            zoomTo(DOUBLE_TAP_ZOOM, tap)
                        }
                    })
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    transformOrigin = TransformOrigin(0.5f, 0f)
                },
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 72.dp + bottomExtra,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(document.pageSizes, key = { i, _ -> i }) { index, size ->
                PdfPage(document, cache, index, size, scale, inverted, total = document.pageCount)
            }
        }

        val currentPage by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }
        Surface(
            onClick = { showJump = true },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp)
                .semantics { contentDescription = "Page $currentPage sur ${document.pageCount}. Aller à une page" },
        ) {
            Text(
                "$currentPage / ${document.pageCount}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }

    if (showJump) {
        JumpToPageDialog(
            pageCount = document.pageCount,
            onDismiss = { showJump = false },
            onJump = { page ->
                showJump = false
                scope.launch { listState.scrollToItem(page - 1) }
            },
        )
    }
}

@Composable
private fun PdfPage(
    document: PdfDocument,
    cache: PageCache,
    index: Int,
    size: PageSize,
    scale: Float,
    inverted: Boolean,
    total: Int,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .aspectRatio(size.aspect)
            .background(androidx.compose.ui.graphics.Color.White)
            .semantics { contentDescription = "Page ${index + 1} sur $total" },
    ) {
        val baseWidth = constraints.maxWidth
        // Paliers de zoom pour ne pas re-rendre à chaque pixel de pincement.
        val bucket = when {
            scale <= 1.25f -> 1f
            scale <= 2.25f -> 2f
            else -> 3f
        }
        // Une page ne prend jamais plus d'un tiers du cache.
        val maxPixels = cache.maxSize / 3 / 4
        val memoryWidth = sqrt(maxPixels * size.aspect).toInt()
        val renderWidth = max(1, min((baseWidth * bucket).toInt(), memoryWidth))
        val cacheKey = "$index@$renderWidth"

        var bitmap by remember { mutableStateOf<Pair<String, Bitmap>?>(null) }
        LaunchedEffect(cacheKey) {
            val cached = cache.get(cacheKey)
            val rendered = cached ?: document.render(index, renderWidth)?.also { cache.put(cacheKey, it) }
            if (rendered != null) bitmap = cacheKey to rendered
        }
        val shown = bitmap
        if (shown != null && !shown.second.isRecycled) {
            DisposableEffect(shown.first) {
                cache.acquire(shown.first)
                onDispose { cache.release(shown.first) }
            }
            Image(
                bitmap = shown.second.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (inverted) InvertFilter else null,
            )
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun JumpToPageDialog(pageCount: Int, onDismiss: () -> Unit, onJump: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val page = text.toIntOrNull()?.takeIf { it in 1..pageCount }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.viewer_jump_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() }.take(4) },
                label = { Text(stringResource(R.string.viewer_jump_label, pageCount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = text.isNotEmpty() && page == null,
            )
        },
        confirmButton = {
            TextButton(onClick = { page?.let(onJump) }, enabled = page != null) {
                Text(stringResource(R.string.viewer_jump_go))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
