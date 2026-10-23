# Externat FMPM — application Android

Application native (Kotlin, Jetpack Compose, Material 3), un seul module `:app`, entièrement hors ligne.
Les PDF et le texte des objectifs viennent du dépôt : rien n'est dupliqué dans `android/`.

## Versions

| Outil | Version | Où |
|---|---|---|
| Android Gradle Plugin | 9.4.1 (Kotlin intégré) | `gradle/libs.versions.toml` |
| Gradle | 9.8.2 | `gradle/wrapper/gradle-wrapper.properties` |
| Kotlin / Compose compiler | 2.4.21 | `libs.versions.toml` |
| Compose BOM | 2026.09.00 (Material 3 1.4.0) | `libs.versions.toml` |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 (Android 7.0) | `app/build.gradle.kts` |
| JDK de build | 17+ | CI : Temurin 17 |

Dépendances : AndroidX core, core-splashscreen, activity-compose, lifecycle, navigation-compose,
Compose Material 3 + `material3-adaptive-navigation-suite`, `material3-adaptive` (layout, navigation),
`material3-window-size-class`, DataStore Preferences, kotlinx-serialization-json. Rien d'autre :
pas de bibliothèque PDF (`android.graphics.pdf.PdfRenderer` de la plateforme), pas de réseau, pas
de Firebase. Les icônes sont des vector drawables dans `res/drawable/` (la bibliothèque
`material-icons-extended` n'est plus maintenue).

Tests uniquement : JUnit 4, Robolectric 4.17, `androidx.test.ext:junit`, `compose ui-test-junit4`.

## Architecture

```
app/src/main/java/io/github/achmalearning/externat/
├── MainActivity.kt          splash screen, edge-to-edge, thème
├── data/                    sans UI
│   ├── Catalog.kt           modèles (IndexEntry, Objectif, Guide, Specialite) + index en mémoire
│   ├── Filters.kt           filtres année / version, avancement « 12 / 20 »
│   ├── Search.kt            normalisation sans accents, recherche nom + plein texte, surlignage
│   ├── Labels.kt            libellés (« Guide de stage 2022, p. 12 »), noms de fichiers partagés
│   ├── Documents.kt         clés s:/e:/g: (favoris, historique) -> DocRef (PDF + partage)
│   ├── ContentRepository.kt lecture des assets JSON (hors thread UI)
│   ├── UserPreferences.kt   DataStore : checklist, favoris, 20 récents, réglages, filtres
│   └── PdfFiles.kt          copie asset -> filesDir, partage FileProvider, SAF, « Ouvrir avec »
└── ui/
    ├── AppViewModel.kt      catalogue + état utilisateur (StateFlow)
    ├── ExternatApp.kt       NavigationSuiteScaffold (barre en bas / rail) + NavHost
    ├── objectifs/           liste-détail (NavigableListDetailPaneScaffold), checklist
    ├── guides/              les 6 guides
    ├── favoris/             favoris et récemment ouverts
    ├── viewer/              PdfDocument (PdfRenderer + cache) et PdfViewer (zoom, pages)
    ├── settings/            thème, couleurs dynamiques, pages sombres, à propos
    ├── common/              boutons favori, progression, actions partager / enregistrer
    └── theme/               Material 3, couleurs dynamiques, palette teal de repli
```

- **Catalogue** : `data/index.json` (78 PDF), `data/objectifs.json` (texte des objectifs),
  `data/guides.json` (6 guides) sont lus une fois au démarrage ; l'écran de démarrage reste affiché
  le temps de les charger.
- **Checklist** : un `Set<String>` d'`id` d'objectifs dans DataStore. Les `id`
  (`cardiologie:2022:3A:01`) sont stables : la checklist survit aux redémarrages et aux mises à jour
  (même clé de signature).
- **PDF** : `PdfRenderer` a besoin d'un fichier « seekable » : l'asset est copié une fois dans
  `filesDir/pdf/v<versionCode>/…` puis réutilisé ; les copies d'une version précédente sont effacées.
  Les pages sont rendues sur un thread dédié, à la largeur affichée × densité, puis re-rendues plus
  fines au zoom (paliers ×1, ×2, ×3). Cache LRU = ¼ de la mémoire de l'app ; une page évincée et
  non affichée est recyclée.
- **Partage** : copie nommée lisiblement dans `cacheDir/share/` puis `FileProvider` (`content://`,
  `application/pdf`, lecture accordée). **Enregistrer** : `CreateDocument` (Storage Access
  Framework). Aucune permission, sur aucune version d'Android.
- **Tablette** : `NavigationSuiteScaffold` choisit barre du bas / rail selon la taille de fenêtre ;
  le module Objectifs passe en deux volets (liste à gauche, checklist ou PDF à droite).

## Copie du contenu (tâche Gradle)

`app/build.gradle.kts` enregistre, pour chaque variante, une tâche `copy<Variant>AppContent`
branchée sur `variant.sources.assets.addGeneratedSourceDirectory(...)` : AGP la lance avant la
fusion des assets. Elle lit `../data/index.json` et `../data/guides.json` et écrit dans
`app/build/generated/assets/copy<Variant>AppContent/` :

```
objectifs/<dossier>/<annee>-<version>.pdf   ex. objectifs/cardiologie/3a-2022.pdf
guides/<id>.pdf                              ex. guides/guide-etudiant-2024.pdf
data/index.json, data/objectifs.json, data/guides.json
data/assets.json                             chemin du dépôt -> chemin d'asset
```

Les noms d'assets sont des slugs ASCII (les fichiers sources ont des espaces, des accents et des
apostrophes). Les noms de fichiers stockés en NFD (accents décomposés) sont retrouvés après
normalisation. Ses entrées sont déclarées (`@InputFiles`) : elle ne se relance que si un PDF ou un
JSON change.

> Linux : lancez Gradle avec une locale UTF-8 (`export LC_ALL=C.UTF-8`), sinon la JVM ne sait pas
> ouvrir les fichiers accentués. La CI le fait déjà.

## Construire

```bash
cd android
./gradlew :app:assembleDebug        # APK de debug : app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # tests JVM + tests UI Robolectric
./gradlew :app:lintDebug            # lint
./gradlew :app:assembleRelease      # APK R8 (signé debug sans les variables KEYSTORE_*)
```

Le SDK Android est trouvé via `ANDROID_HOME` ou `local.properties` (`sdk.dir=…`, non versionné).
Variables lues par le build : `VERSION_CODE`, `VERSION_NAME`, `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`.

## Tests

- `CatalogTest` : lecture des vrais `index.json` / `objectifs.json`, filtres d'année (`3A-6A`,
  `4A-5A`, `XA`), recherche sans accents et surlignage, progression, historique, libellés.
- `AppSmokeTest` (Robolectric, SDK 34) : démarrage, liste -> détail, case cochée persistée (y compris
  après recréation de l'activité), recherche plein texte, onglet Guides, deux volets sur tablette.
  Le rendu des pages PDF (PdfRenderer) n'est pas simulé par Robolectric : il se vérifie sur appareil.
