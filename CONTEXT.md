# Externat FMPM — AI Context File
_Last synced: 2026-10-23 @ f5eac2f (main, after PR #1)_

## 1. What This Is (Plain English)
- **In one sentence:** a pocket library for Marrakech medical students. It holds the official hospital-internship objectives, cut by specialty and year, plus the faculty's guides, and an Android app that shows them offline with a tick-box checklist.
- **Why it exists:** the objectives are buried in two long PDF guides (2022 and an older one). This repo cuts them into one PDF per specialty/year, pulls out their text, and puts everything in a phone app you can use on a hospital floor without network.
- **Who uses it:** the owner (a FMPM student) on their own phone and tablet, maybe friends via the APK. Not on the Play Store, not an official faculty app.
- **Vibe:** polished personal tool. Strict about data accuracy (medical objectives are never invented or reworded), relaxed about everything else.

## 2. How To Run It
- **Setup once:**
  - Python 3 + `pip install pymupdf python-docx` (data scripts).
  - JDK 17+ and the Android SDK with API 37. Point `android/local.properties` at it (`sdk.dir=…`, not committed) or set `ANDROID_HOME`.
  - Linux only: `export LC_ALL=C.UTF-8` before Gradle, or file names with accents break the build.
- **Regenerate the data:** `python scripts/build_objectives.py`, then `python scripts/build_app_data.py`, then `python scripts/check_data.py`.
- **See the app:** `cd android && ./gradlew :app:assembleDebug` → install `android/app/build/outputs/apk/debug/app-debug.apk` on a phone.
- **Tests + lint:** `cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug`.
- **Build / deploy:** every push to `main` → GitHub Actions builds the APK (artifact `externat-fmpm-apk`). Pushing a tag `v1.2.3` → GitHub Release with `.apk` + `.zip`. Steps are in `RELEASE.md`.
- **Required secrets** (GitHub Actions, names only): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The Gradle build reads `VERSION_CODE`, `VERSION_NAME`, `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (`android/app/build.gradle.kts:13-17, 35-37`). No `.env` file.

## 3. Tech Stack
- **Data side:** Python 3, `pymupdf` (cut/compress/read PDFs), `python-docx` (reads the hand-corrected transcriptions).
- **App:** Kotlin 2.4.21, Jetpack Compose BOM 2026.09.00 (Material 3 1.4.0), AGP 9.4.1 with built-in Kotlin, Gradle 9.8.2, compileSdk/targetSdk 37, minSdk 24 (`android/gradle/libs.versions.toml`).
- **Load-bearing libs:** navigation-compose 2.10.2, material3-adaptive 1.3.0 (list-detail), material3-adaptive-navigation-suite, DataStore Preferences 1.3.0, kotlinx-serialization-json 1.12.0. PDFs are drawn by the platform `android.graphics.pdf.PdfRenderer` (no PDF library). Icons are local vector drawables.
- **Tests:** JUnit 4, Robolectric 4.17 (on SDK 34), Compose ui-test.
- **What kind of project:** content repo (PDFs + JSON) + single-module Android app + CI.
- **External services:** none at runtime (no network permission). GitHub Actions for builds and releases only.

## 4. Code Map (The Important Files Only)
- `data/specialites.json` — **source of truth**: specialty → version → year → source pages. Edit this, never `index.json`.
- `scripts/build_objectives.py` — cuts the guides into `objective par specialite/`, rewrites `data/index.json` and the README table between `<!-- TABLE:START -->` / `<!-- TABLE:END -->`.
- `scripts/build_app_data.py` — docx → `data/objectifs.json` + `data/objectifs_report.md`. Watch out: the heading → specialty maps (`HEADINGS_2022`, `HEADINGS_OLD`, `SWITCH_OLD`) are hand-written.
- `scripts/check_data.py` — CI sanity check (files exist, IDs unique, objectives match entries).
- `data/guides.json` — the 6 full guides shipped in the app.
- `android/app/build.gradle.kts` — build config + `CopyAppContentTask`, which copies repo PDFs/JSON into generated assets with ASCII names.
- `android/app/src/main/java/io/github/achmalearning/externat/MainActivity.kt` — **open first**: splash, edge-to-edge, theme.
- `…/ui/ExternatApp.kt` — navigation (bottom bar / rail, routes).
- `…/data/Catalog.kt`, `Filters.kt`, `Search.kt` — pure logic, unit-tested.
- `…/data/UserPreferences.kt` — DataStore: checklist, favorites, recents, settings.
- `…/ui/viewer/PdfViewer.kt` + `PdfDocument.kt` — PDF viewer (zoom, cache, rendering).
- `android/app/src/test/…/CatalogTest.kt`, `AppSmokeTest.kt` — data tests + Robolectric UI smoke tests.
- `.github/workflows/android.yml` — CI, signing (and the stable-tag guard), release creation.
- `RELEASE.md` — how to ship; `android/README.md` — app architecture and keystore setup.

## 5. Rules For Editing This Code
- **Never invent, reword or "fix the meaning" of an objective.** Only light cleanup (hyphenation, spaces, merged lines). Real source errors are fixed in `drafts/*.docx`, then the script is rerun.
- Content changes = edit `data/specialites.json` or `drafts/*.docx` → run both build scripts → commit the generated files. No app code changes for content.
- Never commit APKs, AABs, keystores, `local.properties` or build folders (`.gitignore`). Every committed file stays under 25 MB (GitHub web upload limit).
- Don't copy PDFs into `android/`: the Gradle task pulls them from the repo at build time.
- App: no network, no runtime permissions, no analytics, no Firebase. Stable AndroidX/Jetpack libraries only, no alpha. Every new dependency needs a reason.
- UI text is French, in `res/values/strings.xml` (plurals where a count is shown).
- Lint must stay at "No issues found" (`abortOnError = true`).
- Keep the `<!-- TABLE:START -->` / `<!-- TABLE:END -->` markers in `README.md`.
- Releases: stable versions only from `main`, only with the fixed keystore. Tags are never moved or deleted (`RELEASE.md`).

## 6. Fragile Bits & Landmines
- **Objective IDs are positional** (`cardiologie:2022:3A:01` = 1st objective of that entry). Inserting or removing an objective in a docx shifts later IDs, so already-ticked boxes move to the wrong objective. Prefer appending.
- **Signing key = update path.** An APK can only be updated by one signed with the same key. Without the secrets, CI signs with a throwaway debug key (uninstall needed, checklist lost). A stable tag without the secrets fails on purpose with `Tag stable … sans secrets de signature`.
- **Accented file names:** `others/Guide-de-létudiant-FMPM-2024.pdf` is stored NFD in git, the others NFC. The copy task matches names after Unicode normalization (`build.gradle.kts`, `resolve()` at line 137). The JVM also needs a UTF-8 locale on Linux (symptom: `Failed to create MD5 hash for file … th��sard`).
- **`build_app_data.py` heuristics:**
  - a line ending with ":" becomes the sub-header of the lines after it;
  - a `)` that closes a parenthesis that was never opened ends that list;
  - lowercase continuation lines get merged into the previous line.
  They were tuned on the current docx, so read `data/objectifs_report.md` after any source change.
- **Scrambled rows in the old guide** (e.g. `cardiaque chez Décrire un l'aspect…`) are scrambled in the source itself. They are flagged in the report, not fixed.
- **Robolectric must stay on SDK 34** (`android/app/src/test/resources/robolectric.properties`): SDK 35+ needs Java 21, CI uses JDK 17 (`Android SDK 36 requires Java 21`). The `--add-opens=java.base/jdk.internal.access` test flag in `build.gradle.kts` is also needed.
- **Maven Central 429s:** local Gradle runs sometimes hit `Received status code 429 … Too Many Requests`. Retry with `--max-workers=2`; it isn't a code problem.
- **PDF viewer zoom:** gesture detectors sit outside the `graphicsLayer` (screen coordinates), and the LazyColumn's own scroll sits inside it (unscaled). Reordering these modifiers breaks zoom or scrolling. The extra bottom padding while zoomed is what lets you reach the last page.
- **Bitmap recycling:** `PageCache` recycles only bitmaps that aren't displayed (`acquire`/`release` in a `DisposableEffect`). Recycling one still on screen crashes with "trying to use a recycled bitmap".
- **Skip on cleanup passes:** these look removable but aren't.
  - `sources/guide de stage old - compresse.pdf`: 15 MB rasterized copy. The 68 MB original lives only in git-ignored `sources/original/`.
  - `drafts/*.docx`: the text source, not leftovers.
  - `android/distribution/README-installation.txt`: goes into the release zip.
  - `CLAUDE.md`: pointer to this file.
- **Known warnings (harmless today):** the test file uses `createAndroidComposeRule`, deprecated in favor of the `v2` API (which needs explicit test synchronization). Gradle also prints "Deprecated Gradle features… incompatible with Gradle 10", coming from plugins.

## 7. Current State
- **Last shipped:** PR #1 merged into `main` (data pipeline with 1,331 objectives, the full Android app, CI, docs). CI is green on `main`, and the APK artifact builds (debug-signed: no secrets yet).
- **Working on now:** first stable release `v1.0.0`. Blocked until the owner adds the 4 keystore secrets. Prep (`RELEASE.md`, CHANGELOG `[v1.0.0]`, stable-tag guard, release notes from CHANGELOG) is on branch `claude/eager-hawking-5q04vs`.
- **Next up:** see `TRAJECTORY.md`.

## 8. Update Protocol (Verbatim)
> **For the AI Assistant:** When asked to "Update CONTEXT.md":
> 1. Re-run Phase 0 — check for new or changed `CLAUDE.md` / `AGENTS.md` / `GEMINI.md` / `.github/` files, merge what's new, and re-run Phase 3 so each of them still points to `CONTEXT.md`.
> 2. Re-scan the tree, manifests, and `.github/workflows/` for drift.
> 3. Read our recent conversation for new decisions, fragile bits discovered, or shifted goals.
> 4. Refresh the `_Last synced_` line with today's date and current commit SHA.
> 5. Rewrite — do not append. One clean source of truth. Preserve still-true content, revise the rest.
> 6. Keep §1 and §2 in plain English. Keep the file under ~200 lines.
