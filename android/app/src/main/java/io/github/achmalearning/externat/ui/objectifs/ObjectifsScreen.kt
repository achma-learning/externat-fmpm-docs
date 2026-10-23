package io.github.achmalearning.externat.ui.objectifs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.ui.AppViewModel
import kotlinx.coroutines.launch

/** Sélection affichée dans le volet détail : « dossier|clé d'entrée|pdf ». */
private fun detailKey(dossier: String, entryKey: String?, pdf: Boolean) = "$dossier|${entryKey.orEmpty()}|${if (pdf) 1 else 0}"

/**
 * Module principal. Téléphone : liste puis détail plein écran. Tablette / paysage : liste à gauche,
 * checklist ou PDF à droite ([NavigableListDetailPaneScaffold] gère aussi le geste retour prédictif).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ObjectifsScreen(catalog: Catalog, user: UserState, viewModel: AppViewModel, onOpenSettings: () -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val request by viewModel.openRequest.collectAsStateWithLifecycle()

    LaunchedEffect(request) {
        request?.let {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, detailKey(it.dossier, it.entryKey, it.showPdf))
            viewModel.consumeOpenRequest(it)
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                SpecialtyList(
                    catalog = catalog,
                    user = user,
                    onFiltersChange = viewModel::setFilters,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onOpen = { dossier, entryKey ->
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, detailKey(dossier, entryKey, false)) }
                    },
                    onOpenSettings = onOpenSettings,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val key = navigator.currentDestination?.contentKey
                val parts = key?.split('|')
                val specialite = parts?.getOrNull(0)?.let { catalog.specialite(it) }
                if (specialite == null) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.detail_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    val listHidden = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden
                    SpecialtyDetail(
                        catalog = catalog,
                        specialite = specialite,
                        initialEntryKey = parts.getOrNull(1)?.takeIf { it.isNotEmpty() },
                        initialPdf = parts.getOrNull(2) == "1",
                        user = user,
                        viewModel = viewModel,
                        showBack = listHidden,
                        onBack = { scope.launch { navigator.navigateBack() } },
                    )
                }
            }
        },
    )
}
