"""Génère data/objectifs.json (texte de chaque objectif) pour la checklist et la recherche de l'app.

Usage (depuis la racine du dépôt) :
    python scripts/build_app_data.py

Dépendances : pip install pymupdf python-docx

Sources :
  - 2022 : drafts/Guide_de_stage_hospitalier_2022_OCR.docx (tableaux propres) ;
           le texte du PDF 2022 sert uniquement de contrôle croisé (rapport).
  - old  : drafts/Guide_de_stage_FMPM_fusionne.docx (même contenu que
           sources/liste objectives - guide stage old.pdf, mais en tableaux).

Règles : aucun objectif inventé ni reformulé. Nettoyage léger seulement (puces,
espaces, coupures de mots « médica- menteuses », lignes coupées). Une ligne de
cellule = un objectif ; une ligne qui se termine par « : » (« Démarche
diagnostique devant : ») devient l'en-tête (rubrique) des lignes qui la suivent.

Les identifiants sont « dossier:version:annee:NN » (NN = rang dans l'entrée) :
relancer le script sur des sources inchangées redonne les mêmes identifiants.
Écrit aussi data/objectifs_report.md (comptes, entrées vides, points à vérifier).
"""
import json
import re
import unicodedata
from collections import defaultdict
from pathlib import Path

import docx
import pymupdf
from docx.table import Table
from docx.text.paragraph import Paragraph

ROOT = Path(__file__).resolve().parent.parent
INDEX = ROOT / "data" / "index.json"
OUT = ROOT / "data" / "objectifs.json"
REPORT = ROOT / "data" / "objectifs_report.md"
DOCX_2022 = ROOT / "drafts" / "Guide_de_stage_hospitalier_2022_OCR.docx"
DOCX_OLD = ROOT / "drafts" / "Guide_de_stage_FMPM_fusionne.docx"
PDF_2022 = ROOT / "sources" / "guide de stage 2022.pdf"

# Pages du PDF 2022 sans couche texte exploitable (le docx est la seule source).
PDF_2022_NO_TEXT = {9, 10, 11}


def key(text: str) -> str:
    """Clé de comparaison : minuscules, sans accents, apostrophes et espaces normalisés."""
    text = unicodedata.normalize("NFKD", text.replace("’", "'"))
    text = "".join(c for c in text if not unicodedata.combining(c)).lower()
    return re.sub(r"\s+", " ", text).strip(" :.")


# Titre de section du docx -> entrées index.json qui reçoivent ses objectifs.
# ("keep", rubrique) : même cible que la section précédente, avec une rubrique.
HEADINGS_2022 = {
    "1/ stage d'immersion dans les structures de soins en 1ere annee": [("immersion-1ere-annee", "1A")],
    "2/ stage d'immersion de medecine sociale en 2eme annee": [("medecine-sociale-2eme-annee", "2A")],
    "pathologie de l'appareil locomoteur": [("appareil-locomoteur", "3A")],
    "maladies infectieuses": [("maladies-infectieuses", "3A")],
    "chirurgie viscerale": [("chirurgie-viscerale", "3A")],
    "pneumologie": [("pneumologie", "3A")],
    "neurochirurgie": [("neurochirurgie", "3A")],
    "gastro-enterologie": [("gastro-enterologie", "3A")],
    "cardiologie": [("cardiologie", "3A")],
    "dermatologie": [("dermatologie", "4A")],
    "a- pediatrie": [("pediatrie", "4A")],
    "b- chirurgie pediatrique": [("chirurgie-pediatrique", "4A")],
    "cancerologie hematologie": [("oncologie-hematologie", "4A")],
    "chirurgie cardio-vasculaire": [("chirurgie-cardio-vasculaire", "4A")],
    "endocrinologie": [("endocrinologie", "4A")],
    "imagerie medicale": [("imagerie-medicale", "4A")],
    "urologie": [("urologie", "4A")],
    "orl": [("orl", "5A")],
    "nephrologie": [("nephrologie", "5A")],
    "chirurgie maxillo-faciale": [("chirurgie-maxillo-faciale", "5A")],
    "psychiatrie": [("psychiatrie", "5A")],
    "urgence et reanimation": [("reanimation", "5A")],
    "gynecologie obstetrique 5eme annee": [("gynecologie-obstetrique", "5A")],
    "gynecologie obstetrique 6eme annee": [("gynecologie-obstetrique", "6A")],
    "centre de sante « medecine de famille»": [("centre-de-sante", "3A-6A")],
    "parasitologie-mycologie": [("parasitologie-mycologie", "5A")],
    "anatomie pathologique": [("anatomie-pathologique", "5A")],
    "laboratoire de microbiologie": [("microbiologie", "5A")],
    "laboratoire de biochimie": [("biochimie", "5A")],
    "genetique": [("genetique", "5A")],
    "immunologie": [("immunologie", "5A")],
}

