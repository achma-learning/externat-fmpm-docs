# TRAJECTORY
_Last synced: 2026-10-23 @ 0006055 · built from: my requests (chat) + GitHub issues (none open or closed) + CONTEXT.md §7. No `TODO`/`FIXME` comments in the code._

## 🎯 Final Goal
A native Android app, **"Externat FMPM"**, for my personal phone and tablet. It works **fully offline**, stays **simple**, and **keeps working on future Android versions**. It gives me the FMPM hospital-stage objectives per specialty, year and version (2022 / old) with a checklist and full-text search, plus the full faculty guides, all shareable to WhatsApp and savable. GitHub Actions turns the repo into an installable APK and a downloadable .zip. Updating the content should only mean editing `data/specialites.json`, rerunning the scripts and rebuilding.

**Done when:**
- [ ] Installs and runs on Android 7 → latest, phone and tablet, portrait and landscape, light and dark; works in airplane mode and requests no permissions.
- [ ] All 78 objective PDFs and 6 guides open, share to WhatsApp and save via "Enregistrer".
- [ ] Filters, name search and accent-insensitive full-text search work; checklist, favorites and recents survive restarts **and app updates** (same keystore).
- [ ] CI produces the APK artifact; a `v*` tag produces a Release with `.apk` + `.zip`.
- [ ] No committed file ≥ 25 MB; no APK/keystore in git.

## ✅ To Do

### Now
- [ ] **T014** — Get PR #1 green and merged into `main` — the app only reaches `main` and the default build after the merge · `(chat, CONTEXT §7)`
- [ ] **T015** — "Populate the release in GitHub": first pre-release `v1.0.0-beta.1` with `.apk` + `.zip` · `(chat)`
- [ ] **T016** — Add the keystore secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) — "a fixed keystore is required so updates install over the previous version" · `(chat)`

### Next
- [ ] **T017** — Tag the first stable `v1.0.0` from `main`, signed with the fixed key · start when: T014 + T016 done · `(chat)`
- [ ] **T018** — Run the acceptance checklist on my phone and tablet: PDF rendering and zoom, WhatsApp share, "Enregistrer", 200 % font size, airplane mode — not verifiable in the cloud sandbox · `(chat)`
- [ ] **T019** — Review `data/objectifs_report.md` and fix the flagged rows in `drafts/*.docx` (scrambled old-guide rows, long sub-headers), then rerun `build_app_data.py` · `(chat: "anything ambiguous for me to review")`
- [ ] **T020** — Add screenshots to the README ("screenshots placeholder") · `(chat)`
- [ ] **T021** — Set up the Claude Code pointer file `CLAUDE.md` → `CONTEXT.md` if I say yes · `(chat)`

### Later
- [ ] **T022** — Make objective IDs survive inserting/removing an objective (today they're positional, so ticks can shift) · start when: I edit the docx sources · `(CONTEXT §6)`

## ✔️ Done (latest 10)
- [x] **T001** — Analyse the repo and give a plan before coding · `(chat)`
- [x] **T002** — `scripts/build_app_data.py` → `data/objectifs.json` + `objectifs_report.md`, never inventing objectives, stable IDs · `(a38a66a)`
- [x] **T003** — Python data check run in CI (`scripts/check_data.py`) · `(a38a66a, b30c4e8)`
- [x] **T004** — Kotlin + Compose + Material 3 app skeleton in `android/`, Gradle task copying PDFs into generated assets with ASCII slugs · `(9e7f832)`
- [x] **T005** — Module Objectifs: filters, search (name + full text, accent-insensitive), specialty screen, checklist, source mention · `(f460c50)`
- [x] **T006** — Modules Guides and Favoris & récents, settings (theme, dark pages, about) · `(f460c50)`
- [x] **T007** — PDF viewer on `PdfRenderer`, share via `FileProvider`, save via SAF, no permissions · `(f460c50)`
- [x] **T008** — Phone/tablet: navigation suite, list-detail two-pane, edge-to-edge, predictive back, splash · `(f460c50)`
- [x] **T009** — Unit tests + lint clean · `(f460c50)`
- [x] **T010** — GitHub Actions: APK artifact, tag → Release `.apk` + `.zip`, signing secrets with debug fallback · `(b30c4e8, 0006055)`
<!-- Older done items (T011 README "Application Android" section, T012 android/README.md, T013 .gitignore + 25 MB check) are recorded in CHANGELOG.md (1e37530, 9e7f832). -->

## ❌ Won't Do
- **T023** — Use `others/liste-specialites-fmpm-2024.pdf` in the app — why not: "NOT used by the app" · `(chat)`
- **T024** — Third-party or alpha `androidx.pdf` PDF library, Room, network, analytics, ads, Firebase — why not: excluded by the brief (offline, few stable dependencies) · `(chat)`

## ❓ Questions for me
- The brief asked for branch `feature/android-app`; this session could only push to `claude/eager-hawking-5q04vs` (PR #1). Fine to keep, or rename after merge?
- Should `v1.0.0` wait for the keystore secrets (recommended), or is a debug-signed `v1.0.0` acceptable?

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
