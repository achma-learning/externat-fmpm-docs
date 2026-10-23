package io.github.achmalearning.externat.ui.objectifs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.DocKeys
import io.github.achmalearning.externat.data.IndexEntry
import io.github.achmalearning.externat.data.ObjectifHit
import io.github.achmalearning.externat.data.Specialite
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.VersionFilter
import io.github.achmalearning.externat.data.YEARS
import io.github.achmalearning.externat.data.filter
import io.github.achmalearning.externat.data.matchesName
import io.github.achmalearning.externat.data.progressOf
import io.github.achmalearning.externat.data.searchObjectifs
import io.github.achmalearning.externat.data.shortLabel
import io.github.achmalearning.externat.ui.common.FavoriteButton
import io.github.achmalearning.externat.ui.common.exceptBottom
import io.github.achmalearning.externat.ui.common.ProgressRow
import io.github.achmalearning.externat.ui.common.highlighted

private const val SEARCH_LIMIT = 200

/** Liste des spécialités par catégorie, filtres année/version et recherche (nom + plein texte). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpecialtyList(
    catalog: Catalog,
    user: UserState,
    onFiltersChange: (Int?, VersionFilter) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpen: (dossier: String, entryKey: String?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val year = user.yearFilter
    val version = user.versionFilter
    val focus = LocalFocusManager.current

    val visible = remember(catalog, year, version) { catalog.filter(year, version) }
    val byName = remember(visible, query) { visible.filter { it.matchesName(query) } }
    val hits = remember(catalog, query, year, version) {
        if (query.isBlank()) emptyList() else catalog.searchObjectifs(query, year, version, SEARCH_LIMIT)
    }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_objectifs)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding.exceptBottom()),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(painterResource(R.drawable.ic_close), stringResource(R.string.search_clear))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                )
            }
            item(key = "filters") { Filters(year, version, onFiltersChange) }

            if (query.isBlank()) {
                catalog.categories.forEach { categorie ->
                    val inCategory = visible.filter { it.categorie == categorie }
                    if (inCategory.isNotEmpty()) {
                        item(key = "cat:$categorie") { SectionHeader(categorie) }
                        items(inCategory, key = { "s:${it.dossier}" }) { s ->
                            SpecialtyCard(catalog, s, user, onToggleFavorite, onOpen)
                        }
                    }
                }
            } else {
                if (byName.isNotEmpty()) {
                    item(key = "h:names") { SectionHeader(stringResource(R.string.section_specialites)) }
                    items(byName, key = { "n:${it.dossier}" }) { s -> SpecialtyCard(catalog, s, user, onToggleFavorite, onOpen) }
                }
                if (hits.isNotEmpty()) {
                    item(key = "h:hits") { SectionHeader(stringResource(R.string.section_in_objectifs, hits.size)) }
                    items(hits, key = { "o:${it.objectif.id}" }) { hit -> HitCard(hit) { onOpen(hit.entry.dossier, hit.entry.key) } }
                    if (hits.size >= SEARCH_LIMIT) {
                        item(key = "more") { Text(stringResource(R.string.section_in_objectifs_more), style = MaterialTheme.typography.bodySmall) }
                    }
                }
                if (byName.isEmpty() && hits.isEmpty()) {
                    item(key = "empty") {
                        Text(stringResource(R.string.no_result), Modifier.padding(vertical = 24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Filters(year: Int?, version: VersionFilter, onChange: (Int?, VersionFilter) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.filter_year), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = year == null, onClick = { onChange(null, version) },
                label = { Text(stringResource(R.string.filter_all_years)) })
            YEARS.forEach { y ->
                FilterChip(selected = year == y, onClick = { onChange(y, version) }, label = { Text("${y}A") })
            }
        }
        Text(stringResource(R.string.filter_version), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                VersionFilter.BOTH to R.string.filter_both,
                VersionFilter.V2022 to R.string.filter_2022,
                VersionFilter.OLD to R.string.filter_old,
            ).forEach { (v, label) ->
                FilterChip(selected = version == v, onClick = { onChange(year, v) }, label = { Text(stringResource(label)) })
            }
        }
        HorizontalDivider(Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).semantics { heading() },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialtyCard(
    catalog: Catalog,
    specialite: Specialite,
    user: UserState,
    onToggleFavorite: (String) -> Unit,
    onOpen: (String, String?) -> Unit,
) {
    val favKey = DocKeys.specialite(specialite.dossier)
    val progress = remember(specialite, user.checked) { catalog.progressOf(specialite.entries, user.checked) }
    Card(
        onClick = { onOpen(specialite.dossier, null) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(specialite.nom, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                FavoriteButton(favKey in user.favorites) { onToggleFavorite(favKey) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(end = 12.dp)) {
                specialite.entries.forEach { entry -> EntryChip(entry) { onOpen(specialite.dossier, entry.key) } }
            }
            if (progress.total > 0) ProgressRow(progress, Modifier.padding(top = 4.dp, end = 12.dp))
        }
    }
}

@Composable
private fun EntryChip(entry: IndexEntry, onClick: () -> Unit) {
    val label = entry.shortLabel()
    val description = stringResource(R.string.open_entry, label)
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.semantics { contentDescription = description },
    )
}

@Composable
private fun HitCard(hit: ObjectifHit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${hit.entry.nom} — ${hit.entry.shortLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            hit.objectif.rubrique?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(highlighted(hit.objectif.texte, hit.highlights), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
