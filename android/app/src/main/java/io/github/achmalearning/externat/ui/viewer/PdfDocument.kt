package io.github.achmalearning.externat.ui.viewer

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Taille d'une page en points PDF. */
data class PageSize(val width: Int, val height: Int) {
    val aspect: Float get() = if (height == 0) 0.707f else width.toFloat() / height
}

/**
 * Document ouvert avec le [PdfRenderer] de la plateforme. PdfRenderer n'accepte qu'une page ouverte
 * à la fois et n'est pas thread-safe : tout passe par un [Mutex] et un dispatcher à un seul thread.
 */
class PdfDocument private constructor(
    private val fd: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
    val pageSizes: List<PageSize>,
) {
    private val mutex = Mutex()
    private var closed = false

    val pageCount: Int get() = pageSizes.size

    /** Rend la page [index] à la largeur [widthPx]. Renvoie null si le document a été fermé. */
    suspend fun render(index: Int, widthPx: Int): Bitmap? = withContext(renderDispatcher) {
        mutex.withLock {
            if (closed) return@withLock null
            val page = renderer.openPage(index)
            try {
                val heightPx = (widthPx / pageSizes[index].aspect).toInt().coerceAtLeast(1)
                val bitmap = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE) // les PDF ont un fond transparent
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } finally {
                page.close()
            }
        }
    }

    suspend fun close() = withContext(renderDispatcher) {
        mutex.withLock {
            if (!closed) {
                closed = true
                renderer.close()
                fd.close()
            }
        }
    }

    companion object {
        @OptIn(ExperimentalCoroutinesApi::class)
        private val renderDispatcher = Dispatchers.IO.limitedParallelism(1)

        /** Ouvre le fichier et lit la taille de chaque page (rapide : aucune page n'est rendue). */
        suspend fun open(file: File): PdfDocument = withContext(renderDispatcher) {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            try {
                val renderer = PdfRenderer(fd)
                val sizes = (0 until renderer.pageCount).map { i ->
                    val page = renderer.openPage(i)
                    try {
                        PageSize(page.width, page.height)
                    } finally {
                        page.close()
                    }
                }
                PdfDocument(fd, renderer, sizes)
            } catch (e: Exception) {
                fd.close()
                throw e
            }
        }
    }
}

/**
 * Cache LRU des pages rendues, dimensionné sur la mémoire disponible. Une page évincée est recyclée
 * seulement si elle n'est plus affichée (sinon le GC s'en charge une fois l'écran mis à jour).
 */
class PageCache(maxBytes: Int) {
    private val inUse = HashSet<String>()

    private val cache = object : LruCache<String, Bitmap>(maxBytes) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            val displayed = synchronized(inUse) { key in inUse }
            if (!displayed && oldValue !== newValue) oldValue.recycle()
        }
    }

    val maxSize: Int get() = cache.maxSize()

    fun get(key: String): Bitmap? = cache.get(key)?.takeUnless { it.isRecycled }

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun acquire(key: String) = synchronized(inUse) { inUse += key }
    fun release(key: String) = synchronized(inUse) { inUse -= key }

    fun clear() {
        synchronized(inUse) { inUse.clear() }
        cache.evictAll()
    }

    companion object {
        /** Un quart de la mémoire de l'app, au moins 24 Mo. */
        fun forRuntime(): PageCache {
            val max = Runtime.getRuntime().maxMemory() / 4
            return PageCache(max.coerceIn(24L * 1024 * 1024, Int.MAX_VALUE.toLong()).toInt())
        }
    }
}
