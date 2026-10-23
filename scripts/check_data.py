"""Vérifie la cohérence des données embarquées dans l'app (lancé par la CI avant le build Android).

Usage : python scripts/check_data.py
Sans dépendance externe. Code de sortie 1 en cas d'erreur.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAX_FILE_BYTES = 25 * 1024 * 1024  # limite d'envoi par le site GitHub


def main() -> int:
    errors = []
    index = json.loads((ROOT / "data" / "index.json").read_text(encoding="utf-8"))
    objectifs = json.loads((ROOT / "data" / "objectifs.json").read_text(encoding="utf-8"))

    keys = set()
    for e in index:
        path = ROOT / e["fichier"]
        if not path.is_file():
            errors.append(f"index.json : fichier introuvable {e['fichier']}")
        elif path.stat().st_size > MAX_FILE_BYTES:
            errors.append(f"index.json : {e['fichier']} dépasse 25 Mo")
        k = (e["dossier"], e["version"], e["annee"])
        if k in keys:
            errors.append(f"index.json : entrée en double {k}")
        keys.add(k)

    ids = set()
    for o in objectifs:
        if o["id"] in ids:
            errors.append(f"objectifs.json : id en double {o['id']}")
        ids.add(o["id"])
        if (o["dossier"], o["version"], o["annee"]) not in keys:
            errors.append(f"objectifs.json : {o['id']} ne correspond à aucune entrée de index.json")
        if not o.get("texte", "").strip():
            errors.append(f"objectifs.json : {o['id']} sans texte")

    for e in errors:
        print("ERREUR", e)
    print(f"{len(index)} entrées, {len(objectifs)} objectifs : {'OK' if not errors else f'{len(errors)} erreur(s)'}")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
