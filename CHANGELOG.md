# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the Android app adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). The app version comes from the git tag (`v1.2.3` → `versionName` 1.2.3); content-only changes outside a release get dated sections.

**How to read this:**
- Newest first. Dates are commit dates. The whole history is here; search it (`Ctrl+F`) for an error message, a file name or a feature.
- `(a3f291)` is a short SHA — `git show a3f291` to see the diff.
- Entries explain symptom + cause, not just "fixed."
- Last known-good commit: search for `✅`. The first hit from the top is the newest version or change that was checked and worked.

---

## [Unreleased]
_Changes since the last version. No version tagged yet: the first one will be the app's first release._

### Added
- Android app **Externat FMPM** in `android/` (Kotlin, Compose, Material 3, minSdk 24 / target 37): fully offline, no permissions. `(9e7f832, f460c50)` ✅ verified (f460c50): lint clean, 15/15 tests, R8 release build
- Objectifs module: 43 specialties by category, year/version filters (remembered), accent-insensitive name + full-text search with highlights. `(f460c50)`
- Per-objective checklist saved in DataStore, keyed by stable objective `id`; progress `12 / 20`, `Tout décocher` with confirmation. `(f460c50)`
- PDF viewer on platform `PdfRenderer`: background rendering, LRU bitmap cache, pinch/double-tap zoom with re-render, page jump, `Ouvrir avec une autre application` fallback. `(f460c50)`
- Share PDFs via `FileProvider` with readable names (`Cardiologie - 3A - objectifs de stage 2022.pdf`), save anywhere via SAF. `(f460c50)`
- Guides tab (6 full guides from `data/guides.json`), Favoris & récents (last 20), settings (theme, dynamic color, inverted dark pages). `(9e7f832, f460c50)`
- Tablet / landscape: navigation rail and list-detail two-pane layout. `(f460c50)`
- `data/objectifs.json` — 1,331 objectives extracted from `drafts/*.docx` by `scripts/build_app_data.py`, with `data/objectifs_report.md` for review. `(a38a66a)` ✅ verified (a38a66a): deterministic rerun, `check_data.py` OK
- `scripts/check_data.py` — CI check: every `fichier` exists, objectives match `index.json`, unique IDs. `(a38a66a)`
- GitHub Actions `android.yml`: data check, lint, tests, R8 APK artifact `externat-fmpm-apk`; `v*` tag → Release with `.apk` + `.zip`. `(b30c4e8)`
- Hyphenated tags (`v1.0.0-beta.1`) are published as GitHub pre-releases. `(0006055)`

### Changed
- Architectural: Gradle task `copy<Variant>AppContent` copies repo PDFs/JSON into generated assets with ASCII slugs, so no PDF is duplicated under `android/`. `(9e7f832)`
- README: new "Application Android" section (install, signing, releases, content updates); `drafts/*.docx` are now a data source, no longer "unused drafts". `(1e37530)`

### Fixed
- Build failed with `Failed to create MD5 hash for file … th��sard 2015 fmpm.pdf (No such file or directory)` — JVM ran in a POSIX locale; CI now sets `LC_ALL=C.UTF-8`. `(b30c4e8)`
- Robolectric UI tests would fail on CI with `Android SDK 36 requires Java 21 (have Java 17)` — pinned to SDK 34. `(b30c4e8)`

### Security
- Release signing reads the keystore from CI secrets only; `*.jks`, `*.keystore`, `*.apk`, `local.properties` are git-ignored. `(9e7f832, b30c4e8)`

### Broken
- Without the `KEYSTORE_*` secrets, CI signs with a throwaway debug key: an APK from one run can't be updated by another (uninstall needed, checklist lost). Workaround: add the secrets (README). `(b30c4e8)`
- Not yet run on a real device or emulator: PDF rendering, zoom, WhatsApp share and the SAF save flow are only build/lint-checked. `(f460c50)`

---

## 2026-10-23 — objectives split per specialty
### Added
- 78 objective PDFs in `objective par specialite/` (43 specialties × years × 2022/old) cut from the two stage guides by `scripts/build_objectives.py`. `(da1cd4f)`
- `data/specialites.json` (source of truth, specialty → version → year → pages) and generated `data/index.json`. `(da1cd4f)`
- Full guides in `sources/` (2022 guide, rasterized 15 MB copy of the 68 MB old guide, text list of old objectives) and `others/` (student, resident, thesis guides). `(da1cd4f)`
- Hand-corrected OCR transcriptions of both guides in `drafts/`. `(da1cd4f)`
- README with specialty table between `<!-- TABLE:START -->` / `<!-- TABLE:END -->` (rewritten by the build script). `(da1cd4f)`

---

## 2026-10-23 — the first commit
- Project started: empty repository with `.gitattributes` (`* text=auto`). `(763665d)`

---

_No version tags yet: link references are added once the first `v*` tag exists._
[Unreleased]: https://github.com/achma-learning/externat-fmpm-docs/commits/main

---

## Update Protocol (Verbatim)
> **For the AI Assistant:** When asked to "Update CHANGELOG.md":
> 1. Quick mode (default): find the most recent SHA cited in the existing file. Full rebuild (only if I ask, or there's no file): start from the first commit.
> 2. Run `git log` and `git diff --stat` for everything since that SHA.
> 3. Apply the WIP Decoder — derive intent from diffs when commit messages are vague.
> 4. Group consecutive same-intent commits into single entries; split when intent changes.
> 5. Skip noise (lockfiles, formatter passes, typos, no-op commits).
> 6. Detect version bumps in manifests — if found, move the `[Unreleased]` entries into a `## [vX.Y.Z] — YYYY-MM-DD` section and add its link reference at the bottom.
> 7. Append new entries above existing ones. **Never rewrite past entries** except to fix factual errors or repair the Keep a Changelog shape (and note the correction inline).
> 8. Keep every past section, however old. Never trim, archive or delete history.
> 9. Keep every entry under 2 lines. The file itself has no length limit.
> 10. Add ✅ markers only for tagged versions and for changes I confirmed or a check passed. For "rescue", follow Rescue Mode.
> 11. Follow [Keep a Changelog](https://keepachangelog.com/en/): bucket order, ISO dates, link references, `[YANKED]`, deprecations and breaking changes always listed.