HEADINGS_OLD = {
    "stage d'immersion dans les structures de soin 1ere annee": [("immersion-1ere-annee", "1A")],
    "stage de medecine sociale — 2eme annee": [("medecine-sociale-2eme-annee", "2A")],
    "stage de semiologie et des soins infirmiers": [("semiologie-soins-infirmiers", "3A")],
    "stage fondamental de medecine": [("stage-fondamental-medecine", "3A")],
    "stage fondamental de chirurgie": [("stage-fondamental-chirurgie", "3A")],
    "service de rhumatologie": [("rhumatologie", "4A")],
    "service de cardiologie : 3eme annee": [("cardiologie", "3A")],
    "service de cardiologie : 6eme annee": [("cardiologie", "6A")],
    "service de dermatologie": [("dermatologie", "4A")],
    "service d'endocrinologie et maladies metaboliques": [("endocrinologie", "5A")],
    "service de gastro-enterologie": [("gastro-enterologie", "3A")],
    "service d'hematologie clinique": [("oncologie-hematologie", "4A")],
    "service de medecine interne": [("medecine-interne", "3A")],
    "service de maladies infectieuses": [("maladies-infectieuses", "3A")],
    "service de nephrologie": [("nephrologie", "4A-5A")],
    "service de neurologie": [("neurologie", "3A")],
    "service de pneumophtisiologie - stage en 3eme annee": [("pneumologie", "3A")],
    # p59 de l'ancien guide (tâches d'apprentissage, ARC) est commune à 3A et 6A.
    "taches d'apprentissage": [("pneumologie", "3A"), ("pneumologie", "6A")],
    "service de psychiatrie": [("psychiatrie", "5A")],
    "service de medecine physique et readaptation fonctionnelle": [("medecine-physique-readaptation", "XA")],
    "service de radiotherapie-oncologie": [("oncologie-hematologie", "4A")],
    "service de radiologie": [("imagerie-medicale", "4A")],
    "service de chirurgie cardiovasculaire - stage en 4eme annee": [("chirurgie-cardio-vasculaire", "4A")],
    "chirurgie vasculaire peripherique - stage en 4eme annee": [("chirurgie-vasculaire-peripherique", "4A")],
    "service de chirurgie viscerale": [("chirurgie-viscerale", "3A")],
    "service de traumatologie-orthopedie": [("traumatologie-orthopedie", "3A")],
    "service de neurochirurgie": [("neurochirurgie", "3A")],
    "service d'orl": [("orl", "5A")],
    "service d'ophtalmologie": [("ophtalmologie", "5A")],
    "service d'urologie": [("urologie", "4A")],
    "service de stomatologie et chirurgie maxillo-faciale": [("chirurgie-maxillo-faciale", "5A")],
    "service de chirurgie plastique": [("chirurgie-plastique", "5A")],
    "service de gynecologie obstetrique - stage en 5eme annee": [("gynecologie-obstetrique", "5A")],
    "services de l'enfant": [("pediatrie", "4A")],
    "pediatrie": [("pediatrie", "4A")],
    "chirurgie pediatrique": [("chirurgie-pediatrique", "4A")],
    "radiologie pediatrique / imagerie de l'enfant": [("imagerie-medicale", "4A")],
    "service de reanimation pediatrique": [("reanimation-pediatrique", "4A")],
    "service de reanimation anesthesie": [("reanimation", "5A")],
    "themes neurologiques": ("keep", "Thèmes neurologiques"),
    "service de parasitologie": [("parasitologie-mycologie", "5A")],
    "service de microbiologie": [("microbiologie", "5A")],
    "service d'anatomo-pathologie": [("anatomie-pathologique", "5A")],
    "service d'immunologie": [("immunologie", "5A")],
    "service de genetique": [("genetique", "5A")],
    "service d'hematologie": [("hematologie-biologique", "5A")],
    "centre de sante": [("centre-de-sante", "3A-6A")],
}

