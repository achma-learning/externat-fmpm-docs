EXTERNAT FMPM — INSTALLATION SUR ANDROID
========================================

Application hors ligne : objectifs de stage (checklist) et guides de la FMPM.
Android 7.0 ou plus récent, téléphone ou tablette. Aucune permission demandée.

1. INSTALLER
------------
 1. Copiez le fichier « externat-fmpm-X.Y.Z.apk » sur le téléphone
    (téléchargement direct, câble USB, Google Drive, WhatsApp « Document »…).
 2. Ouvrez-le depuis l'application « Fichiers » (ou la notification de téléchargement).
 3. Android demande d'autoriser l'installation d'applications inconnues :
    touchez « Paramètres », activez « Autoriser cette source » pour l'application
    qui ouvre le fichier (Fichiers, Chrome, Drive…), puis revenez en arrière.
    Selon les téléphones : Paramètres > Applications > Accès spécial >
    Installer des applications inconnues.
 4. Touchez « Installer ». Si Play Protect affiche un avertissement
    (application non publiée sur le Play Store), touchez « Installer quand même ».
 5. Vous pouvez désactiver ensuite l'autorisation « sources inconnues ».

2. METTRE À JOUR
----------------
 - Installez simplement le nouvel APK par-dessus l'ancien (mêmes étapes).
   La checklist, les favoris et les réglages sont conservés.
 - Toutes les versions publiées sur la page Releases sont signées avec la même
   clé : elles s'installent toujours les unes par-dessus les autres.
 - Si Android refuse (« Application non installée », « signature différente ») :
   l'APK installé venait d'ailleurs (par ex. un build de test de l'onglet
   Actions d'avant la v1.0.0). Il faut alors le désinstaller une fois, ce qui
   EFFACE la checklist, puis installer la version de la page Releases.

3. REVENIR À UNE VERSION PRÉCÉDENTE
-----------------------------------
 Android n'installe pas une version plus ancienne par-dessus une plus récente :
 il faut désinstaller (la checklist est effacée), puis installer l'ancien APK.

4. VÉRIFIER LE FICHIER (facultatif)
-----------------------------------
 L'empreinte SHA-256 de l'APK est indiquée sur la page de la release GitHub.
