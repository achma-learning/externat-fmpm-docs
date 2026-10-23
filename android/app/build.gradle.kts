import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import java.text.Normalizer

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// versionCode = numéro de run de la CI ; versionName = tag git (v1.2.3 -> 1.2.3) ou 1.0.<run>.
val ciVersionCode = providers.environmentVariable("VERSION_CODE").orNull?.toIntOrNull() ?: 1
val ciVersionName = providers.environmentVariable("VERSION_NAME").orNull ?: "1.0.$ciVersionCode"

// Signature : keystore fixe fourni par la CI (secrets), sinon clé de debug.
val keystoreFile = providers.environmentVariable("KEYSTORE_FILE").orNull?.takeIf { File(it).isFile }

android {
    namespace = "io.github.achmalearning.externat"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.achmalearning.externat"
        minSdk = 24
        targetSdk = 37
        versionCode = ciVersionCode
        versionName = ciVersionName
    }

    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = File(keystoreFile)
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        // Les versions sont choisies à la main (stables uniquement) : pas d'alerte « nouvelle version ».
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
    }

    testOptions {
        // Robolectric : les tests JVM lisent les vraies ressources et les assets générés.
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

/**
 * Copie le contenu du dépôt (PDF + JSON) dans un dossier d'assets généré, avec des noms ASCII.
 * Rien n'est dupliqué dans android/ : la source reste data/, objective par specialite/, sources/, others/.
 *
 * Sortie :
 *   objectifs/<dossier>/<annee>-<version>.pdf   (chaque entrée de data/index.json)
 *   guides/<id>.pdf                              (chaque entrée de data/guides.json)
 *   data/index.json, data/objectifs.json, data/guides.json
 *   data/assets.json                             (chemin du dépôt -> chemin d'asset)
 */
abstract class CopyAppContentTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentFiles: ConfigurableFileCollection

    @get:Internal
    abstract val repoRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val root = repoRoot.get().asFile
        val out = outputDir.get().asFile
        out.deleteRecursively()
        val mapping = linkedMapOf<String, String>()

        fun place(repoPath: String, assetPath: String) {
            val source = resolve(root, repoPath)
            source.copyTo(File(out, assetPath), overwrite = true)
            mapping[nfc(repoPath)] = assetPath
        }

        @Suppress("UNCHECKED_CAST")
        val index = JsonSlurper().parse(File(root, "data/index.json")) as List<Map<String, Any>>
        for (entry in index) {
            val asset = "objectifs/${slug(entry["dossier"] as String)}/" +
                "${slug("${entry["annee"]}-${entry["version"]}")}.pdf"
            place(entry["fichier"] as String, asset)
        }
        @Suppress("UNCHECKED_CAST")
        val guides = JsonSlurper().parse(File(root, "data/guides.json")) as List<Map<String, Any>>
        for (guide in guides) {
            place(guide["fichier"] as String, "guides/${slug(guide["id"] as String)}.pdf")
        }
        for (name in listOf("index.json", "objectifs.json", "guides.json")) {
            File(root, "data/$name").copyTo(File(out, "data/$name"), overwrite = true)
        }
        File(out, "data/assets.json").writeText(JsonOutput.toJson(mapping), Charsets.UTF_8)
        logger.lifecycle("Contenu de l'app : ${mapping.size} PDF copiés dans ${out.relativeTo(root)}")
    }

    private fun nfc(s: String) = Normalizer.normalize(s, Normalizer.Form.NFC)

    private fun slug(s: String): String = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')

    /** Retrouve un fichier même si son nom est stocké en NFD (accents décomposés) sur le disque. */
    private fun resolve(root: File, repoPath: String): File {
        val direct = File(root, repoPath)
        if (direct.isFile) return direct
        var current = root
        for (part in repoPath.split('/')) {
            current = current.listFiles()?.firstOrNull { nfc(it.name) == nfc(part) }
                ?: throw GradleException("Fichier introuvable : $repoPath")
        }
        return current
    }
}

val repoRootDir = rootProject.layout.projectDirectory.dir("..")

androidComponents {
    onVariants { variant ->
        val task = tasks.register<CopyAppContentTask>("copy${variant.name.replaceFirstChar { it.uppercase() }}AppContent") {
            repoRoot.set(repoRootDir)
            contentFiles.from(
                repoRootDir.dir("data").asFileTree.matching { include("*.json") },
                repoRootDir.dir("objective par specialite").asFileTree.matching { include("**/*.pdf") },
                repoRootDir.dir("others").asFileTree.matching { include("*.pdf") },
                repoRootDir.dir("sources").asFileTree.matching { include("*.pdf") },
            )
        }
        variant.sources.assets?.addGeneratedSourceDirectory(task, CopyAppContentTask::outputDir)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.navigation.suite)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
}
