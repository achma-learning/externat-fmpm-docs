# Externat FMPM — Objectifs de stage

Dépôt de référence des documents de stage de la **Faculté de Médecine et de Pharmacie de Marrakech (FMPM)**, avec les **objectifs de stage classés par spécialité** pour les deux versions du guide de stage :

- **2022** : *Guide de stage hospitalier de l'étudiant — Édition 2022*
- **old** : *Guide de stage de l'étudiant en médecine* (ancienne version, avant la réforme)

Ce dépôt contient aussi l'application Android **« Externat FMPM »** qui embarque ces documents (voir [Application Android](#application-android)).

## Organisation

```
.
├── objective par specialite/        ← un dossier par service, un PDF par année et par version
│   ├── cardiologie/
│   │   ├── 3A - objective de stage 2022.pdf
│   │   ├── 3A - objective de stage old.pdf
│   │   └── 6A - objective de stage old.pdf
│   └── …
├── sources/                         ← guides complets (sources des extraits)
│   ├── guide de stage 2022.pdf
│   ├── guide de stage old - compresse.pdf
│   ├── liste objectives - guide stage old.pdf
│   └── original/                    ← guide old original (68 Mo, non versionné)
├── drafts/                          ← brouillons OCR (.docx) faits à la main
├── others/                          ← autres guides FMPM (étudiant, résident-interne, thésard, spécialités)
├── data/
│   ├── specialites.json             ← source de vérité : spécialité → version → année → pages
│   ├── index.json                   ← généré : liste de tous les PDF (pour l'app)
│   ├── objectifs.json               ← généré : texte de chaque objectif (checklist, recherche)
│   ├── objectifs_report.md          ← généré : comptes par entrée et points à vérifier
│   └── guides.json                  ← les 6 guides complets embarqués dans l'app
├── scripts/
│   ├── build_objectives.py          ← régénère « objective par specialite/ »
│   ├── build_app_data.py            ← régénère data/objectifs.json depuis les docx
│   └── check_data.py                ← contrôle de cohérence (lancé par la CI)
├── android/                         ← application Android (voir android/README.md)
└── .github/workflows/android.yml    ← build de l'APK, artifact et releases
```

### Nommage des fichiers

`<année> - objective de stage <version>.pdf`

| Préfixe | Signification |
|---|---|
| `1A` … `6A` | objectifs du stage en 1ère … 6ème année |
| `4A-5A` | le guide place ce stage en 4ème et 5ème année |
| `3A-6A` | stage effectué de la 3ème à la 6ème année (centre de santé) |
| `XA` | année non précisée par le guide |

Les dossiers sont en minuscules, sans accents ni espaces, pour rester compatibles avec git et l'application.

## Spécialités

Numéros de page = pages du **PDF source** (et non les numéros imprimés).

