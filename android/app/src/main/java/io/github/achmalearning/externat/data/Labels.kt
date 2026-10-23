package io.github.achmalearning.externat.data

/** « 3A · 2022 », « 6A · old » */
fun IndexEntry.shortLabel(): String = "$annee · $version"

/** Libellé d'année lisible : « 3A », « 3A-6A », « année non précisée » pour XA. */
fun IndexEntry.yearLabel(): String = if (annee == "XA") "année non précisée" else annee

/** « Guide de stage 2022, p. 12 » / « Ancien guide de stage, p. 45-46 » */
fun IndexEntry.sourceLabel(): String {
    val guide = if (version == VERSION_2022) "Guide de stage 2022" else "Ancien guide de stage"
    val p = if (pages.size > 1) "pp." else "p."
    return "$guide, $p ${pages.joinToString("-")}"
}

/** Nom de fichier partagé : « Cardiologie - 3A - objectifs de stage 2022.pdf » */
fun IndexEntry.shareFileName(): String {
    val v = if (version == VERSION_2022) "2022" else "ancien guide"
    return safeFileName("$nom - $annee - objectifs de stage $v") + ".pdf"
}

fun Guide.shareFileName(): String = safeFileName(titre) + ".pdf"

/** Texte joint au partage. */
fun IndexEntry.shareText(): String {
    val v = if (version == VERSION_2022) "guide 2022" else "ancien guide"
    return "Objectifs de stage — $nom — ${yearLabel()} ($v)\nvia Externat FMPM"
}

fun Guide.shareText(): String = "$titre — FMPM\nvia Externat FMPM"

/** Retire les caractères interdits dans un nom de fichier (« Imagerie médicale / Radiologie »). */
fun safeFileName(name: String): String =
    name.replace(Regex("[\\\\/:*?\"<>|]+"), "-").replace(Regex("\\s+"), " ").trim().take(120)