# Ligne fusionnée d'un tableau old qui change de cible (année ou service).
SWITCH_OLD = {
    "service de pneumophtisiologie - stage en 6eme annee": [("pneumologie", "6A")],
    "service de chirurgie cardiovasculaire - stage en 6eme annee": [("chirurgie-cardio-vasculaire", "6A")],
    "chirurgie vasculaire peripherique - stage en 6eme annee": [("chirurgie-vasculaire-peripherique", "6A")],
    "service de gynecologie obstetrique - stage en 6eme annee": [("gynecologie-obstetrique", "6A")],
    "service de biochimie": [("biochimie", "5A")],
}
# p69 (séances d'ARC) de la chirurgie vasculaire périphérique est commune à 4A et 6A.
SHARED_ARC_OLD = {"chirurgie-vasculaire-peripherique": [("chirurgie-vasculaire-peripherique", "4A"),
                                                        ("chirurgie-vasculaire-peripherique", "6A")]}

# Colonnes à ignorer (méthodes, évaluation, validation) : seul « objectif » est gardé.
DROP_COLUMNS = re.compile(r"methode|methodologie|evaluation|validation|date|moyen", re.I)
BULLET = re.compile(r"^\s*(?:[•●▪◦\-–]|[a-z]\.|\d+[.)])\s+|^\s*[•●▪◦]\s*")
# Infinitifs en tête de phrase : un tel verbe au milieu d'une ligne signale un ordre de mots cassé.
VERB = re.compile(r"(?<!^)(?<!ARC : )(?<!et )\b(?:Décrire|Démarche|Reconnaître|Reconnaitre|Argumenter|Interpréter|Réaliser|"
                  r"Rédiger|Identifier|Poser|Évaluer|Elaborer|Élaborer)\b")
JOIN_PREFIXES = ("logi", "mie")


class Report:
    def __init__(self):
        self.lines = defaultdict(list)

    def add(self, section: str, text: str):
        self.lines[section].append(text)


REPORT_DATA = Report()


def clean(text: str) -> str:
    text = text.replace(" ", " ").replace("\t", " ")
    text = re.sub(r"\s+", " ", text).strip()

    def dehyphen(m):
        left, right = m.group(1), m.group(2)
        if left[-1] in "oé" and not right.startswith(JOIN_PREFIXES):
            return f"{left}-{right}"  # mot composé : céto-acidosique, péritonéo-vaginal
        return left + right  # coupure de ligne : médica- menteuses
    text = re.sub(r"(\w+)- (\w+)", dehyphen, text)
    text = re.sub(r"\s+([,.;)])", r"\1", text)
    text = re.sub(r"\(\s+", "(", text)
    return text.strip()


def strip_bullet(text: str) -> tuple[bool, str]:
    m = BULLET.match(text)
    return (True, text[m.end():]) if m else (False, text)


DANGLING = re.compile(r"\b(?:un|une|le|la|les|de|des|du|et|en|d’une|d'une|d’un|d'un)$")


def is_continuation(prev: str, text: str, in_list: bool) -> bool:
    """Une ligne sans puce prolonge la précédente quand la phrase a été coupée."""
    if prev.endswith(":"):
        return False
    return (len(prev.split()) <= 1  # « une » / « SPM, une Adénomegalie… »
            or text.startswith("(")  # « Vomissement » / « (argumenter… »
            or prev.count("(") > prev.count(")")  # parenthèse ouverte sur la ligne suivante
            or len(text.split()) <= 1 and text[0].islower()
            or bool(DANGLING.search(prev))
            # dans une liste « … devant : », chaque ligne en minuscule est un élément
            or not in_list and text[0].islower() and not re.search(r"[.,;]$", prev))


