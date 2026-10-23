package io.github.achmalearning.externat.ui.guides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.DocKeys
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.document
import io.github.achmalearning.externat.ui.AppViewModel
import io.github.achmalearning.externat.ui.common.FavoriteButton
import io.github.achmalearning.externat.ui.common.exceptBottom
import io.github.achmalearning.externat.ui.common.rememberDocumentActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidesScreen(
    catalog: Catalog,
    user: UserState,
    viewModel: AppViewModel,
    onOpen: (docKey: String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val actions = rememberDocumentActions(catalog)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_guides)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize().padding(padding.exceptBottom()),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.guides_intro), style = MaterialTheme.typography.bodyMedium)
            }
            items(catalog.guides, key = { it.id }) { guide ->
                val key = DocKeys.guide(guide)
                val doc = catalog.document(key)
                ElevatedCard(onClick = { onOpen(key) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 4.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                painterResource(R.drawable.ic_book),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp, end = 12.dp),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(guide.titre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    guide.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            FavoriteButton(key in user.favorites) { viewModel.toggleFavorite(key) }
                            if (doc != null) {
                                IconButton(onClick = { actions.share(doc) }) {
                                    Icon(painterResource(R.drawable.ic_share), stringResource(R.string.action_share))
                                }
                                IconButton(onClick = { actions.save(doc) }) {
                                    Icon(painterResource(R.drawable.ic_save), stringResource(R.string.action_save))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