<!-- TABLE:START -->
| Dossier | Spécialité | Catégorie | 2022 | old |
|---|---|---|---|---|
| `immersion-1ere-annee` | Stage d'immersion dans les structures de soins | 1er cycle | 1A (p3) | 1A (p36) |
| `medecine-sociale-2eme-annee` | Stage de médecine sociale | 1er cycle | 2A (p4) | 2A (p37) |
| `semiologie-soins-infirmiers` | Sémiologie - soins infirmiers | Stages fondamentaux | — | 3A (p39) |
| `stage-fondamental-medecine` | Stage fondamental de médecine | Stages fondamentaux | — | 3A (p41) |
| `stage-fondamental-chirurgie` | Stage fondamental de chirurgie | Stages fondamentaux | — | 3A (p42) |
| `appareil-locomoteur` | Pathologie de l'appareil locomoteur (traumatologie, rhumatologie, réadaptation) | Médecine | 3A (p5-6) | — |
| `cardiologie` | Cardiologie | Médecine | 3A (p12) | 3A (p45-46), 6A (p47-48) |
| `pneumologie` | Pneumo-phtisiologie | Médecine | 3A (p9) | 3A (p58-59), 6A (p58-59) |
| `gastro-enterologie` | Gastro-entérologie | Médecine | 3A (p11) | 3A (p52) |
| `maladies-infectieuses` | Maladies infectieuses | Médecine | 3A (p7) | 3A (p55) |
| `medecine-interne` | Médecine interne | Médecine | — | 3A (p54) |
| `neurologie` | Neurologie | Médecine | — | 3A (p57) |
| `rhumatologie` | Rhumatologie | Médecine | — | 4A (p44) |
| `medecine-physique-readaptation` | Médecine physique et réadaptation fonctionnelle | Médecine | — | XA (p61) |
| `dermatologie` | Dermatologie | Médecine | 4A (p13) | 4A (p49) |
| `oncologie-hematologie` | Cancérologie - hématologie | Médecine | 4A (p16) | 4A (p53-62) |
| `endocrinologie` | Endocrinologie et maladies métaboliques | Médecine | 4A (p18) | 5A (p50-51) |
| `imagerie-medicale` | Imagerie médicale / Radiologie | Médecine | 4A (p19) | 4A (p63-64-86) |
| `nephrologie` | Néphrologie | Médecine | 5A (p22) | 4A-5A (p56) |
| `psychiatrie` | Psychiatrie | Médecine | 5A (p24) | 5A (p60) |
| `chirurgie-viscerale` | Chirurgie viscérale | Chirurgie | 3A (p8) | 3A (p70) |
| `neurochirurgie` | Neurochirurgie | Chirurgie | 3A (p10) | 3A (p72-73) |
| `traumatologie-orthopedie` | Traumatologie-orthopédie | Chirurgie | — | 3A (p71) |
| `chirurgie-cardio-vasculaire` | Chirurgie cardio-vasculaire | Chirurgie | 4A (p17) | 4A (p66-67), 6A (p67) |
| `chirurgie-vasculaire-peripherique` | Chirurgie vasculaire périphérique | Chirurgie | — | 4A (p68-69), 6A (p68-69) |
| `urologie` | Urologie | Chirurgie | 4A (p20) | 4A (p76-77) |
| `orl` | ORL | Chirurgie | 5A (p21) | 5A (p74) |
| `chirurgie-maxillo-faciale` | Stomatologie et chirurgie maxillo-faciale | Chirurgie | 5A (p23) | 5A (p78) |
| `ophtalmologie` | Ophtalmologie | Chirurgie | — | 5A (p75) |
| `chirurgie-plastique` | Chirurgie plastique et réparatrice | Chirurgie | — | 5A (p79) |
| `pediatrie` | Pédiatrie (néonatologie, pédiatrie) | Enfant | 4A (p14) | 4A (p83-84) |
| `chirurgie-pediatrique` | Chirurgie pédiatrique | Enfant | 4A (p15) | 4A (p84-85) |
| `reanimation-pediatrique` | Réanimation pédiatrique | Enfant | — | 4A (p86-87) |
| `gynecologie-obstetrique` | Gynécologie-obstétrique | Gynécologie-obstétrique | 5A (p26), 6A (p27) | 5A (p80), 6A (p80-81) |
| `reanimation` | Urgences et réanimation (anesthésie-réanimation) | Réanimation | 5A (p25) | 5A (p89-90) |
| `parasitologie-mycologie` | Parasitologie-mycologie | Laboratoires | 5A (p30) | 5A (p92) |
| `microbiologie` | Microbiologie | Laboratoires | 5A (p32) | 5A (p93) |
| `anatomie-pathologique` | Anatomie pathologique | Laboratoires | 5A (p31) | 5A (p94) |
| `immunologie` | Immunologie | Laboratoires | 5A (p35) | 5A (p94) |
| `genetique` | Génétique | Laboratoires | 5A (p34) | 5A (p95) |
| `biochimie` | Biochimie | Laboratoires | 5A (p33) | 5A (p95) |
| `hematologie-biologique` | Hématologie biologique (laboratoire) | Laboratoires | — | 5A (p96) |
| `centre-de-sante` | Centre de santé - Médecine de famille | Centres de santé | 3A-6A (p28-29) | 3A-6A (p98) |
<!-- TABLE:END -->

### Choix de classement

- **Années de l'ancien guide** : seuls cardiologie, pneumologie, chirurgie cardio-vasculaire, chirurgie vasculaire périphérique et gynécologie-obstétrique y précisent l'année. Pour les autres services, l'année vient du cursus décrit dans ce même guide (p26-27 : services de 3ème, 4ème et 5ème année).
- **Guide 2022** : centre de santé et laboratoires n'ont pas d'en-tête d'année. Le centre de santé est classé 3A-6A ; les laboratoires 5A (comme dans l'ancien guide).
- **Pages partagées** : quand une page contient deux services ou deux années (ex. gynéco 5A/6A p80, anapath + immunologie p94), la page entière est incluse dans chaque fichier concerné (voir `note` dans `data/specialites.json`).
- **Appareil locomoteur (2022)** : traumatologie, rhumatologie et réadaptation forment un seul tableau dans le guide 2022 → dossier `appareil-locomoteur`. Les services équivalents de l'ancien guide restent dans `traumatologie-orthopedie`, `rhumatologie` et `medecine-physique-readaptation`.
- **Guide old compressé** : l'original (68 Mo, texte vectorisé) dépasse la limite de 25 Mo de GitHub. Une copie rastérisée en niveaux de gris à 200 dpi (~15 Mo) est versionnée et sert de source aux extraits ; l'original reste en local dans `sources/original/` (ignoré par git).

## Régénérer les PDF

```bash
pip install pymupdf
python scripts/build_objectives.py                 # extrait tous les PDF par spécialité
python scripts/build_objectives.py --compress-old  # + régénère la copie compressée du guide old
```

Pour corriger un classement : modifier `data/specialites.json`, puis relancer le script (le dossier `objective par specialite/`, `data/index.json` et le tableau de ce README sont régénérés).

## Application Android

**Externat FMPM** : les objectifs de stage et les guides de la FMPM sur téléphone et tablette, **100 % hors ligne**, **sans aucune permission**, sans publicité ni statistiques.

