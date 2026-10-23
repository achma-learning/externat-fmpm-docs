package io.github.achmalearning.externat.ui.favoris

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.shortLabel
import io.github.achmalearning.externat.ui.AppViewModel
import io.github.achmalearning.externat.ui.common.FavoriteButton
import io.github.achmalearning.externat.ui.common.exceptBottom
import java.text.DateFormat
import java.util.Date

/** Une ligne affichable pour une clé de favori / d'historique (`s:`, `e:`, `g:`). */
private data class Item(val key: String, val title: String, val subtitle: String, val icon: Int)

private fun Catalog.describe(key: String, specialiteLabel: String, guideLabel: String): Item? = when {
    key.startsWith("s:") -> specialite(key.removePrefix("s:"))?.let {
        Item(key, it.nom, "$specialiteLabel · ${it.entries.joinToString { e -> e.shortLabel() }}", R.drawable.ic_checklist)
    }
    key.startsWith("e:") -> entry(key.removePrefix("e:"))?.let {
        Item(key, it.nom, it.shortLabel(), R.drawable.ic_document)
    }
    key.startsWith("g:") -> guide(key.removePrefix("g:"))?.let { Item(key, it.titre, guideLabel, R.drawable.ic_book) }
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavorisScreen(
    catalog: Catalog,
    user: UserState,
    viewModel: AppViewModel,
    onOpenKey: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val specialiteLabel = stringResource(R.string.kind_specialite)
    val guideLabel = stringResource(R.string.kind_guide)
    val favorites = remember(catalog, user.favorites) {
        user.favorites.mapNotNull { catalog.describe(it, specialiteLabel, guideLabel) }.sortedBy { it.title }
    }
    val recents = remember(catalog, user.recents) {
        user.recents.mapNotNull { r -> catalog.describe(r.key, specialiteLabel, guideLabel)?.let { it to r.openedAt } }
    }
    var confirmClear by remember { mutableStateOf(false) }
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_favoris)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding.exceptBottom()),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "h:fav") { Header(stringResource(R.string.section_favorites)) }
            if (favorites.isEmpty()) {
                item(key = "nofav") { Hint(stringResource(R.string.no_favorites)) }
            }
            items(favorites, key = { "f:${it.key}" }) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.subtitle) },
                    leadingContent = { Icon(painterResource(item.icon), null) },
                    trailingContent = { FavoriteButton(true) { viewModel.toggleFavorite(item.key) } },
                    modifier = Modifier.clickable { onOpenKey(item.key) },
                )
            }
            item(key = "h:rec") { Header(stringResource(R.string.section_recents)) }
            if (recents.isEmpty()) {
                item(key = "norec") { Hint(stringResource(R.string.no_recents)) }
            } else {
                items(recents, key = { "r:${it.first.key}" }) { (item, at) ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text("${item.subtitle} · ${dateFormat.format(Date(at))}") },
                        leadingContent = { Icon(painterResource(R.drawable.ic_history), null) },
                        modifier = Modifier.clickable { onOpenKey(item.key) },
                    )
                }
                item(key = "clear") {
                    TextButton(onClick = { confirmClear = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(painterResource(R.drawable.ic_delete), null, Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.clear_history))
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_history)) },
            text = { Text(stringResource(R.string.clear_history_message)) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; viewModel.clearRecents() }) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun Header(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
