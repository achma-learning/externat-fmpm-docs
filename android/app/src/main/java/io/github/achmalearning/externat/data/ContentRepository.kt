package io.github.achmalearning.externat.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lit le catalogue copié dans les assets par la tâche Gradle `copy<Variant>AppContent`. */
class ContentRepository(private val context: Context) {

    suspend fun load(): Catalog = withContext(Dispatchers.IO) {
        fun read(name: String) = context.assets.open("data/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }
        Catalog.parse(read("index.json"), read("objectifs.json"), read("guides.json"), read("assets.json"))
    }
}
