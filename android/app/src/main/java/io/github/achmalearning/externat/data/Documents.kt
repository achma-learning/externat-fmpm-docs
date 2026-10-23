package io.github.achmalearning.externat.data

/**
 * Clés des favoris et de l'historique :
 *  - `s:<dossier>`                    une spécialité
 *  - `e:<dossier>:<version>:<annee>`  une entrée (PDF d'objectifs)
 *  - `g:<id>`                         un guide
 */
object DocKeys {
    fun specialite(dossier: String) = "s:$dossier"
    fun entry(entry: IndexEntry) = "e:${entry.key}"
    fun guide(guide: Guide) = "g:${guide.id}"
}

/** Un PDF ouvrable dans la visionneuse, avec ce qu'il faut pour le partager. */
data class DocRef(
    val key: String,
    val title: String,
    val subtitle: String,
    val assetPath: String,
    val shareName: String,
    val shareText: String,
)

fun Catalog.document(key: String): DocRef? = when {
    key.startsWith("e:") -> entry(key.removePrefix("e:"))?.let { e ->
        assetPath(e.fichier)?.let { asset ->
            DocRef(key, e.nom, "${e.shortLabel()} — ${e.sourceLabel()}", asset, e.shareFileName(), e.shareText())
        }
    }
    key.startsWith("g:") -> guide(key.removePrefix("g:"))?.let { g ->
        assetPath(g.fichier)?.let { asset -> DocRef(key, g.titre, g.description, asset, g.shareFileName(), g.shareText()) }
    }
    else -> null
}
