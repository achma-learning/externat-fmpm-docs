package io.github.achmalearning.externat.data

import java.text.Normalizer

/**
 * Normalisation pour la recherche : minuscules, sans accents (« Gynécologie » -> « gynecologie »),
 * ligatures dépliées (« œ » -> « oe »), apostrophes typographiques unifiées.
 */
object TextNormalizer {
    private fun fold(c: Char): String = when (c) {
        'œ', 'Œ' -> "oe"
        'æ', 'Æ' -> "ae"
        '’', '‘', 'ʼ' -> "'"
        else -> Normalizer.normalize(c.toString(), Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
            .lowercase()
    }

    fun normalize(text: String): String = buildString(text.length) { text.forEach { append(fold(it)) } }

    /** Texte normalisé + pour chaque caractère normalisé, l'index du caractère d'origine. */
    fun normalizeWithMap(text: String): Pair<String, IntArray> {
        val out = StringBuilder(text.length)
        val map = ArrayList<Int>(text.length)
        text.forEachIndexed { i, c ->
            val f = fold(c)
            out.append(f)
            repeat(f.length) { map.add(i) }
        }
        return out.toString() to map.toIntArray()
    }

    fun tokens(query: String): List<String> = normalize(query).split(Regex("[\\s,;]+")).filter { it.isNotBlank() }
}

/** Plage [start, end) à surligner dans le texte d'origine. */
data class Highlight(val start: Int, val end: Int)

/** Tous les mots de la requête doivent apparaître (ET) ; renvoie les plages trouvées, ou null. */
fun highlights(text: String, query: String): List<Highlight>? {
    val tokens = TextNormalizer.tokens(query)
    if (tokens.isEmpty()) return null
    val (normalized, map) = TextNormalizer.normalizeWithMap(text)
    val ranges = mutableListOf<Highlight>()
    for (token in tokens) {
        var from = normalized.indexOf(token)
        if (from < 0) return null
        while (from >= 0) {
            ranges += Highlight(map[from], map[from + token.length - 1] + 1)
            from = normalized.indexOf(token, from + token.length)
        }
    }
    return ranges.sortedBy { it.start }
}

fun Specialite.matchesName(query: String): Boolean {
    val tokens = TextNormalizer.tokens(query)
    if (tokens.isEmpty()) return true
    val haystack = TextNormalizer.normalize("$nom $dossier $categorie")
    return tokens.all { it in haystack }
}

/** Résultat de recherche plein texte dans un objectif. */
data class ObjectifHit(val objectif: Objectif, val entry: IndexEntry, val highlights: List<Highlight>)

/**
 * Recherche plein texte dans les objectifs des entrées visibles (filtres année/version appliqués).
 * Les requêtes de moins de 2 caractères ne cherchent pas dans le texte (trop de résultats).
 */
fun Catalog.searchObjectifs(query: String, year: Int?, version: VersionFilter, limit: Int = 200): List<ObjectifHit> {
    val tokens = TextNormalizer.tokens(query)
    if (tokens.isEmpty() || tokens.sumOf { it.length } < 2) return emptyList()
    val hits = ArrayList<ObjectifHit>()
    for ((objectif, normalized) in searchIndex) {
        if (!tokens.all { it in normalized }) continue
        val entry = entry(entryKey(objectif.dossier, objectif.version, objectif.annee)) ?: continue
        if (!entry.matches(year, version)) continue
        hits += ObjectifHit(objectif, entry, highlights(objectif.texte, query).orEmpty())
        if (hits.size >= limit) break
    }
    return hits
}