def cell_items(paragraphs: list[str], context: str) -> list[tuple[str | None, str]]:
    """Découpe une cellule « objectif » en (sous-rubrique, objectif)."""
    lines: list[list] = []  # [bullet, text]
    for raw in paragraphs:
        raw = raw.strip()
        if not raw:
            continue
        bullet, text = strip_bullet(raw)
        text = text.strip()
        if not text:
            continue
        in_list = any(t.endswith(":") for _, t in lines)
        if lines and not bullet and is_continuation(lines[-1][1], text, in_list):
            lines[-1][1] = f"{lines[-1][1]} {text}"
            continue
        lines.append([bullet, text])
    if len(lines) >= 1 and len(lines) > 1 and not lines[0][0] and all(b for b, _ in lines[1:]) \
            and len(lines[0][1].split()) <= 3 and not lines[0][1].endswith(":"):
        lines[0][1] += " :"  # « Traumatologie » suivi d'une liste à puces = en-tête

    items: list[tuple[str | None, str]] = []
    header, header_used = None, False
    for _, text in lines:
        text = clean(text)
        if text.endswith(":") and len(text) > 1:
            if header and not header_used:
                items.append((None, header))  # en-tête sans élément : c'est un objectif
            header, header_used = text.rstrip(" :"), False
            continue
        items.append((header, text))
        header_used = header_used or header is not None
        if header:
            group = " ".join(t for h, t in items if h == header)
            if group.count(")") > group.count("(") + header.count("("):
                header = None  # « …champs stériles) » referme la liste ouverte par l'en-tête
    if header and not header_used:
        items.append((None, header))
    return items


def row_cells(row) -> list:
    """Cellules distinctes d'une ligne (les cellules fusionnées n'apparaissent qu'une fois)."""
    out, seen = [], set()
    for c in row.cells:
        out.append(None if c._tc in seen else c)
        seen.add(c._tc)
    return out


def paragraphs_of(cell) -> list[str]:
    return [p.text for p in cell.paragraphs] if cell is not None else []


def join_rubrique(*parts) -> str | None:
    parts = [clean(p).rstrip(" :") for p in parts if p and clean(p).rstrip(" :")]
    return " — ".join(dict.fromkeys(parts)) or None


def parse_table_2022(table: Table, context: str) -> list[tuple[str | None, str]]:
    header = [key(" ".join(paragraphs_of(c))) if c is not None else "" for c in row_cells(table.rows[0])]
    obj_cols = [i for i, h in enumerate(header) if "objectif" in h]
    if not obj_cols:
        REPORT_DATA.add("Tableaux ignorés", f"{context} : pas de colonne « objectif » ({header})")
        return []
    oc = obj_cols[0]
    rub_col = next((i for i, h in enumerate(header) if "rubrique" in h), None)
    # Colonne sans titre juste après « objectif » (urologie) : suite de la colonne objectif.
    extra = oc + 1 if oc + 1 < len(header) and header[oc + 1] == "" else None
    out, rubrique = [], None
    for row in table.rows[1:]:
        cells = row_cells(row)
        if rub_col is not None and cells[rub_col] is not None:
            text = " ".join(paragraphs_of(cells[rub_col])).strip()
            if text:
                rubrique = text
        obj = paragraphs_of(cells[oc])
        if extra is not None and cells[extra] is not None and "".join(paragraphs_of(cells[extra])).strip():
            sub = " ".join(obj)
            for h, t in cell_items(paragraphs_of(cells[extra]), context):
                out.append((join_rubrique(rubrique, sub, h), t))
            continue
        for h, t in cell_items(obj, context):
            out.append((join_rubrique(rubrique, h), t))
    return out


