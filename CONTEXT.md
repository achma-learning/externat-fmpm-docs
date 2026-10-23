# Externat FMPM — AI Context File
_Last synced: 2026-10-23 @ 0006055_

## 1. What This Is (Plain English)
- **In one sentence:** a pocket library for Marrakech medical students. It holds the official hospital-internship objectives, split by specialty and year, plus the faculty's guides, and an Android app that shows them offline with a tick-box checklist.
- **Why it exists:** the objectives are buried in two long PDF guides (2022 and an older one). This repo cuts them into one PDF per specialty/year, extracts their text, and puts everything in a phone app.
- **Who uses it:** the owner (a FMPM student) on their own phone and tablet, possibly friends via the APK. Not published on the Play Store, not an official faculty app.
- **Vibe:** polished personal tool. Careful with data accuracy (medical objectives must never be invented or paraphrased), relaxed about everything else.

## 2. How To Run It
- **Setup once:**
  - Python 3 + `pip install pymupdf python-docx` (data scripts).
  - JDK 17+ and the Android SDK (API 37) for the app. Point `android/local.properties` at it (`sdk.dir=…`, not committed) or set `ANDROID_HOME`.
  - Linux only: `export LC_ALL=C.UTF-8` before Gradle, or file names with accents break the build.
- **Regenerate the data:** `python scripts/build_objectives.py`, then `python scripts/build_app_data.py`, then `python scripts/check_data.py`.
- **Run the app:** `cd android && ./gradlew :app:assembleDebug` → `android/app/build/outputs/apk/debug/app-debug.apk`, install on a phone.
- **Tests + lint:** `cd android && ./gradlew :app:testDebugUnitTest :app:lintDebug`.
- **Build / deploy:** push to `main` → GitHub Actions builds the APK artifact `externat-fmpm-apk`. Push a tag `v*` → a GitHub Release with `.apk` + `.zip` (`.github/workflows/android.yml`).
- **Required env vars / secrets** (CI only, names only): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The build itself reads `VERSION_CODE`, `VERSION_NAME`, `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (`android/app/build.gradle.kts:12-16`).

## 3. Tech Stack
- **Data side:** Python 3, `pymupdf` (PDF cut/compress/text), `python-docx` (reads the hand-corrected transcriptions).
- **App:** Kotlin 2.4.21, Jetpack Compose (BOM 2026.09.00, Material 3 1.4.0), AGP 9.4.1 with built-in Kotlin, Gradle 9.8.2, compileSdk/targetSdk 37, minSdk 24 (`android/gradle/libs.versions.toml`).
- **Load-bearing libs:** navigation-compose 2.10.2, material3-adaptive 1.3.0 (list-detail), material3-adaptive-navigation-suite, DataStore Preferences 1.3.0, kotlinx-serialization-json 1.12.0. PDF display uses the platform `android.graphics.pdf.PdfRenderer` (no PDF library).
- **Tests:** JUnit 4, Robolectric 4.17 (runs on SDK 34 so it works under Java 17), Compose ui-test.
- **What kind of project:** content repo (PDFs + JSON) + single-module Android app + CI.
- **External services:** none at runtime (no network permission). GitHub Actions for builds/releases only.

## 4. Code Map (The Important Files Only)
- `data/specialites.json` — **source of truth**: specialty → version → year → source pages. Edit this, never `index.json`.
- `scripts/build_objectives.py` — cuts the guides into `objective par specialite/`, rewrites `data/index.json` and the README table between `<!-- TABLE:START -->` / `<!-- TABLE:END -->`.
- `scripts/build_app_data.py` — docx → `data/objectifs.json` + `data/objectifs_report.md`. Watch out: heading → specialty mapping tables (`HEADINGS_2022`, `HEADINGS_OLD`, `SWITCH_OLD`) are hand-written.
- `scripts/check_data.py` — CI sanity check (files exist, IDs unique, objectives match entries).
- `data/guides.json` — the 6 full guides shipped in the app.
- `android/app/build.gradle.kts` — build config + `CopyAppContentTask`: copies repo PDFs/JSON into generated assets with ASCII slugs.
- `android/app/src/main/java/io/github/achmalearning/externat/MainActivity.kt` — entry point (splash, edge-to-edge, theme).
- `…/ui/ExternatApp.kt` — navigation (bottom bar / rail, routes).
- `…/data/Catalog.kt`, `Filters.kt`, `Search.kt` — pure logic, unit-tested.
- `…/data/UserPreferences.kt` — DataStore: checklist, favorites, recents, settings.
- `…/ui/viewer/PdfViewer.kt` + `PdfDocument.kt` — the PDF viewer (zoom, cache, rendering).
- `android/app/src/test/…/CatalogTest.kt`, `AppSmokeTest.kt` — data tests + Robolectric UI smoke tests.
- `.github/workflows/android.yml` — CI, signing fallback, release creation.
- `android/README.md` — app architecture in more detail.

## 5. Rules For Editing This Code
- **Never invent, reword or "fix the meaning" of an objective.** Only light cleanup (hyphenation, spaces, merged lines). Real errors in the sources get fixed in `drafts/*.docx`, then the script is rerun.
- Content changes = edit `data/specialites.json` or `drafts/*.docx` → run both build scripts → commit the generated files. No app code changes for content.
- Never commit APKs, AABs, keystores, `local.properties` or build folders (`.gitignore`). Every committed file must stay under 25 MB (GitHub web upload limit).
- Don't copy PDFs into `android/`: the Gradle task pulls them from the repo at build time.
- App: no network, no runtime permissions, no analytics, no Firebase. Only stable AndroidX/Jetpack libraries, no alpha. New dependencies need a reason.
- UI text is in French and goes in `res/values/strings.xml` (plurals where a count is shown).
- Lint must stay at "No issues found" (`abortOnError = true`).
- Keep the `<!-- TABLE:START -->` / `<!-- TABLE:END -->` markers in `README.md`.

## 6. Fragile Bits & Landmines
- **Objective IDs are positional** (`cardiologie:2022:3A:01` = 1st objective of that entry). Inserting or removing an objective in a docx shifts the later IDs, so already-ticked boxes move to the wrong objective. Prefer appending, or accept the shift knowingly.
- **Signing key = update path.** Without the fixed keystore secrets, CI signs with a throwaway debug key. Android then refuses to update over an older install, and uninstalling erases the checklist.
- **Accented file names:** `others/Guide-de-létudiant-FMPM-2024.pdf` is stored NFD (decomposed accent) in git, the others NFC. The copy task matches names after Unicode normalization (`build.gradle.kts`, `resolve()`). The JVM also needs a UTF-8 locale on Linux (symptom: `Failed to create MD5 hash for file … th��sard`).
- **`build_app_data.py` heuristics:** lines ending with ":" become sub-headers for the following lines. A `)` that closes an unopened parenthesis ends that list. Lowercase continuation lines get merged. These were tuned on the current docx; check `data/objectifs_report.md` after any source change.
- **Scrambled rows in the old guide** (e.g. `cardiaque chez Décrire un l'aspect…`) are scrambled in the source itself (Word export). They are flagged in the report, not fixed.
- **Robolectric must stay on SDK 34** (`android/app/src/test/resources/robolectric.properties`). SDK 35+ needs Java 21, and CI uses JDK 17 (`Android SDK 36 requires Java 21`). The `--add-opens=java.base/jdk.internal.access` flag in `build.gradle.kts` is also needed for it.
- **PDF viewer zoom:** gesture detectors sit outside the `graphicsLayer` (screen coordinates), and the LazyColumn's own scroll sits inside it (unscaled). Moving the modifiers around breaks zoom or scrolling. The extra bottom padding while zoomed is what lets you reach the last page.
- **Bitmap recycling:** `PageCache` recycles only bitmaps that aren't displayed (`acquire`/`release` in a `DisposableEffect`). Recycling a bitmap still on screen crashes with "trying to use a recycled bitmap".
- **Skip on cleanup passes:** `sources/guide de stage old - compresse.pdf` (15 MB rasterized copy; the 68 MB original lives only in git-ignored `sources/original/`), `drafts/*.docx` (they are the text source, not leftovers), `android/distribution/README-installation.txt` (goes into the release zip).

## 7. Current State
- **Last shipped:** data pipeline (`objectifs.json`, 1,331 objectives), the full Android app, CI with APK artifact + tag releases, docs (PR #1, branch `claude/eager-hawking-5q04vs`).
- **Working on now:** getting PR #1 green and merged; first GitHub pre-release `v1.0.0-beta.1` (debug-signed).
- **Next up:** see `TRAJECTORY.md`.

## 8. Update Protocol (Verbatim)
> **For the AI Assistant:** When asked to "Update CONTEXT.md":
> 1. Re-run Phase 0 — check for new or changed `CLAUDE.md` / `AGENTS.md` / `GEMINI.md` / `.github/` files, merge what's new, and re-run Phase 3 so each of them still points to `CONTEXT.md`.
> 2. Re-scan the tree, manifests, and `.github/workflows/` for drift.
> 3. Read our recent conversation for new decisions, fragile bits discovered, or shifted goals.
> 4. Refresh the `_Last synced_` line with today's date and current commit SHA.
> 5. Rewrite — do not append. One clean source of truth. Preserve still-true content, revise the rest.
> 6. Keep §1 and §2 in plain English. Keep the file under ~200 lines.
