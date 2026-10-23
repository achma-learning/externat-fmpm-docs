# Releases

What users can download, now and before. History of every change: [`CHANGELOG.md`](./CHANGELOG.md). All releases: [GitHub Releases](https://github.com/achma-learning/externat-fmpm-docs/releases).

## Tracks
| Track | Release file(s) | Comes from | Version source | Tag prefix | Users get it from |
|---|---|---|---|---|---|
| Android app | `externat-fmpm-X.Y.Z.apk`, `externat-fmpm-X.Y.Z.zip` (APK + `README-installation.txt`) | built by CI: `./gradlew :app:assembleRelease` in `.github/workflows/android.yml`, never committed | the git tag (`v1.2.3` → `versionName` 1.2.3); `versionCode` = CI run number | `v` | GitHub Releases (sideload) |

The PDFs and JSON in the repo are not a separate track: they ship inside the APK.

## Current versions
| Track | Version | Tag | Date | Download latest | Download exactly this version |
|---|---|---|---|---|---|
| Android app | 1.0.0 | `v1.0.0` | _pending_ | [latest release](https://github.com/achma-learning/externat-fmpm-docs/releases/latest) | [`externat-fmpm-1.0.0.apk`](https://github.com/achma-learning/externat-fmpm-docs/releases/download/v1.0.0/externat-fmpm-1.0.0.apk) |

## Roll back a user
1. Android refuses an APK with a lower `versionCode` than the installed one, so **uninstall first**. This **erases the checklist, favorites and settings**: there is no export yet.
2. Download the older `.apk` from its release page (pinned link: `…/releases/download/<tag>/externat-fmpm-<version>.apk`) and install it.
3. Nothing auto-updates (no store, no update check), so the rollback sticks.

Also: an APK signed with a different key can't be installed over the current one either. All releases use the repo key, so this only concerns APKs from other sources (or debug builds).

## Signing
- Every release APK is signed with the **public key committed in the repo**: `android/signing/externat-fmpm-release.jks`, alias `externat`, password `externat-fmpm` (`android/app/build.gradle.kts`). It's public on purpose: personal project, sharing is fine. No GitHub secrets needed.
- **Never replace or regenerate it:** a new key means every user has to uninstall (checklist lost). Accepted trade-off: anyone could sign an APK that phones accept as an update of this app.

## How to release
**Easiest:** ask Claude Code "release v1.0.1" (or the next version). It does the steps below.

🤖 Agent mode, through CI: no `gh` CLI or release-creating tool is needed, because pushing the tag is the release.
1. CI green on `main` (or on the commit to tag).
2. Move CHANGELOG `[Unreleased]` → `## [vX.Y.Z] — YYYY-MM-DD`. The workflow copies that section into the release notes.
3. `git tag -a vX.Y.Z <sha> -m "Externat FMPM vX.Y.Z"` → `git push origin vX.Y.Z`.
4. The **Android** workflow builds and signs the APK, zips it with `README-installation.txt`, and runs `gh release create` with SHA-256 sums. A tag with a hyphen (`-beta.1`, `-rc.1`) becomes a **pre-release**.
5. Check the release page, then mark the CHANGELOG heading ✅ and add a row below.

Manual fallback (🧭): GitHub → Actions → Android → the tag's run → artifact `externat-fmpm-apk`, then Releases → Draft a new release → choose the tag → drag in the APK.

## Release checklist
- [ ] `python scripts/check_data.py` OK; `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleRelease` green (CI does all three)
- [ ] CHANGELOG section dated `## [vX.Y.Z] — YYYY-MM-DD`
- [ ] Tag pushed; release lists `.apk` + `.zip` with non-zero sizes and SHA-256; one download tested
- [ ] CHANGELOG heading ✅ with the tag SHA; history row added here

## Release history
Newest first.
| Date | Tag | Commit | Files | Notes |
|---|---|---|---|---|
| _pending_ | `v1.0.0` | _pending_ | `.apk`, `.zip` | first release, public repo key |

---

## Update Protocol (Verbatim)
> **For the AI Assistant:** when asked to "release", "ship" or "update RELEASE.md":
> 0. Say which mode you're in (🤖 Agent / 🧭 Manual / Mixed).
> 1. Phase 0: read RELEASE.md, CHANGELOG `[Unreleased]`, version sources, release candidates, tags, releases (Manual: ask me to paste what you can't open).
> 2. Phase 1: ask me what ships — only what RELEASE.md → Tracks doesn't answer yet.
> 3. Phase 2: which tracks changed since their last tag, and the bump for each.
> 4. Phase 3: prepare the bump, the CHANGELOG move, RELEASE.md, the files and the notes (Manual: as numbered file cards).
> 5. Phase 4: show me the plan and **stop**.
> 6. After my OK, Phase 5: ship it (Agent) or walk me through it step by step (Manual), verify, mark ✅, update RELEASE.md.