def walk(doc_path: Path, start: str, stop: str, headings: dict, parse_table) -> dict:
    d = docx.Document(doc_path)
    results = defaultdict(list)  # (dossier, annee) -> [(rubrique, texte)]
    targets, keep_rubrique, active = None, None, False
    for el in d.element.body.iterchildren():
        tag = el.tag.rsplit("}", 1)[1]
        if tag == "p":
            p = Paragraph(el, d)
            style = p.style.name
            if style.startswith("Heading 1") and start in key(p.text):
                active = True
                continue
            if not active:
                continue
            if stop in key(p.text) and style.startswith("Heading"):
                break
            if style.startswith("Heading") or style == "Subhead":
                k = key(p.text)
                value = headings.get(k)
                if isinstance(value, tuple):
                    keep_rubrique = value[1]
                elif value is not None:
                    targets, keep_rubrique = value, None
                elif style.startswith(("Heading 3", "Heading 4")) or style == "Subhead":
                    if style == "Subhead" and targets is None:
                        continue
                    if k.startswith(("maladies de l'enfant", "evaluation")) or "stages hospitaliers de la" in k:
                        targets = None
                        continue
                    REPORT_DATA.add("Titres non reconnus", f"{doc_path.name} : « {p.text.strip()} » ({style})")
                    targets = None
        elif tag == "tbl" and active and targets:
            context = f"{doc_path.name} / {targets[0][0]} {targets[0][1]}"
            for target_list, rub, text in parse_table(Table(el, d), context, targets, keep_rubrique):
                for t in target_list:
                    results[t].append((rub, text))
            if headings is HEADINGS_OLD:
                targets = getattr(parse_table, "last_targets", targets)
    return results


def table_2022(table, context, targets, keep_rubrique):
    return [(targets, join_rubrique(keep_rubrique, r), t) for r, t in parse_table_2022(table, context)]


def table_old(table, context, targets, keep_rubrique):
    out, section, row_rub = [], keep_rubrique, None
    for row in table.rows[1:]:
        cells = row_cells(row)
        first = " ".join(paragraphs_of(cells[0])).strip()
        merged = all(c is None for c in cells[1:])
        if merged:
            k = key(first)
            if k in SWITCH_OLD:
                targets, section, row_rub = SWITCH_OLD[k], None, None
            elif "arc" in k and targets[0][0] in SHARED_ARC_OLD:
                targets, section, row_rub = SHARED_ARC_OLD[targets[0][0]], clean(first), None
            else:
                section, row_rub = clean(first).rstrip(" :"), None
            continue
        second = paragraphs_of(cells[1]) if len(cells) > 1 else []
        if "".join(second).strip():
            if first:
                row_rub = first
            for h, t in cell_items(second, context):
                out.append((targets, join_rubrique(row_rub or section, h), t))
        else:
            row_rub = None
            for h, t in cell_items(paragraphs_of(cells[0]), context):
                out.append((targets, join_rubrique(section, h), t))
    table_old.last_targets = targets
    return out


def pdf_pages_text(path: Path) -> dict[int, str]:
    doc = pymupdf.open(path)
    return {i + 1: key(page.get_text()) for i, page in enumerate(doc)}


def coverage(text: str, page_text: str) -> float:
    words = [w for w in re.findall(r"\w{4,}", key(text))]
    if not words:
        return 1.0
    squashed = page_text.replace(" ", "")
    return sum(w in squashed for w in words) / len(words)


