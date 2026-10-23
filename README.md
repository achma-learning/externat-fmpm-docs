# Externat FMPM — Objectifs de stage

Dépôt de référence des documents de stage de la **Faculté de Médecine et de Pharmacie de Marrakech (FMPM)**, avec les **objectifs de stage classés par spécialité** pour les deux versions du guide de stage :

- **2022** : *Guide de stage hospitalier de l'étudiant — Édition 2022*
- **old** : *Guide de stage de l'étudiant en médecine* (ancienne version, avant la réforme)

Ce dépôt sert aussi de base de données à une future application Android (APK) « Externat FMPM » (voir [Feuille de route](#feuille-de-route)).

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
│   └── index.json                   ← généré : liste de tous les PDF (pour l'app)
└── scripts/build_objectives.py      ← régénère « objective par specialite/ »
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

## Feuille de route

Application Android « Externat FMPM » (dans ce même dépôt), modules prévus :

1. **Objectifs de stage** (fonction principale)
   - recherche par service, filtre par année et par version (2022 / old) à partir de `data/index.json` ;
   - affichage des PDF, téléchargement pour usage hors-ligne ;
   - partage du PDF via WhatsApp ou toute autre application (feuille de partage Android).
2. **Guide de l'étudiant** — éditions 2015 et 2024.
3. **Guide du résident et de l'interne** (2017).
4. **Guide pratique du thésard** (2015).

## Sources

| Fichier | Description |
|---|---|
| `sources/guide de stage 2022.pdf` | Guide de stage hospitalier, édition 2022 (partie objectifs, p. 46-78 imprimées) |
| `sources/guide de stage old - compresse.pdf` | Ancien guide de stage (116 p.), copie compressée |
| `sources/liste objectives - guide stage old.pdf` | Liste des objectifs de l'ancien guide (version texte) |
| `drafts/*.docx` | Transcriptions OCR manuelles des deux guides (brouillons, non utilisés pour les extraits) |
| `others/` | Guides de l'étudiant 2015 et 2024, guide du résident-interne 2017, guide du thésard 2015, liste des spécialités 2024 |
