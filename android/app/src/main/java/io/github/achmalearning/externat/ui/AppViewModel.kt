package io.github.achmalearning.externat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.ContentRepository
import io.github.achmalearning.externat.data.Objectif
import io.github.achmalearning.externat.data.PdfFiles
import io.github.achmalearning.externat.data.ThemeMode
import io.github.achmalearning.externat.data.UserPreferences
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.data.VersionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Demande d'ouverture d'une spécialité (depuis les favoris, l'historique ou la recherche). */
data class OpenRequest(val dossier: String, val entryKey: String?, val showPdf: Boolean = false, val id: Long = System.nanoTime())

sealed interface CatalogState {
    data object Loading : CatalogState
    data class Ready(val catalog: Catalog) : CatalogState
    data class Error(val message: String) : CatalogState
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)

    private val _catalog = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalog: StateFlow<CatalogState> = _catalog.asStateFlow()

    /** `null` tant que DataStore n'a pas été lu (évite d'afficher des filtres par défaut une fraction de seconde). */
    val user: StateFlow<UserState?> = prefs.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _openRequest = MutableStateFlow<OpenRequest?>(null)
    val openRequest: StateFlow<OpenRequest?> = _openRequest.asStateFlow()

    init {
        viewModelScope.launch {
            _catalog.value = runCatching { CatalogState.Ready(ContentRepository(application).load()) }
                .getOrElse { CatalogState.Error(it.message ?: it.toString()) }
        }
        viewModelScope.launch { PdfFiles.cleanupOldVersions(application) }
    }

    fun requestOpen(dossier: String, entryKey: String?, showPdf: Boolean = false) {
        _openRequest.value = OpenRequest(dossier, entryKey, showPdf)
    }

    fun consumeOpenRequest(request: OpenRequest) {
        if (_openRequest.value?.id == request.id) _openRequest.value = null
    }

    fun setChecked(objectif: Objectif, checked: Boolean) = launch { prefs.setChecked(objectif.id, checked) }
    fun uncheckAll(objectifs: List<Objectif>) = launch { prefs.uncheckAll(objectifs.map { it.id }) }
    fun toggleFavorite(key: String) = launch { prefs.toggleFavorite(key) }
    fun addRecent(key: String) = launch { prefs.addRecent(key) }
    fun clearRecents() = launch { prefs.clearRecents() }
    fun setFilters(year: Int?, version: VersionFilter) = launch { prefs.setFilters(year, version) }
    fun setThemeMode(mode: ThemeMode) = launch { prefs.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = launch { prefs.setDynamicColor(enabled) }
    fun setDarkPages(enabled: Boolean) = launch { prefs.setDarkPages(enabled) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