def main() -> None:
    index = json.loads(INDEX.read_text(encoding="utf-8"))
    entries = {(e["dossier"], e["version"], e["annee"]): e for e in index}

    raw = {
        "2022": walk(DOCX_2022, "vii- les objectifs des stages hospitaliers", "le referentiel pour l'evaluation",
                     HEADINGS_2022, table_2022),
        "old": walk(DOCX_OLD, "vii — les objectifs des stages hospitaliers", "annexes", HEADINGS_OLD, table_old),
    }

    objectifs, counts = [], {}
    for version, results in raw.items():
        for (dossier, annee), items in results.items():
            if (dossier, version, annee) not in entries:
                REPORT_DATA.add("Cibles inconnues", f"{dossier} {version} {annee} absent de index.json")
    for e in index:
        k = (e["dossier"], e["annee"])
        items = raw[e["version"]].get(k, [])
        seen, n = set(), 0
        for rubrique, texte in items:
            if (rubrique, key(texte)) in seen:
                REPORT_DATA.add("Doublons retirés", f"`{e['dossier']}` {e['version']} {e['annee']} : « {texte} »")
                continue
            seen.add((rubrique, key(texte)))
            n += 1
            item = {"id": f"{e['dossier']}:{e['version']}:{e['annee']}:{n:02d}", "dossier": e["dossier"],
                    "version": e["version"], "annee": e["annee"]}
            if rubrique:
                item["rubrique"] = rubrique
            item["texte"] = texte
            objectifs.append(item)
            if VERB.search(texte) or texte[0].islower() and not rubrique:
                REPORT_DATA.add("Ordre des mots ou début suspect (vérifier dans le docx)",
                                f"`{item['id']}` : « {texte} »")
        counts[(e["dossier"], e["version"], e["annee"])] = n

    pages = pdf_pages_text(PDF_2022)
    cross = []
    for e in index:
        if e["version"] != "2022":
            continue
        texts = [o["texte"] for o in objectifs if (o["dossier"], o["version"], o["annee"]) ==
                 (e["dossier"], "2022", e["annee"])]
        page_text = " ".join(pages[p] for p in e["pages_source"])
        if not texts:
            continue
        if set(e["pages_source"]) <= PDF_2022_NO_TEXT:
            cross.append(f"| `{e['dossier']}` {e['annee']} | p{'-'.join(map(str, e['pages_source']))} | "
                         f"— | pas de couche texte : docx seule source |")
            continue
        cov = sum(coverage(t, page_text) for t in texts) / len(texts)
        flag = "à vérifier" if cov < 0.85 else "ok"
        cross.append(f"| `{e['dossier']}` {e['annee']} | p{'-'.join(map(str, e['pages_source']))} | "
                     f"{cov:.0%} | {flag} |")

    OUT.write_text(json.dumps(objectifs, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    write_report(index, counts, cross, len(objectifs))
    empty = [k for k, v in counts.items() if v == 0]
    print(f"{len(objectifs)} objectifs pour {len(index)} entrées ({len(empty)} vides) -> {OUT.relative_to(ROOT)}")


def write_report(index, counts, cross, total) -> None:
    lines = ["# Rapport — data/objectifs.json", "",
             "Généré par `scripts/build_app_data.py`. À relire après chaque modification des sources.", "",
             f"**{total} objectifs** pour {len(index)} entrées de `index.json`.", "",
             "## Nombre d'objectifs par entrée", "", "| Dossier | Version | Année | Objectifs |", "|---|---|---|---|"]
    for e in index:
        n = counts[(e["dossier"], e["version"], e["annee"])]
        lines.append(f"| `{e['dossier']}` | {e['version']} | {e['annee']} | {n if n else '**0**'} |")
    lines += ["", "## Entrées sans objectif", ""]
    empty = [e for e in index if counts[(e["dossier"], e["version"], e["annee"])] == 0]
    for e in empty:
        why = " (attendu : objectifs non élaborés dans l'ancien guide)" \
            if (e["dossier"], e["version"]) == ("medecine-sociale-2eme-annee", "old") else " **(inattendu)**"
        lines.append(f"- `{e['dossier']}` {e['version']} {e['annee']}{why}")
    if not empty:
        lines.append("_Aucune._")
    lines += ["", "## Contrôle croisé avec le texte du PDF 2022", "",
              "Part des mots (≥ 4 lettres) des objectifs retrouvés sur les pages source. "
              "Un score bas signale un écart entre le docx et le PDF (ou une couche texte en désordre).", "",
              "| Entrée | Pages | Couverture | |", "|---|---|---|---|", *cross]
    lines += ["", "## Points à vérifier", "",
              "Choix de classement faits par le script :", "",
              "- old `pneumologie` : les tâches d'apprentissage et séances d'ARC (p59, communes) sont dans 3A et 6A.",
              "- old `chirurgie-vasculaire-peripherique` : les séances d'ARC (p69, communes) sont dans 4A et 6A.",
              "- old `gynecologie-obstetrique` : les séances d'ARC qui suivent les objectifs 6A sont rangées en 6A.",
              "- old `oncologie-hematologie` = hématologie clinique + radiothérapie-oncologie ; old "
              "`imagerie-medicale` inclut la radiologie pédiatrique ; old `pediatrie` inclut la néonatologie.",
              "- Les lignes terminées par « : » deviennent la rubrique des lignes suivantes de la même cellule ; "
              "la fin d'une telle liste n'est pas marquée dans les sources, vérifier les rubriques longues.", ""]
    for section, items in REPORT_DATA.lines.items():
        lines += [f"### {section}", "", *[f"- {i}" for i in items], ""]
    REPORT.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
