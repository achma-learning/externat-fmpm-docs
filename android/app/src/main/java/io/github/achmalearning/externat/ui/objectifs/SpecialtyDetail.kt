package io.github.achmalearning.externat.ui.objectifs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.DocKeys
import io.github.achmalearning.externat.data.IndexEntry
import io.github.achmalearning.externat.data.Objectif
import io.github.achmalearning.externat.data.Specialite
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.document
import io.github.achmalearning.externat.data.progressOf
import io.github.achmalearning.externat.data.shortLabel
import io.github.achmalearning.externat.data.sourceLabel
import io.github.achmalearning.externat.ui.AppViewModel
import io.github.achmalearning.externat.ui.common.FavoriteButton
import io.github.achmalearning.externat.ui.common.exceptBottom
import io.github.achmalearning.externat.ui.common.ProgressRow
import io.github.achmalearning.externat.ui.common.rememberDocumentActions
import io.github.achmalearning.externat.ui.viewer.PdfViewer

/** Écran d'une spécialité : un onglet par (année, version), vue « Objectifs » (checklist) ou « PDF ». */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtyDetail(
    catalog: Catalog,
    specialite: Specialite,
    initialEntryKey: String?,
    initialPdf: Boolean,
    user: UserState,
    viewModel: AppViewModel,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    val entries = specialite.entries
    val initialIndex = entries.indexOfFirst { it.key == initialEntryKey }.takeIf { it >= 0 }
        ?: entries.indexOfFirst { it.annees.contains(user.yearFilter) && user.versionFilter.matches(it.version) }
            .takeIf { it >= 0 }
        ?: 0
    var selected by rememberSaveable(specialite.dossier, initialEntryKey) { mutableIntStateOf(initialIndex) }
    var showPdf by rememberSaveable(specialite.dossier, initialEntryKey) { mutableStateOf(initialPdf) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmUncheck by remember { mutableStateOf(false) }

    val entry = entries[selected.coerceIn(entries.indices)]
    val objectifs = remember(entry) { catalog.objectifs(entry) }
    val doc = remember(entry) { catalog.document(DocKeys.entry(entry)) }
    val actions = rememberDocumentActions(catalog)
    val entryFav = DocKeys.entry(entry)

    LaunchedEffect(entry.key) { viewModel.addRecent(entryFav) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back))
                        }
                    }
                },
                title = { Text(specialite.nom, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                actions = {
                    FavoriteButton(entryFav in user.favorites) { viewModel.toggleFavorite(entryFav) }
                    if (doc != null) {
                        IconButton(onClick = { actions.share(doc) }) {
                            Icon(painterResource(R.drawable.ic_share), stringResource(R.string.action_share))
                        }
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(painterResource(R.drawable.ic_more_vert), stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (doc != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_save)) },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_save), null) },
                                onClick = { menuOpen = false; actions.save(doc) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_open_with)) },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_open_in_new), null) },
                                onClick = { menuOpen = false; actions.openExternally(doc) },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.uncheck_all)) },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_delete), null) },
                            enabled = objectifs.any { it.id in user.checked },
                            onClick = { menuOpen = false; confirmUncheck = true },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding.exceptBottom())) {
            if (entries.size > 1) {
                PrimaryScrollableTabRow(selectedTabIndex = selected, edgePadding = 16.dp) {
                    entries.forEachIndexed { i, e ->
                        Tab(selected = i == selected, onClick = { selected = i }, text = { Text(e.shortLabel()) })
                    }
                }
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                SegmentedButton(
                    selected = !showPdf,
                    onClick = { showPdf = false },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { SegmentedButtonDefaults.Icon(!showPdf) { Icon(painterResource(R.drawable.ic_checklist), null) } },
                ) { Text(stringResource(R.string.tab_objectifs)) }
                SegmentedButton(
                    selected = showPdf,
                    onClick = { showPdf = true },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { SegmentedButtonDefaults.Icon(showPdf) { Icon(painterResource(R.drawable.ic_document), null) } },
                ) { Text(stringResource(R.string.tab_pdf)) }
            }
            if (showPdf && doc != null) {
                PdfViewer(
                    doc = doc,
                    darkPages = user.darkPages,
                    onOpenExternally = { actions.openExternally(doc) },
                    contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
                )
            } else {
                Checklist(
                    entry = entry,
                    objectifs = objectifs,
                    checked = user.checked,
                    bottomPadding = padding.calculateBottomPadding(),
                    onCheck = viewModel::setChecked,
                    onShowPdf = { showPdf = true },
                )
            }
        }
    }

    if (confirmUncheck) {
        val count = objectifs.count { it.id in user.checked }
        AlertDialog(
            onDismissRequest = { confirmUncheck = false },
            title = { Text(stringResource(R.string.uncheck_all_title)) },
            text = { Text(pluralStringResource(R.plurals.uncheck_all_message, count, count, "${specialite.nom} — ${entry.shortLabel()}")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmUncheck = false
                    viewModel.uncheckAll(objectifs)
                }) { Text(stringResource(R.string.uncheck_all_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmUncheck = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun Checklist(
    entry: IndexEntry,
    objectifs: List<Objectif>,
    checked: Set<String>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onCheck: (Objectif, Boolean) -> Unit,
    onShowPdf: () -> Unit,
) {
    val progress = remember(objectifs, checked) { progressOf(objectifs, checked) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding + 24.dp),
    ) {
        item(key = "header") {
            Card(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(entry.sourceLabel(), style = MaterialTheme.typography.labelLarge)
                    entry.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    if (progress.total > 0) ProgressRow(progress)
                }
            }
        }
        if (objectifs.isEmpty()) {
            item(key = "empty") {
                Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.no_objectifs))
                    Button(onClick = onShowPdf) { Text(stringResource(R.string.see_pdf)) }
                }
            }
        }
        itemsIndexed(objectifs, key = { _, o -> o.id }) { i, objectif ->
            val rubrique = objectif.rubrique
            if (rubrique != null && rubrique != objectifs.getOrNull(i - 1)?.rubrique) {
                Text(
                    rubrique,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() },
                )
            }
            val isChecked = objectif.id in checked
            Row(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = { onCheck(objectif, it) })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = isChecked, onCheckedChange = null, modifier = Modifier.padding(end = 12.dp))
                Text(
                    objectif.texte,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
