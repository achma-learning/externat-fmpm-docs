"""Construit le dossier 'objective par specialite/' à partir de data/specialites.json.

Usage (depuis la racine du dépôt) :
    python scripts/build_objectives.py                 # extrait les PDF par spécialité
    python scripts/build_objectives.py --compress-old  # (re)génère d'abord la copie compressée du guide old

Dépendance : pip install pymupdf
"""
import argparse
import json
import shutil
from pathlib import Path

import pymupdf

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "data" / "specialites.json"
OUT = ROOT / "objective par specialite"
INDEX = ROOT / "data" / "index.json"
README = ROOT / "README.md"
TABLE_START, TABLE_END = "<!-- TABLE:START -->", "<!-- TABLE:END -->"

# Le guide old est un PDF vectoriel de 68 Mo (texte vectorisé) : on le rastérise
# en niveaux de gris, ce qui reste net à l'écran et passe sous la limite GitHub de 25 Mo.
COMPRESS_DPI = 200
COMPRESS_JPEG_QUALITY = 70


def compress_old(original: Path, target: Path) -> None:
    src = pymupdf.open(original)
    out = pymupdf.open()
    for page in src:
        pix = page.get_pixmap(dpi=COMPRESS_DPI, colorspace=pymupdf.csGRAY)
        new = out.new_page(width=page.rect.width, height=page.rect.height)
        new.insert_image(new.rect, stream=pix.tobytes("jpeg", jpg_quality=COMPRESS_JPEG_QUALITY))
    out.set_metadata({"title": "Guide de stage de l'étudiant en médecine - FMPM (ancienne version, compressée)"})
    out.save(target, garbage=4, deflate=True)
    print(f"Compressé : {target.relative_to(ROOT)} ({target.stat().st_size / 2**20:.1f} Mo)")


def file_name(entry: dict) -> str:
    return f"{entry['annee']} - objective de stage {entry['version']}.pdf"


def build(config: dict) -> list[dict]:
    sources = {v: pymupdf.open(ROOT / s["fichier"]) for v, s in config["sources"].items()}
    if OUT.exists():
        shutil.rmtree(OUT)
    index = []
    for spec in config["specialites"]:
        folder = OUT / spec["dossier"]
        folder.mkdir(parents=True)
        for entry in spec["objectifs"]:
            src = sources[entry["version"]]
            doc = pymupdf.open()
            for p in entry["pages"]:
                doc.insert_pdf(src, from_page=p - 1, to_page=p - 1)
            doc.set_metadata({
                "title": f"Objectifs de stage - {spec['nom']} - {entry['annee']} ({entry['version']})",
                "subject": config["sources"][entry["version"]]["titre"],
            })
            path = folder / file_name(entry)
            doc.save(path, garbage=4, deflate=True)
            index.append({
                "dossier": spec["dossier"],
                "nom": spec["nom"],
                "categorie": spec["categorie"],
                "version": entry["version"],
                "annee": entry["annee"],
                "annees": entry["annees"],
                "pages_source": entry["pages"],
                "fichier": path.relative_to(ROOT).as_posix(),
                "taille_ko": round(path.stat().st_size / 1024),
                **({"note": entry["note"]} if "note" in entry else {}),
            })
    return index


def readme_table(config: dict) -> str:
    def cell(spec, version):
        items = [e for e in spec["objectifs"] if e["version"] == version]
        return ", ".join(f"{e['annee']} (p{'-'.join(map(str, e['pages']))})" for e in items) or "—"

    rows = ["| Dossier | Spécialité | Catégorie | 2022 | old |", "|---|---|---|---|---|"]
    rows += [
        f"| `{s['dossier']}` | {s['nom']} | {s['categorie']} | {cell(s, '2022')} | {cell(s, 'old')} |"
        for s in config["specialites"]
    ]
    return "\n".join(rows)


def update_readme(config: dict) -> None:
    text = README.read_text(encoding="utf-8")
    start, end = text.index(TABLE_START) + len(TABLE_START), text.index(TABLE_END)
    README.write_text(text[:start] + "\n" + readme_table(config) + "\n" + text[end:], encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--compress-old", action="store_true", help="régénère la copie compressée du guide old")
    args = parser.parse_args()

    config = json.loads(DATA.read_text(encoding="utf-8"))
    old = config["sources"]["old"]
    compressed = ROOT / old["fichier"]
    if args.compress_old or not compressed.exists():
        compress_old(ROOT / old["original"], compressed)

    index = build(config)
    INDEX.write_text(json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    update_readme(config)

    total = sum(i["taille_ko"] for i in index)
    biggest = max(index, key=lambda i: i["taille_ko"])
    print(f"{len(index)} fichiers dans {len(config['specialites'])} dossiers ({total / 1024:.1f} Mo au total)")
    print(f"Plus gros fichier : {biggest['fichier']} ({biggest['taille_ko']} Ko)")


if __name__ == "__main__":
    main()
