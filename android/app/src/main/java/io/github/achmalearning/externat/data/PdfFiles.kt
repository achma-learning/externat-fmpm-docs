package io.github.achmalearning.externat.data

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.github.achmalearning.externat.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Fichiers PDF : PdfRenderer a besoin d'un descripteur « seekable », donc chaque asset est copié
 * une fois dans filesDir/pdf/v<versionCode>/… puis réutilisé. Les copies d'une version précédente
 * de l'app sont supprimées (le contenu a pu changer avec la mise à jour).
 */
object PdfFiles {
    private val mutex = Mutex()
    private const val PDF_MIME = "application/pdf"

    private fun authority(context: Context) = "${context.packageName}.files"
    private fun pdfRoot(context: Context) = File(context.filesDir, "pdf")
    private fun versionDir(context: Context) = File(pdfRoot(context), "v${BuildConfig.VERSION_CODE}")

    suspend fun ensureLocal(context: Context, assetPath: String): File = withContext(Dispatchers.IO) {
        mutex.withLock {
            val target = File(versionDir(context), assetPath)
            if (!target.isFile) {
                target.parentFile?.mkdirs()
                val tmp = File(target.parentFile, target.name + ".tmp")
                context.assets.open(assetPath).use { input -> tmp.outputStream().use { input.copyTo(it) } }
                check(tmp.renameTo(target)) { "Impossible d'enregistrer $assetPath" }
            }
            target
        }
    }

    /** Supprime les copies faites par une version précédente de l'app. */
    suspend fun cleanupOldVersions(context: Context) = withContext(Dispatchers.IO) {
        val keep = versionDir(context).name
        pdfRoot(context).listFiles()?.filter { it.name != keep }?.forEach { it.deleteRecursively() }
    }

    /** Partage via la feuille Android (WhatsApp, e-mail…) avec un nom de fichier lisible. */
    suspend fun share(context: Context, doc: DocRef, chooserTitle: String) {
        val source = ensureLocal(context, doc.assetPath)
        val named = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "share").apply { deleteRecursively(); mkdirs() }
            source.copyTo(File(dir, doc.shareName), overwrite = true)
        }
        val uri = FileProvider.getUriForFile(context, authority(context), named)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = PDF_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, doc.shareName.removeSuffix(".pdf"))
            putExtra(Intent.EXTRA_TEXT, doc.shareText)
            clipData = ClipData.newRawUri(doc.shareName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Copie le PDF vers l'emplacement choisi par l'utilisateur (Storage Access Framework). */
    suspend fun saveTo(context: Context, doc: DocRef, destination: Uri) {
        val source = ensureLocal(context, doc.assetPath)
        withContext(Dispatchers.IO) {
            val out = context.contentResolver.openOutputStream(destination) ?: error("Destination indisponible")
            out.use { stream -> source.inputStream().use { it.copyTo(stream) } }
        }
    }

    /** Ouvre le PDF dans une autre application. Renvoie false si aucune ne sait l'ouvrir. */
    suspend fun openExternally(context: Context, doc: DocRef): Boolean {
        val file = ensureLocal(context, doc.assetPath)
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, PDF_MIME)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(view)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
