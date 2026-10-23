# Externat FMPM — objectifs de stage

> Les objectifs de stage hospitalier de la FMPM (Marrakech), découpés par spécialité et par année, et une appli Android pour les cocher hors ligne.

**Démarrage rapide :** 1. télécharger `externat-fmpm-X.Y.Z.apk` dans [Releases](../../releases) · 2. l'ouvrir sur le téléphone et autoriser « sources inconnues » · 3. choisir une spécialité, cocher ses objectifs.

_TODO : ajouter une capture d'écran (liste des spécialités + checklist)._

## Ce que c'est

Les objectifs de stage sont noyés dans deux longs guides PDF : le **guide de stage hospitalier 2022** et l'**ancien guide**. Ce dépôt les découpe en un PDF par service, par année et par version (78 fichiers, 43 spécialités), en extrait le texte, et l'application Android **Externat FMPM** les rend consultables partout : checklist par objectif, recherche dans le texte, PDF, partage WhatsApp. Elle fonctionne **sans internet et sans aucune permission**. Tout le contenu est dans l'APK, rien ne sort du téléphone.

Elle embarque aussi les guides complets : guide de l'étudiant 2024 et 2015, guide du résident et de l'interne 2017, guide pratique du thésard 2015, guide de stage 2022 et ancienne version.

## Installer et lancer

**Sur le téléphone (Android 7.0 ou plus) :** prendre l'APK sur la page [Releases](../../releases) (ou l'artifact `externat-fmpm-apk` d'un run de l'onglet **Actions**), l'ouvrir, autoriser l'installation depuis cette source, « Installer ». Notice détaillée : [`android/distribution/README-installation.txt`](android/distribution/README-installation.txt).

**Construire soi-même** (JDK 17+, SDK Android API 37, Python 3) :

```bash
pip install pymupdf python-docx
python scripts/build_objectives.py    # PDF par spécialité + data/index.json + tableau ci-dessous
python scripts/build_app_data.py      # texte des objectifs -> data/objectifs.json
python scripts/check_data.py          # contrôle de cohérence

cd android
export LC_ALL=C.UTF-8                 # Linux : noms de fichiers accentués
./gradlew :app:assembleDebug          # -> android/app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Les APK sont signés avec une clé **publique, versionnée dans le dépôt** (`android/signing/`) : pas de secret à configurer, et chaque nouvelle version s'installe par-dessus l'ancienne sans perdre la checklist. Publier une version : [`RELEASE.md`](RELEASE.md) (ou demander à Claude Code « release v1.0.1 »).

## Utilisation

1. Onglet **Objectifs** : filtrer par année (`5A`) et par version (`2022`, `Ancien`, `Les deux`). Le choix est mémorisé.
2. Toucher **Gynécologie-obstétrique**, puis l'onglet `6A · 2022`.
3. Cocher « Pratiquer un accouchement normal » : la barre passe à `1 / 12`.
4. Basculer sur **PDF** pour voir la page du guide : pincer pour zoomer, toucher « 3 / 12 » pour aller à une page. ★ pour les favoris, **Partager** pour l'envoyer sur WhatsApp, **Enregistrer** pour en garder une copie.

La recherche ignore les accents et cherche aussi dans le texte : « hemorragie post partum » trouve les objectifs de gynéco 6A et de l'ancien guide.

Sur tablette ou en paysage, la liste et le détail s'affichent côte à côte.

## Organisation du dépôt

```
objective par specialite/<dossier>/<année> - objective de stage <2022|old>.pdf   ← 78 extraits
sources/          guides de stage complets (2022, ancien compressé, liste texte des objectifs)
others/           guides de l'étudiant 2015/2024, résident-interne 2017, thésard 2015
drafts/           transcriptions corrigées à la main (.docx) : source du texte des objectifs
data/             specialites.json (source de vérité), index.json, objectifs.json, guides.json
scripts/          build_objectives.py, build_app_data.py, check_data.py
android/          l'application (voir android/README.md)
```

Nom des fichiers : `1A` … `6A` = année du stage ; `4A-5A` = le guide place le stage en 4ème et 5ème année ; `3A-6A` = de la 3ème à la 6ème année (centre de santé) ; `XA` = année non précisée par le guide. Les dossiers sont en minuscules, sans accents ni espaces.

**Mettre à jour le contenu** (aucun code à modifier) : corriger `data/specialites.json` ou les `drafts/*.docx`, relancer les deux scripts, relire `data/objectifs_report.md`, pousser. La CI reconstruit l'APK. Attention : les `id` des objectifs suivent leur ordre dans chaque entrée, donc en insérer un décale les cases déjà cochées après lui.

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
- **Guide old compressé** : l'original (68 Mo, texte vectorisé) dépasse la limite de 25 Mo de GitHub. Une copie rastérisée en niveaux de gris à 200 dpi (~15 Mo) est versionnée et sert de source aux extraits ; l'original reste en local dans `sources/original/` (ignoré par git, `python scripts/build_objectives.py --compress-old` pour la régénérer).

## Nouveautés

Première version de l'application Android : checklist, recherche plein texte, visionneuse PDF, guides, favoris, APK construit par GitHub Actions. [Historique complet →](CHANGELOG.md)

## Pourquoi

En stage, je voulais savoir en deux secondes ce que je dois valider dans le service où je suis, sans fouiller un PDF de 116 pages ni dépendre du réseau de l'hôpital. Et pouvoir l'envoyer au groupe WhatsApp.

## Licence

Pas encore de licence. Les guides et objectifs sont des documents de la Faculté de Médecine et de Pharmacie de Marrakech. Application personnelle, non officielle.

## Voir aussi

- [`CONTEXT.md`](CONTEXT.md) — pour les assistants IA qui reprennent le projet
- [`CHANGELOG.md`](CHANGELOG.md) — ce qui a changé et quand
- [`TRAJECTORY.md`](TRAJECTORY.md) — objectif final et liste des choses à faire
- [`RELEASE.md`](RELEASE.md) — ce qu'on peut télécharger, et comment publier une version
- [`android/README.md`](android/README.md) — architecture de l'app, signature, tests
