package io.github.achmalearning.externat

import io.github.achmalearning.externat.data.Catalog
import io.github.achmalearning.externat.data.IndexEntry
import io.github.achmalearning.externat.data.Objectif
import io.github.achmalearning.externat.data.RecentItem
import io.github.achmalearning.externat.data.TextNormalizer
import io.github.achmalearning.externat.data.VersionFilter
import io.github.achmalearning.externat.data.document
import io.github.achmalearning.externat.data.filter
import io.github.achmalearning.externat.data.highlights
import io.github.achmalearning.externat.data.matchesName
import io.github.achmalearning.externat.data.matchesYear
import io.github.achmalearning.externat.data.progressOf
import io.github.achmalearning.externat.data.searchObjectifs
import io.github.achmalearning.externat.data.shareFileName
import io.github.achmalearning.externat.data.sourceLabel
import io.github.achmalearning.externat.data.withOpened
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Tests sur les vraies données du dépôt (data/index.json, data/objectifs.json, data/guides.json). */
class CatalogTest {

    private val dataDir = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "data") }
        .first { File(it, "index.json").isFile }

    /** Même règle de nommage que la tâche Gradle copy<Variant>AppContent. */
    private val catalog: Catalog by lazy {
        val index = File(dataDir, "index.json").readText()
        val guides = File(dataDir, "guides.json").readText()
        val raw = Catalog.parse(index, "[]", guides, "{}")
        val assets = (raw.entries.map { it.fichier } + raw.guides.map { it.fichier })
            .joinToString(",", "{", "}") { "\"${it.replace("\"", "\\\"")}\":\"asset/${it.hashCode()}.pdf\"" }
        Catalog.parse(index, File(dataDir, "objectifs.json").readText(), guides, assets)
    }

    private fun entry(annee: String, annees: List<Int>, version: String = "2022") = IndexEntry(
        dossier = "test", nom = "Test", categorie = "Médecine", version = version, annee = annee,
        annees = annees, fichier = "x.pdf",
    )

    @Test
    fun parsesIndexAndObjectifs() {
        assertEquals(78, catalog.entries.size)
        assertEquals(43, catalog.specialites.size)
        assertEquals(6, catalog.guides.size)
        assertTrue(catalog.objectifs.size > 1000)
        assertEquals(catalog.objectifs.size, catalog.objectifs.map { it.id }.toSet().size)
        // Chaque objectif appartient à une entrée de index.json.
        catalog.objectifs.forEach { o ->
            assertNotNull(o.id, catalog.entry("${o.dossier}:${o.version}:${o.annee}"))
        }
        val cardio = catalog.entry("cardiologie:2022:3A")!!
        assertTrue(catalog.objectifs(cardio).any { it.texte.contains("dyspnée") })
        // Objectifs non élaborés dans l'ancien guide : 0 est correct.
        assertTrue(catalog.objectifs(catalog.entry("medecine-sociale-2eme-annee:old:2A")!!).isEmpty())
    }

    @Test
    fun categoriesKeepIndexOrder() {
        assertEquals("1er cycle", catalog.categories.first())
        assertEquals("Centres de santé", catalog.categories.last())
    }

    @Test
    fun yearFilterRanges() {
        val centre = entry("3A-6A", listOf(3, 4, 5, 6))
        (3..6).forEach { assertTrue("$it", centre.matchesYear(it)) }
        assertFalse(centre.matchesYear(2))

        val nephro = entry("4A-5A", listOf(4, 5))
        assertTrue(nephro.matchesYear(4))
        assertTrue(nephro.matchesYear(5))
        assertFalse(nephro.matchesYear(3))
        assertFalse(nephro.matchesYear(6))

        val unknown = entry("XA", emptyList())
        assertTrue(unknown.matchesYear(null)) // « Toutes »
        YEARS_ALL.forEach { assertFalse(unknown.matchesYear(it)) }
    }

    @Test
    fun catalogFilterByYearAndVersion() {
        val fiveA = catalog.filter(5, VersionFilter.BOTH)
        assertTrue(fiveA.any { it.dossier == "centre-de-sante" })
        assertTrue(fiveA.any { it.dossier == "nephrologie" })
        assertFalse(fiveA.any { it.dossier == "medecine-physique-readaptation" })
        assertTrue(catalog.filter(null, VersionFilter.BOTH).any { it.dossier == "medecine-physique-readaptation" })

        val old6 = catalog.filter(6, VersionFilter.OLD)
        assertTrue(old6.all { s -> s.entries.all { it.version == "old" && 6 in it.annees } })
        assertTrue(old6.any { it.dossier == "cardiologie" })
        assertFalse(catalog.filter(6, VersionFilter.V2022).any { it.dossier == "cardiologie" })
    }

    @Test
    fun accentInsensitiveNameSearch() {
        val gyneco = catalog.specialite("gynecologie-obstetrique")!!
        assertTrue(gyneco.matchesName("gyneco"))
        assertTrue(gyneco.matchesName("GYNÉCO"))
        assertTrue(gyneco.matchesName("obstétrique gyn"))
        assertFalse(gyneco.matchesName("cardio"))
        assertEquals("oedeme", TextNormalizer.normalize("Œdème"))
        assertEquals("l'enfant", TextNormalizer.normalize("L’enfant"))
    }

    @Test
    fun fullTextSearchWithHighlights() {
        val hits = catalog.searchObjectifs("dyspnee", null, VersionFilter.BOTH)
        assertTrue(hits.isNotEmpty())
        hits.forEach { hit ->
            assertTrue(hit.highlights.isNotEmpty())
            val h = hit.highlights.first()
            assertEquals("dyspnee", TextNormalizer.normalize(hit.objectif.texte.substring(h.start, h.end)))
        }
        assertTrue(catalog.searchObjectifs("dyspnee", 3, VersionFilter.V2022).all { it.entry.version == "2022" })
        assertTrue(catalog.searchObjectifs("x", null, VersionFilter.BOTH).isEmpty())
    }

    @Test
    fun highlightMapsBackThroughLigatures() {
        val text = "Syndrome œdémateux chez l’enfant"
        val ranges = highlights(text, "oedemateux enfant")!!
        assertEquals("œdémateux", text.substring(ranges[0].start, ranges[0].end))
        assertEquals("enfant", text.substring(ranges[1].start, ranges[1].end))
        assertNull(highlights(text, "cardio"))
    }

    @Test
    fun checklistProgress() {
        val objectifs = (1..20).map { Objectif("t:2022:3A:%02d".format(it), "t", "2022", "3A", null, "o$it") }
        val checked = objectifs.take(12).map { it.id }.toSet() + "autre:id"
        val p = progressOf(objectifs, checked)
        assertEquals(12, p.done)
        assertEquals(20, p.total)
        assertEquals("12 / 20", p.label)
        assertEquals(0.6f, p.fraction, 0.0001f)
        assertEquals(0f, progressOf(emptyList(), checked).fraction)
    }

    @Test
    fun recentsAreDedupedAndCapped() {
        var list = emptyList<RecentItem>()
        (1..25).forEach { list = list.withOpened("e:$it", it.toLong()) }
        list = list.withOpened("e:10", 100)
        assertEquals(20, list.size)
        assertEquals("e:10", list.first().key)
        assertEquals(1, list.count { it.key == "e:10" })
    }

    @Test
    fun labelsAndDocuments() {
        val cardio = catalog.entry("cardiologie:2022:3A")!!
        assertEquals("Guide de stage 2022, p. 12", cardio.sourceLabel())
        assertEquals("Cardiologie - 3A - objectifs de stage 2022.pdf", cardio.shareFileName())
        assertEquals("Ancien guide de stage, pp. 45-46", catalog.entry("cardiologie:old:3A")!!.sourceLabel())
        assertFalse(catalog.entry("imagerie-medicale:2022:4A")!!.shareFileName().contains("/"))
        assertNotNull(catalog.document("e:cardiologie:2022:3A"))
        assertNotNull(catalog.document("g:guide-stage-2022"))
        assertNull(catalog.document("g:inconnu"))
    }

    private companion object {
        val YEARS_ALL = 1..6
    }
}
