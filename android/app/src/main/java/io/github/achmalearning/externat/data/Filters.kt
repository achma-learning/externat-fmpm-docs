package io.github.achmalearning.externat.data

/** Filtre de version : 2022, ancien guide, ou les deux. */
enum class VersionFilter(val storageKey: String) {
    BOTH("both"), V2022(VERSION_2022), OLD(VERSION_OLD);

    fun matches(version: String) = this == BOTH || storageKey == version

    companion object {
        fun fromStorage(value: String?) = entries.firstOrNull { it.storageKey == value } ?: BOTH
    }
}

/** Années proposées dans le filtre (1A … 6A). `null` = « Toutes ». */
val YEARS = 1..6

/**
 * Une entrée correspond à l'année [year] si elle la couvre : « 3A-6A » (annees [3,4,5,6])
 * apparaît en 3A, 4A, 5A et 6A ; « XA » (année non précisée, annees vide) seulement sous « Toutes ».
 */
fun IndexEntry.matchesYear(year: Int?): Boolean = year == null || year in annees

fun IndexEntry.matches(year: Int?, version: VersionFilter): Boolean = matchesYear(year) && version.matches(this.version)

/** Spécialités ayant au moins une entrée visible, avec seulement ces entrées. */
fun Catalog.filter(year: Int?, version: VersionFilter): List<Specialite> = specialites.mapNotNull { s ->
    val visible = s.entries.filter { it.matches(year, version) }
    if (visible.isEmpty()) null else s.copy(entries = visible)
}

/** Avancement de la checklist : `done / total`. */
data class Progress(val done: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
    val label: String get() = "$done / $total"
}

fun progressOf(objectifs: List<Objectif>, checked: Set<String>) =
    Progress(objectifs.count { it.id in checked }, objectifs.size)

fun Catalog.progressOf(entries: List<IndexEntry>, checked: Set<String>): Progress =
    progressOf(entries.flatMap { objectifs(it) }, checked)
