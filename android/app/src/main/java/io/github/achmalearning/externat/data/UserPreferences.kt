package io.github.achmalearning.externat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

enum class ThemeMode(val storageKey: String) {
    SYSTEM("system"), LIGHT("light"), DARK("dark");

    companion object {
        fun fromStorage(value: String?) = entries.firstOrNull { it.storageKey == value } ?: SYSTEM
    }
}

@Serializable
data class RecentItem(val key: String, val openedAt: Long)

/** État persistant de l'utilisateur (checklist, favoris, historique, réglages). */
data class UserState(
    val checked: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(),
    val recents: List<RecentItem> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val darkPages: Boolean = false,
    val yearFilter: Int? = null,
    val versionFilter: VersionFilter = VersionFilter.BOTH,
)

const val MAX_RECENTS = 20

/** Ajoute [key] en tête de l'historique (sans doublon), limité à [MAX_RECENTS]. */
fun List<RecentItem>.withOpened(key: String, now: Long): List<RecentItem> =
    (listOf(RecentItem(key, now)) + filterNot { it.key == key }).take(MAX_RECENTS)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "externat")

/**
 * Stockage DataStore. La checklist est indexée par l'`id` stable des objectifs : elle survit aux
 * redémarrages et aux mises à jour de l'app (même clé de signature).
 */
class UserPreferences(context: Context) {
    private val store = context.applicationContext.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val checked = stringSetPreferencesKey("checked")
        val favorites = stringSetPreferencesKey("favorites")
        val recents = stringPreferencesKey("recents")
        val theme = stringPreferencesKey("theme")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val darkPages = booleanPreferencesKey("dark_pages")
        val year = intPreferencesKey("filter_year")
        val version = stringPreferencesKey("filter_version")
    }

    val state: Flow<UserState> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            UserState(
                checked = p[Keys.checked].orEmpty(),
                favorites = p[Keys.favorites].orEmpty(),
                recents = p[Keys.recents]?.let { runCatching { json.decodeFromString<List<RecentItem>>(it) }.getOrNull() }
                    .orEmpty(),
                themeMode = ThemeMode.fromStorage(p[Keys.theme]),
                dynamicColor = p[Keys.dynamicColor] ?: true,
                darkPages = p[Keys.darkPages] ?: false,
                yearFilter = p[Keys.year]?.takeIf { it in YEARS },
                versionFilter = VersionFilter.fromStorage(p[Keys.version]),
            )
        }

    suspend fun setChecked(id: String, checked: Boolean) = store.edit {
        val set = it[Keys.checked].orEmpty()
        it[Keys.checked] = if (checked) set + id else set - id
    }

    suspend fun uncheckAll(ids: Collection<String>) = store.edit {
        it[Keys.checked] = it[Keys.checked].orEmpty() - ids.toSet()
    }

    suspend fun toggleFavorite(key: String) = store.edit {
        val set = it[Keys.favorites].orEmpty()
        it[Keys.favorites] = if (key in set) set - key else set + key
    }

    suspend fun addRecent(key: String, now: Long = System.currentTimeMillis()) = store.edit {
        val current = it[Keys.recents]?.let { s -> runCatching { json.decodeFromString<List<RecentItem>>(s) }.getOrNull() }
            .orEmpty()
        it[Keys.recents] = json.encodeToString(current.withOpened(key, now))
    }

    suspend fun clearRecents() = store.edit { it.remove(Keys.recents) }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.theme] = mode.storageKey }
    suspend fun setDynamicColor(enabled: Boolean) = store.edit { it[Keys.dynamicColor] = enabled }
    suspend fun setDarkPages(enabled: Boolean) = store.edit { it[Keys.darkPages] = enabled }

    suspend fun setFilters(year: Int?, version: VersionFilter) = store.edit {
        if (year == null) it.remove(Keys.year) else it[Keys.year] = year
        it[Keys.version] = version.storageKey
    }
}