- **Objectifs de stage** : les 43 spécialités par catégorie, filtres année (1A … 6A) et version (2022 / ancien / les deux, mémorisés), recherche par nom **et dans le texte des objectifs**, sans tenir compte des accents (« gyneco » trouve « Gynécologie »), avec surlignage.
- **Checklist** : chaque objectif se coche ; avancement « 12 / 20 » par entrée et par spécialité ; « Tout décocher » avec confirmation ; conservée après redémarrage et mise à jour.
- **PDF** de chaque entrée dans la visionneuse intégrée (pincer pour zoomer, double tap, « 3 / 12 », aller à la page), **Partager** (WhatsApp ou autre), **Enregistrer** (emplacement au choix), **Favori** ★, source affichée (« Guide de stage 2022, p. 12 »).
- **Guides** : guide de l'étudiant 2024 et 2015, guide du résident et de l'interne 2017, guide du thésard 2015, guide de stage 2022 et ancienne version.
- **Favoris & récents** (20 derniers documents), thème clair / sombre / système, couleurs dynamiques (Android 12+), pages PDF sombres en option.
- Tablette et paysage : rail de navigation et deux volets (liste + détail).

_Captures d'écran : à ajouter._

### Installer l'APK

- **Dernière version publiée** : page [Releases](../../releases) → `externat-fmpm-X.Y.Z.apk` (ou le `.zip` avec la notice d'installation en français).
- **Build de chaque commit** : onglet **Actions** → workflow **Android** → run → artifact `externat-fmpm-apk` (GitHub le fournit en `.zip`).
- Sur le téléphone : ouvrir l'APK, autoriser l'installation depuis cette source (« sources inconnues »), « Installer ». Notice complète : [`android/distribution/README-installation.txt`](android/distribution/README-installation.txt).

### Construire en local

JDK 17+ et le SDK Android (API 37) :

```bash
cd android
export LC_ALL=C.UTF-8          # Linux : noms de fichiers accentués
./gradlew :app:assembleDebug   # -> android/app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Détails (architecture, copie des PDF dans l'APK, tests) : [`android/README.md`](android/README.md).

### Signature : clé fixe obligatoire pour les mises à jour

Android n'installe une nouvelle version par-dessus l'ancienne **que si les deux sont signées avec la même clé**. Sans clé fixe, la CI signe avec une clé de debug **différente à chaque run** : chaque mise à jour imposerait de désinstaller l'app, ce qui **efface la checklist**. À faire une fois :

1. Créer la clé (garder le fichier et les mots de passe en lieu sûr, hors du dépôt) :

   ```bash
   keytool -genkeypair -v -storetype PKCS12 -keystore externat-fmpm.jks -alias externat \
     -keyalg RSA -keysize 4096 -validity 36500 -dname "CN=Externat FMPM"
   ```

   (Avec PKCS12, le mot de passe de la clé est le même que celui du keystore.)

2. Encoder le fichier en base64 :
   - **Linux / macOS** : `base64 -w0 externat-fmpm.jks > keystore.b64` (macOS : `base64 -i externat-fmpm.jks -o keystore.b64`)
   - **Windows (PowerShell)** : `[Convert]::ToBase64String([IO.File]::ReadAllBytes("externat-fmpm.jks")) | Set-Content -NoNewline keystore.b64`

3. GitHub → **Settings → Secrets and variables → Actions → New repository secret** : `KEYSTORE_BASE64` (contenu de `keystore.b64`), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (`externat`), `KEY_PASSWORD` (le même mot de passe). Supprimer ensuite `keystore.b64`.

### Publier une version

Pousser un tag `v*` sur `main` (ex. `git tag v1.0.0 && git push origin v1.0.0`) : la CI crée la release GitHub avec `externat-fmpm-1.0.0.apk` et `externat-fmpm-1.0.0.zip`. Le `versionCode` est le numéro du run (toujours croissant), le `versionName` vient du tag.

### Mettre à jour le contenu

Aucun code à modifier :

1. Corriger `data/specialites.json` (ou les transcriptions dans `drafts/`) ;
2. `python scripts/build_objectives.py` puis `python scripts/build_app_data.py` (`pip install pymupdf python-docx`) ;
3. relire `data/objectifs_report.md`, committer, pousser : la CI construit le nouvel APK.

> Les `id` des objectifs (`cardiologie:2022:3A:01`) suivent l'ordre des objectifs dans chaque entrée : insérer ou retirer un objectif décale les suivants, et les cases déjà cochées après lui.

## Sources

| Fichier | Description |
|---|---|
| `sources/guide de stage 2022.pdf` | Guide de stage hospitalier, édition 2022 (partie objectifs, p. 46-78 imprimées) |
| `sources/guide de stage old - compresse.pdf` | Ancien guide de stage (116 p.), copie compressée |
| `sources/liste objectives - guide stage old.pdf` | Liste des objectifs de l'ancien guide (version texte) |
| `drafts/*.docx` | Transcriptions OCR corrigées à la main des deux guides : source du texte des objectifs (`data/objectifs.json`), pas des extraits PDF |
| `others/` | Guides de l'étudiant 2015 et 2024, guide du résident-interne 2017, guide du thésard 2015, liste des spécialités 2024 |
