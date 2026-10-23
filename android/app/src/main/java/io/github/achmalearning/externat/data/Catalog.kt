package io.github.achmalearning.externat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Une entrée de data/index.json : un PDF d'objectifs (spécialité × version × année). */
@Serializable
data class IndexEntry(
    val dossier: String,
    val nom: String,
    val categorie: String,
    val version: String,
    val annee: String,
    val annees: List<Int> = emptyList(),
    @Suppress("PropertyName") val pages_source: List<Int> = emptyList(),
    val fichier: String,
    @Suppress("PropertyName") val taille_ko: Int = 0,
    val note: String? = null,
) {
    /** Clé stable d'une entrée, utilisée pour les favoris et l'historique. */
    val key: String get() = entryKey(dossier, version, annee)
    val pages: List<Int> get() = pages_source
}

/** Un objectif de data/objectifs.json. */
@Serializable
data class Objectif(
    val id: String,
    val dossier: String,
    val version: String,
    val annee: String,
    val rubrique: String? = null,
    val texte: String,
)

/** Un guide complet de data/guides.json. */
@Serializable
data class Guide(
    val id: String,
    val titre: String,
    val description: String,
    val fichier: String,
)

/** Une spécialité et toutes ses entrées (dans l'ordre de index.json). */
data class Specialite(
    val dossier: String,
    val nom: String,
    val categorie: String,
    val entries: List<IndexEntry>,
)

const val VERSION_2022 = "2022"
const val VERSION_OLD = "old"

fun entryKey(dossier: String, version: String, annee: String) = "$dossier:$version:$annee"

/**
 * Tout le contenu embarqué, chargé une seule fois au démarrage.
 * [assets] associe un chemin du dépôt (`fichier`) au chemin de l'asset ASCII copié par Gradle.
 */
class Catalog(
    val entries: List<IndexEntry>,
    val objectifs: List<Objectif>,
    val guides: List<Guide>,
    private val assets: Map<String, String>,
) {
    val specialites: List<Specialite> = entries.groupBy { it.dossier }.map { (dossier, list) ->
        Specialite(dossier, list.first().nom, list.first().categorie, list)
    }
    val categories: List<String> = specialites.map { it.categorie }.distinct()

    private val specialiteByDossier = specialites.associateBy { it.dossier }
    private val entryByKey = entries.associateBy { it.key }
    private val guideById = guides.associateBy { it.id }
    private val objectifsByEntry: Map<String, List<Objectif>> =
        objectifs.groupBy { entryKey(it.dossier, it.version, it.annee) }

    /** Objectifs normalisés une fois pour la recherche plein texte. */
    internal val searchIndex: List<Pair<Objectif, String>> by lazy {
        objectifs.map { it to TextNormalizer.normalize(it.texte + " " + (it.rubrique ?: "")) }
    }

    fun specialite(dossier: String): Specialite? = specialiteByDossier[dossier]
    fun entry(key: String): IndexEntry? = entryByKey[key]
    fun guide(id: String): Guide? = guideById[id]
    fun objectifs(entry: IndexEntry): List<Objectif> = objectifsByEntry[entry.key].orEmpty()

    fun assetPath(fichier: String): String? = assets[java.text.Normalizer.normalize(fichier, java.text.Normalizer.Form.NFC)]

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(indexJson: String, objectifsJson: String, guidesJson: String, assetsJson: String) = Catalog(
            entries = json.decodeFromString(indexJson),
            objectifs = json.decodeFromString(objectifsJson),
            guides = json.decodeFromString(guidesJson),
            assets = json.decodeFromString(assetsJson),
        )
    }
}
