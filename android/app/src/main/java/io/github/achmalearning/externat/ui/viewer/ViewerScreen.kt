package io.github.achmalearning.externat.ui.viewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.document
import io.github.achmalearning.externat.ui.AppViewModel
import io.github.achmalearning.externat.ui.common.FavoriteButton
import io.github.achmalearning.externat.ui.common.exceptBottom
import io.github.achmalearning.externat.ui.common.rememberDocumentActions

/** Visionneuse plein écran (guides, et PDF ouverts depuis les favoris ou l'historique). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    docKey: String,
    catalog: Catalog,
    user: UserState,
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val doc = catalog.document(docKey)
    val actions = rememberDocumentActions(catalog)
    LaunchedEffect(docKey) { if (doc != null) viewModel.addRecent(docKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                title = {
                    Column {
                        Text(doc?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (doc != null) {
                            Text(
                                doc.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = {
                    if (doc != null) {
                        FavoriteButton(docKey in user.favorites) { viewModel.toggleFavorite(docKey) }
                        IconButton(onClick = { actions.share(doc) }) {
                            Icon(painterResource(R.drawable.ic_share), stringResource(R.string.action_share))
                        }
                        IconButton(onClick = { actions.save(doc) }) {
                            Icon(painterResource(R.drawable.ic_save), stringResource(R.string.action_save))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (doc == null) {
            Text(stringResource(R.string.document_missing), Modifier.padding(padding))
        } else {
            PdfViewer(
                doc = doc,
                darkPages = user.darkPages,
                onOpenExternally = { actions.openExternally(doc) },
                modifier = Modifier.padding(padding.exceptBottom()),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = padding.calculateBottomPadding()),
            )
        }
    }
}
