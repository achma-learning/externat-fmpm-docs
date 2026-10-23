# TRAJECTORY
_Last synced: 2026-10-23 @ f5eac2f · built from: my requests (chat) + GitHub issues (none open or closed) + CONTEXT.md §7. No `TODO`/`FIXME` comments in the code._

## 🎯 Final Goal
A native Android app, **"Externat FMPM"**, for my personal phone and tablet. It works **fully offline**, stays **simple**, and **keeps working on future Android versions**. It gives me the FMPM hospital-stage objectives per specialty, year and version (2022 / old) with a checklist and full-text search, plus the full faculty guides, all shareable to WhatsApp and savable. GitHub Actions turns the repo into an installable APK and a downloadable .zip. Updating the content should only mean editing `data/specialites.json`, rerunning the scripts and rebuilding.

**Done when:**
- [ ] Installs and runs on Android 7 → latest, phone and tablet, portrait and landscape, light and dark; works in airplane mode and requests no permissions.
- [ ] All 78 objective PDFs and 6 guides open, share to WhatsApp and save via "Enregistrer".
- [ ] Filters, name search and accent-insensitive full-text search work; checklist, favorites and recents survive restarts **and app updates** (same repo key).
- [ ] CI produces the APK artifact; a `v*` tag produces a Release with `.apk` + `.zip`.
- [ ] No committed file ≥ 25 MB; no APK in git (only the public release key is committed, on purpose).

## ✅ To Do

### Now
- [ ] **T025** — "Make the key publique": commit the release key, drop GitHub secrets — "i just want to share the apps… keep it simple" · `(chat)`
- [ ] **T017** — "Release v1.0.0 directly": merge the release-prep PR, tag `v1.0.0` on `main`, verify the Release has `.apk` + `.zip` · start when: T025 merged · `(chat)`

### Next
- [ ] **T018** — Run the acceptance checklist on my phone and tablet: PDF rendering and zoom, WhatsApp share, "Enregistrer", 200 % font size, airplane mode — not verifiable in the cloud sandbox · `(chat)`
- [ ] **T019** — Review `data/objectifs_report.md` and fix the flagged rows in `drafts/*.docx` (scrambled old-guide rows, long sub-headers), then rerun `build_app_data.py` · `(chat: "anything ambiguous for me to review")`
- [ ] **T020** — Add screenshots to the README ("screenshots placeholder") · `(chat)`

### Later
- [ ] **T022** — Make objective IDs survive inserting/removing an objective (today they're positional, so ticks can shift) · start when: I edit the docx sources · `(CONTEXT §6)`

## ✔️ Done (latest 10)
- [x] **T021** — Claude Code pointer file `CLAUDE.md` → `CONTEXT.md` ("do what is recommended") · `(chat)`
- [x] **T014** — PR #1 green and merged into `main` · `(f5eac2f, #1)`
- [x] **T003** — Python data check run in CI (`scripts/check_data.py`) · `(a38a66a, b30c4e8)`
- [x] **T004** — Kotlin + Compose + Material 3 app skeleton in `android/`, Gradle task copying PDFs into generated assets with ASCII slugs · `(9e7f832)`
- [x] **T005** — Module Objectifs: filters, search (name + full text, accent-insensitive), specialty screen, checklist, source mention · `(f460c50)`
- [x] **T006** — Modules Guides and Favoris & récents, settings (theme, dark pages, about) · `(f460c50)`
- [x] **T007** — PDF viewer on `PdfRenderer`, share via `FileProvider`, save via SAF, no permissions · `(f460c50)`
- [x] **T008** — Phone/tablet: navigation suite, list-detail two-pane, edge-to-edge, predictive back, splash · `(f460c50)`
- [x] **T009** — Unit tests + lint clean · `(f460c50)`
- [x] **T010** — GitHub Actions: APK artifact, tag → Release `.apk` + `.zip`, signing secrets with debug fallback · `(b30c4e8, 0006055)`
<!-- Older done items (T001 plan, T002 objectifs.json, T011 README section, T012 android/README.md, T013 .gitignore + 25 MB check) are recorded in CHANGELOG.md (a38a66a, 1e37530, 9e7f832). -->

## ❌ Won't Do
- **T016** — Keystore in GitHub secrets — why not: owner chose "make the key publique" to keep sharing and updates simple · `(chat)`
- **T015** — Debug-signed pre-release `v1.0.0-beta.1` — why not: owner chose "add the keystore secrets first and release v1.0.0 directly" · `(chat)`
- **T023** — Use `others/liste-specialites-fmpm-2024.pdf` in the app — why not: "NOT used by the app" · `(chat)`
- **T024** — Third-party or alpha `androidx.pdf` PDF library, Room, network, analytics, ads, Firebase — why not: excluded by the brief (offline, few stable dependencies) · `(chat)`

## ❓ Questions for me
- The brief asked for branch `feature/android-app`; this session could only push to `claude/eager-hawking-5q04vs` (PR #1). Fine to keep, or rename after merge?

---

## Update Protocol (Verbatim)
> **For the AI Assistant:** when asked to "Update TRAJECTORY.md":
> 1. Re-run Phase 0: the existing file, this conversation, all GitHub issues, CHANGELOG, git log since the last sync, TODO comments.
> 2. New requests or issues → new items with the next free ID (`T` + number). IDs never change and are never reused.
> 3. Finished items → tick them and move them to **Done** with the commit or PR. Keep only the latest 10 there; older ones are already in CHANGELOG.
> 4. An issue closed as not planned, or an item I rejected → move it to **Won't Do** with the reason.
> 5. Re-order **Next** by value to me ÷ effort, and say in one line what moved and why.
> 6. Rewrite the **Final Goal** only if my requests changed the direction. Then say what changed.
> 7. Refresh the `_Last synced_` line.
