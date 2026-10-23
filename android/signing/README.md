# Clé de signature (publique, volontairement)

`externat-fmpm-release.jks` signe tous les APK de release : PKCS12, RSA 4096, valable 100 ans,
alias `externat`, mot de passe `externat-fmpm` (écrit dans `android/app/build.gradle.kts`).
Elle est versionnée exprès : projet personnel, partage libre, aucun secret GitHub à configurer.

- **Ne jamais la remplacer ni la régénérer.** Android n'installe une mise à jour que si elle est
  signée avec la même clé. Nouvelle clé = désinstaller l'app = checklist perdue.
- Conséquence acceptée : n'importe qui peut signer un APK que le téléphone acceptera comme mise à jour
  de cette app. N'installez que les APK publiés sur ce dépôt.
