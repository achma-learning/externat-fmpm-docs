package io.github.achmalearning.externat.ui.common

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.DocRef
import io.github.achmalearning.externat.data.PdfFiles
import io.github.achmalearning.externat.data.document
import kotlinx.coroutines.launch

/** Partager / Enregistrer / Ouvrir avec : aucune permission nécessaire (FileProvider + SAF). */
class DocumentActions(
    val share: (DocRef) -> Unit,
    val save: (DocRef) -> Unit,
    val openExternally: (DocRef) -> Unit,
)

@Composable
fun rememberDocumentActions(catalog: Catalog): DocumentActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chooserTitle = stringResource(R.string.share_chooser)
    // Clé du document en attente d'enregistrement (survit à la rotation pendant le sélecteur de fichier).
    var pendingSave by rememberSaveable { mutableStateOf<String?>(null) }

    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val doc = pendingSave?.let { catalog.document(it) }
        pendingSave = null
        if (uri != null && doc != null) {
            scope.launch {
                val ok = runCatching { PdfFiles.saveTo(context, doc, uri) }.isSuccess
                Toast.makeText(context, if (ok) R.string.save_done else R.string.save_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    return remember(catalog, createDocument, chooserTitle) {
        DocumentActions(
            share = { doc ->
                scope.launch {
                    runCatching { PdfFiles.share(context, doc, chooserTitle) }
                        .onFailure { Toast.makeText(context, R.string.share_failed, Toast.LENGTH_SHORT).show() }
                }
            },
            save = { doc ->
                pendingSave = doc.key
                createDocument.launch(doc.shareName)
            },
            openExternally = { doc ->
                scope.launch {
                    if (!PdfFiles.openExternally(context, doc)) {
                        Toast.makeText(context, R.string.no_pdf_app, Toast.LENGTH_LONG).show()
                    }
                }
            },
        )
    }
}
