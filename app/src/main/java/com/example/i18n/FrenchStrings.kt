package com.example.i18n

/** French screen texts, keyed by the English text passed to [tr]. */
object FrenchStrings {
    val table: Map<String, String> = mapOf(
        // Tabs, top bar, app languages, enum labels
        "Home" to "Accueil",
        "Call Announcer" to "Call Announcer",
        "Settings" to "Réglages",
        "History" to "Historique",
        "Setup" to "Configuration",
        "Paused" to "En pause",
        "Setup needed" to "À configurer",
        "Active" to "Actif",
        "Phone language" to "Langue du téléphone",
        "English" to "English",
        "Kinyarwanda" to "Kinyarwanda",
        "Français" to "Français",
        "Speaker and headphones" to "Haut-parleur et écouteurs",
        "Only in headphones / car" to "Seulement dans les écouteurs / la voiture",
        "Don't announce" to "Ne pas annoncer",
        "Saved contact" to "Contact enregistré",
        "Rwandan name" to "Nom rwandais",
        "MTN contact" to "Contact MTN",

        // Notification, tile, widget
        "Announcing calls" to "Annonce les appels",
        "Paused · tap to turn on" to "En pause · touchez pour activer",
        "On" to "Activé",
        "Announcer Active" to "Annonceur actif",
        "Announcer Paused" to "Annonceur en pause",
        "Announcing: %s" to "Annonce : %s",
        "Monitoring incoming calls" to "Surveille les appels entrants",
        "Setup incomplete: %s. Tap to fix." to "Configuration incomplète : %s. Touchez pour corriger.",
        "Pause" to "Pause",
        "Resume" to "Reprendre",
        "Backup saved" to "Sauvegarde enregistrée",
        "Backup failed: %s" to "Échec de la sauvegarde : %s",
        "Settings restored" to "Réglages restaurés",
        "Restore failed: %s" to "Échec de la restauration : %s",

        // Simulated call
        "Stop" to "Arrêter",
        "Simulated call" to "Appel simulé",
        "Mobile · saved contact" to "Mobile · contact enregistré",
        "Unknown number" to "Numéro inconnu",
        "ANNOUNCING" to "ANNONCE EN COURS",
        "WILL SAY" to "DIRA",
        "Ringtone lowered while speaking" to "Sonnerie baissée pendant l'annonce",
        "Decline" to "Refuser",
        "Answer" to "Répondre",

        // Contacts & pronunciation
        "Contacts & pronunciation" to "Contacts et prononciation",
        "Back" to "Retour",
        "Add contact" to "Ajouter un contact",
        "%d contacts" to "%d contacts",
        "Sounds wrong? Write the name the way it sounds, for example \u201CChoo-sah\u201D for Cyusa." to
            "Mal prononcé ? Écrivez le nom comme il se dit, par exemple \u201CChoo-sah\u201D pour Cyusa.",
        "Say it as" to "Le dire comme",
        "Listen" to "Écouter",
        "Announce" to "Annoncer",
        "Save" to "Enregistrer",
        "Reset" to "Réinitialiser",
        "Cancel" to "Annuler",
        "Make every name sound right" to "Que chaque nom sonne juste",
        "Fix how a name is pronounced, always announce important people, or never announce someone. You can also do this from History." to
            "Corrigez la prononciation d'un nom, annoncez toujours les personnes importantes ou n'annoncez jamais quelqu'un. Possible aussi depuis l'Historique.",
        "Said as \u201C%s\u201D" to "Dit \u201C%s\u201D",
        "Always announced, even in quiet hours" to "Toujours annoncé, même pendant les heures calmes",
        "Never announced" to "Jamais annoncé",
        "Normal" to "Normal",
        "Always" to "Toujours",
        "Never" to "Jamais",
        "Follows your settings." to "Suit vos réglages.",
        "Announced even during quiet hours or when contacts are switched off." to
            "Annoncé même pendant les heures calmes ou si les contacts sont désactivés.",
        "Their calls ring normally but are never announced." to "Ses appels sonnent normalement mais ne sont jamais annoncés.",

        // Dashboard
        "Try it" to "Essayer",
        "Phone number" to "Numéro de téléphone",
        "Contact name (optional)" to "Nom du contact (facultatif)",
        "Play voice" to "Écouter la voix",
        "Simulate call" to "Simuler un appel",
        "How it works" to "Fonctionnement",
        "Saved contacts" to "Contacts enregistrés",
        "Says the name you saved, e.g. \"Incoming call from Alice\"." to
            "Dit le nom enregistré, par ex. \"Appel de Alice\".",
        "Unknown numbers" to "Numéros inconnus",
        "Reads the number two digits at a time: 07 85 46 12 34." to "Lit le numéro deux chiffres à la fois : 07 85 46 12 34.",
        "Restored as soon as you answer, decline, or the call ends." to "Rétablie dès que vous répondez, refusez ou que l'appel se termine.",
        "Customize voice and wording" to "Personnaliser la voix et les mots",
        "Announcer is paused" to "L'annonceur est en pause",
        "Turn it on to hear who is calling." to "Activez-le pour entendre qui appelle.",
        "Almost ready" to "Presque prêt",
        "1 setup step left before calls are announced while the app is closed." to
            "Encore 1 étape avant que les appels soient annoncés application fermée.",
        "%d setup steps left before calls are announced while the app is closed." to
            "Encore %d étapes avant que les appels soient annoncés application fermée.",
        "You're covered" to "Tout est prêt",
        "Every incoming call will be announced, even when the app is closed." to
            "Chaque appel entrant sera annoncé, même application fermée.",
        "Finish setup" to "Terminer la configuration",
        "YOU'LL HEAR" to "VOUS ENTENDREZ",
        "Saved contact · \"MTN\" is spelled out as M-T-N" to "Contact enregistré · \"MTN\" est épelé M-T-N",
        "Saved contact · name is spoken" to "Contact enregistré · le nom est prononcé",
        "Unknown number · read in pairs" to "Numéro inconnu · lu par paires",

        // History
        "Clear all" to "Tout effacer",
        "Clear history?" to "Effacer l'historique ?",
        "All %d announced calls will be removed from this list." to "Les %d appels annoncés seront retirés de cette liste.",
        "Clear" to "Effacer",
        "Marked as spam" to "Marqué comme spam",
        "Replay announcement" to "Rejouer l'annonce",
        "More" to "Plus",
        "Pronunciation & rules" to "Prononciation et règles",
        "Not spam" to "Pas un spam",
        "Mark as spam" to "Marquer comme spam",
        "Delete" to "Supprimer",
        "No calls yet" to "Aucun appel pour l'instant",
        "Announced calls appear here, so you can replay what was said." to
            "Les appels annoncés apparaissent ici pour réécouter ce qui a été dit.",
        "Today" to "Aujourd'hui",
        "Yesterday" to "Hier",

        // Privacy policy
        "Phone state and call log" to "État du téléphone et journal d'appels",
        "Used only to notice an incoming call, read the caller's number so it can be spoken, check whether your phone blocked that number before, and list missed calls if you turn on the summary." to
            "Utilisé uniquement pour détecter un appel entrant, lire le numéro de l'appelant pour l'annoncer, vérifier si votre téléphone a déjà bloqué ce numéro et lister les appels manqués si vous activez le résumé.",
        "Contacts" to "Contacts",
        "Used only to look up the caller's name (and phonetic name, if saved) on your phone. Your contact list is never copied or uploaded." to
            "Utilisé uniquement pour trouver le nom de l'appelant (et son nom phonétique, s'il est enregistré) sur votre téléphone. Vos contacts ne sont jamais copiés ni envoyés.",
        "Notification access (optional)" to "Accès aux notifications (facultatif)",
        "Used to spot incoming-call notifications from WhatsApp, Telegram, Signal and similar apps. Only if you turn on message announcements, new-message notifications of chat apps are read aloud. Nothing from notifications is stored." to
            "Utilisé pour repérer les notifications d'appel de WhatsApp, Telegram, Signal et applications similaires. Seulement si vous activez l'annonce des messages, les notifications de nouveaux messages sont lues à voix haute. Rien n'est conservé.",
        "Notifications" to "Notifications",
        "Shows that the announcer is running and lets you pause it." to "Indique que l'annonceur fonctionne et permet de le mettre en pause.",
        "Text-to-speech" to "Synthèse vocale",
        "Announcements are spoken by your phone's speech engine. Spoken audio is cached on the phone to start faster next time." to
            "Les annonces sont prononcées par le moteur vocal du téléphone. L'audio est gardé en cache sur le téléphone pour démarrer plus vite.",
        "Announcement history" to "Historique des annonces",
        "Date, number or name, the calling app (for app calls) and what was spoken, for each announced call. Kept only on this phone." to
            "Date, numéro ou nom, application d'appel (pour les appels d'applications) et texte prononcé, pour chaque appel annoncé. Conservé uniquement sur ce téléphone.",
        "Your settings" to "Vos réglages",
        "Voice, wording, volume and theme preferences, how you want some contacts announced, and numbers you marked as spam." to
            "Préférences de voix, de formulation, de volume et de thème, la façon d'annoncer certains contacts et les numéros marqués comme spam.",
        "Nothing leaves your phone" to "Rien ne quitte votre téléphone",
        "The app has no internet access. No analytics, no ads, no accounts, and no data is sold or shared." to
            "L'application n'a pas accès à Internet. Pas de statistiques, pas de publicité, pas de compte, et aucune donnée n'est vendue ou partagée.",
        "Backups" to "Sauvegardes",
        "A backup file is only created when you save one, in the place you choose. If you turned on Android's own backup, it may include this app's settings and history, like any other app." to
            "Un fichier de sauvegarde n'est créé que lorsque vous en enregistrez un, à l'endroit choisi. Si la sauvegarde d'Android est activée, elle peut inclure les réglages et l'historique de l'application, comme pour toute autre application.",
        "Delete anytime" to "Supprimez à tout moment",
        "Clear the history in the app, revoke permissions in Android settings, or uninstall to remove everything." to
            "Effacez l'historique dans l'application, retirez les autorisations dans les réglages Android, ou désinstallez pour tout supprimer.",
        "Privacy policy" to "Politique de confidentialité",
        "What the app uses" to "Ce que l'application utilise",
        "What is stored" to "Ce qui est conservé",
        "Your data, your control" to "Vos données, votre contrôle",
        "Policy version %d. If this policy changes, you'll be asked to review it again." to
            "Version %d de la politique. Si elle change, il vous sera demandé de la relire.",
        "Your privacy" to "Votre vie privée",
        "Call Announcer speaks the name or number of whoever is calling. Here's exactly what it needs to do that." to
            "Call Announcer prononce le nom ou le numéro de la personne qui appelle. Voici exactement ce dont il a besoin pour cela.",
        "Everything stays on this phone" to "Tout reste sur ce téléphone",
        "By tapping Accept, you agree to this privacy policy. The app can't announce calls without it." to
            "En touchant Accepter, vous acceptez cette politique de confidentialité. L'application ne peut pas annoncer les appels sans cela.",
        "Accept" to "Accepter",

        // Settings: wording and caller details
        "App language" to "Langue de l'application",
        "Language of the words" to "Langue des annonces",
        "\u201CIncoming call from\u201D, \u201Con WhatsApp\u201D, \u201Cunknown caller\u201D\u2026" to
            "\u201CAppel de\u201D, \u201Csur WhatsApp\u201D, \u201Cnuméro inconnu\u201D\u2026",
        "Say the network" to "Dire le réseau",
        "\u201CAn MTN number\u201D or \u201Can Airtel number\u201D before unsaved Rwandan numbers." to
            "\u201CUn numéro MTN\u201D ou \u201Cun numéro Airtel\u201D avant les numéros rwandais non enregistrés.",
        "Warn about spam" to "Avertir des spams",
        "Says \u201CPossible spam\u201D first for numbers you marked as spam in History or that your phone blocked before." to
            "Dit d'abord \u201CAttention, appel suspect\u201D pour les numéros marqués comme spam dans l'Historique ou déjà bloqués par votre téléphone.",
        "Fix a name that sounds wrong, always or never announce someone" to "Corriger un nom mal prononcé, toujours ou jamais annoncer quelqu'un",
        "1 contact with its own settings" to "1 contact avec ses propres réglages",
        "%d contacts with their own settings" to "%d contacts avec leurs propres réglages",

        // Quiet times, headphones, stopping
        "Quiet times" to "Moments calmes",
        "Quiet hours" to "Heures calmes",
        "Silent from %s to %s. Contacts set to \u201CAlways\u201D are still announced." to
            "Silencieux de %s à %s. Les contacts réglés sur \u201CToujours\u201D sont quand même annoncés.",
        "Stay silent at night; the phone still rings." to "Silencieux la nuit ; le téléphone sonne quand même.",
        "From" to "De",
        "Until" to "À",
        "With headphones or in the car" to "Avec écouteurs ou en voiture",
        "Same as without headphones" to "Comme sans écouteurs",
        "Private, at a comfortable volume" to "En privé, à un volume confortable",
        "Only the ringtone" to "Seulement la sonnerie",
        "Stop speaking" to "Arrêter l'annonce",
        "Volume or power button" to "Bouton de volume ou marche/arrêt",
        "Silencing the ringtone also stops the announcement." to "Couper la sonnerie arrête aussi l'annonce.",
        "Turn the phone face down" to "Retourner le téléphone",
        "Shake the phone" to "Secouer le téléphone",

        // Messages, missed calls, backup
        "Messages & missed calls" to "Messages et appels manqués",
        "Announce messages" to "Annoncer les messages",
        "\u201CMessage from Jean, on WhatsApp\u201D for WhatsApp, SMS, Telegram, Signal and more." to
            "\u201CMessage de Jean, sur WhatsApp\u201D pour WhatsApp, SMS, Telegram, Signal et plus.",
        "Read the message too" to "Lire aussi le message",
        "Anyone near the phone will hear it." to "Toute personne près du téléphone l'entendra.",
        "Notification access needed" to "Accès aux notifications requis",
        "Messages are found through their notifications." to "Les messages sont repérés grâce à leurs notifications.",
        "Allow" to "Autoriser",
        "Missed call summary" to "Résumé des appels manqués",
        "When you unlock the phone: \u201CYou missed calls from Jean and Paul\u201D." to
            "Au déverrouillage : \u201CAppels manqués de Jean et Paul\u201D.",
        "Backup" to "Sauvegarde",
        "Save settings to a file" to "Enregistrer les réglages dans un fichier",
        "Settings, contact pronunciations and spam numbers. Keep it for a new phone." to
            "Réglages, prononciations des contacts et numéros spam. Gardez-le pour un nouveau téléphone.",
        "Restore from a file" to "Restaurer depuis un fichier",

        // Settings: voice and numbers
        "Number pairs" to "Paires de nombres",
        "Digit by digit" to "Chiffre par chiffre",
        "Spaced digits" to "Chiffres espacés",
        "\u201Czero seven, eighty-five, forty-six\u201D" to "\u201Czéro sept, quatre-vingt-cinq, quarante-six\u201D",
        "\u201Czero seven, eight five, four six\u201D" to "\u201Czéro sept, huit cinq, quatre six\u201D",
        "\u201Czero seven, 8 5, 4 6\u201D with short pauses" to "\u201Czéro sept, 8 5, 4 6\u201D avec de courtes pauses",
        "Voice" to "Voix",
        "Speed" to "Vitesse",
        "Pitch" to "Hauteur",
        "Play sample" to "Écouter un exemple",
        "What's said" to "Ce qui est dit",
        "Before the caller's name" to "Avant le nom de l'appelant",
        "Reading unknown numbers" to "Lecture des numéros inconnus",
        "Say it" to "Le dire",
        "Once" to "1 fois",
        "Wait before speaking" to "Attendre avant de parler",
        "The ringtone plays normally first, then lowers while the name is spoken." to
            "La sonnerie joue d'abord normalement, puis baisse pendant que le nom est prononcé.",
        "Volume" to "Volume",
        "Louder than the ringtone" to "Plus fort que la sonnerie",
        "Speaks about 30% louder than your ringtone volume." to "Parle environ 30 % plus fort que le volume de la sonnerie.",
        "Lower ringtone while speaking" to "Baisser la sonnerie pendant l'annonce",
        "Restored when the announcement ends or the call is answered." to "Rétablie à la fin de l'annonce ou quand vous répondez.",
        "Who to announce" to "Qui annoncer",
        "Speaks the contact's name." to "Prononce le nom du contact.",
        "Reads the number in pairs." to "Lit le numéro par paires.",
        "Calls from apps" to "Appels des applications",
        "WhatsApp" to "WhatsApp",
        "Voice and video calls, including WhatsApp Business." to "Appels vocaux et vidéo, y compris WhatsApp Business.",
        "Other calling apps" to "Autres applications d'appel",
        "Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, Zoom and more." to
            "Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, Zoom et plus.",
        "App calls are found through their call notifications. Only those are read." to
            "Les appels d'applications sont repérés grâce à leurs notifications d'appel. Seules celles-ci sont lues.",
        "Appearance" to "Apparence",
        "Theme" to "Thème",
        "About" to "À propos",
        "System" to "Système",
        "Light" to "Clair",
        "Dark" to "Sombre",
        "Rwandan & French names" to "Noms rwandais et français",
        "Names are read by the main voice." to "Les noms sont lus par la voix principale.",
        "Rwandan names use the Swahili voice, French names the French voice." to
            "Les noms rwandais utilisent la voix swahilie, les noms français la voix française.",
        "Rwandan names use the Swahili voice." to "Les noms rwandais utilisent la voix swahilie.",
        "Rwandan names are respelled for the English voice. The Swahili voice sounds much closer." to
            "Les noms rwandais sont réécrits pour la voix anglaise. La voix swahilie est bien plus fidèle.",
        "Get Swahili voice" to "Obtenir la voix swahilie",
        "Phone default" to "Par défaut du téléphone",
        "Language" to "Langue",
        "Download voices in Android Settings → Text-to-speech" to "Téléchargez des voix dans Paramètres Android → Synthèse vocale",

        // Setup
        "Required" to "Obligatoire",
        "Recommended" to "Recommandé",
        "All set" to "Tout est prêt",
        "%d of %d done" to "%d sur %d terminées",
        "Calls will be announced, even when the app is closed." to "Les appels seront annoncés, même application fermée.",
        "Finish the steps below so calls are announced when the app is closed." to
            "Terminez les étapes ci-dessous pour que les appels soient annoncés application fermée.",
        "Approve each screen…" to "Validez chaque écran…",
        "Fix everything" to "Tout corriger",
        "Opens each setting in turn. Tap Allow, then come back; the next one opens automatically." to
            "Ouvre chaque réglage tour à tour. Touchez Autoriser, puis revenez ; le suivant s'ouvre automatiquement.",
        "It's on" to "C'est activé",
        "Phone and call log access" to "Accès au téléphone et au journal d'appels",
        "Detects incoming calls and reads the caller's number." to "Détecte les appels entrants et lit le numéro de l'appelant.",
        "Contacts access" to "Accès aux contacts",
        "Speaks saved names instead of numbers, and lets Android wake the app for calls from contacts." to
            "Prononce les noms enregistrés au lieu des numéros et permet à Android de réveiller l'application pour les appels des contacts.",
        "Caller ID app" to "Application d'identification d'appel",
        "Android then wakes Call Announcer for every call, even when the phone has frozen or closed it. Calls are never blocked. Only one app can hold this: it replaces Truecaller or similar apps for spam blocking." to
            "Android réveille alors Call Announcer à chaque appel, même si le téléphone l'a figé ou fermé. Aucun appel n'est bloqué. Une seule application peut avoir ce rôle : elle remplace Truecaller ou similaire pour le blocage des spams.",
        "Set as caller ID app" to "Définir comme application d'identification",
        "Unrestricted battery use" to "Batterie sans restriction",
        "Stops Android from putting the announcer to sleep to save battery." to "Empêche Android d'endormir l'annonceur pour économiser la batterie.",
        "Background activity allowed" to "Activité en arrière-plan autorisée",
        "Background use is restricted for this app. Open App info → Battery and choose \"Unrestricted\"." to
            "L'utilisation en arrière-plan est restreinte. Ouvrez Infos sur l'appli → Batterie et choisissez \"Sans restriction\".",
        "Open app battery settings" to "Ouvrir les réglages de batterie",
        "%s auto-start" to "Démarrage auto %s",
        "%s phones stop apps that are not on their auto-start list when they are closed. Turn Call Announcer on in that list, then confirm here." to
            "Les téléphones %s arrêtent les applications absentes de leur liste de démarrage automatique quand elles sont fermées. Activez Call Announcer dans cette liste, puis confirmez ici.",
        "Open settings" to "Ouvrir les réglages",
        "Offline voice installed" to "Voix hors ligne installée",
        "No working text-to-speech engine. Install \"Speech Services by Google\"." to
            "Aucun moteur de synthèse vocale fonctionnel. Installez \"Services de reconnaissance vocale de Google\".",
        "The speech engine has no downloaded voice. Download one so calls are announced without internet." to
            "Le moteur vocal n'a aucune voix téléchargée. Téléchargez-en une pour annoncer les appels sans Internet.",
        "A voice is installed and works without internet." to "Une voix est installée et fonctionne sans Internet.",
        "Install speech engine" to "Installer le moteur vocal",
        "Download a voice" to "Télécharger une voix",
        "Announcer switched on" to "Annonceur activé",
        "Announcements are paused." to "Les annonces sont en pause.",
        "The background announcer is running." to "L'annonceur fonctionne en arrière-plan.",
        "Turn on" to "Activer",
        "Shows the small \"Monitoring incoming calls\" notification that keeps the announcer visible to Android." to
            "Affiche la petite notification \"Surveille les appels entrants\" qui garde l'annonceur visible pour Android.",
        "WhatsApp and app calls" to "WhatsApp et appels d'applications",
        "Calls from WhatsApp, Telegram, Signal and similar apps are announced too." to
            "Les appels de WhatsApp, Telegram, Signal et applications similaires sont aussi annoncés.",
        "Allow notification access so calls from WhatsApp, Telegram, Signal and similar apps are announced. Only call notifications (and messages, if you turn that on) are read." to
            "Autorisez l'accès aux notifications pour annoncer les appels de WhatsApp, Telegram, Signal et similaires. Seules les notifications d'appel (et les messages, si vous l'activez) sont lues.",
        "If Android says \"Restricted setting\", open App info → ⋮ → Allow restricted settings, then try again." to
            "Si Android affiche \"Paramètre restreint\", ouvrez Infos sur l'appli → ⋮ → Autoriser les paramètres restreints, puis réessayez.",
        "Keep app active when unused" to "Garder l'application active si inutilisée",
        "Turn off \"Pause app activity if unused\" so Android never removes the permissions after months without opening the app." to
            "Désactivez \"Suspendre l'activité si inutilisée\" pour qu'Android ne retire jamais les autorisations après des mois sans ouvrir l'application.",
        "Battery saver is on" to "Économiseur de batterie activé",
        "Some phones stop background apps in battery saver. If calls are missed, turn it off or add Call Announcer to its exceptions." to
            "Certains téléphones arrêtent les applications en arrière-plan en mode économie. Si des appels ne sont pas annoncés, désactivez-le ou ajoutez Call Announcer aux exceptions.",
        "Open battery saver" to "Ouvrir l'économiseur de batterie",
        "Ringer on" to "Sonnerie activée",
        "The phone is on silent or vibrate, so calls are not announced until the ringer is turned back on." to
            "Le téléphone est en silencieux ou vibreur : les appels ne sont pas annoncés tant que la sonnerie n'est pas réactivée.",
        "Calls are announced while the ringer is on." to "Les appels sont annoncés quand la sonnerie est activée.",
        "Real call test" to "Test avec un vrai appel",
        "A real call was announced while the app was closed on this phone." to
            "Un vrai appel a été annoncé alors que l'application était fermée sur ce téléphone.",
        "Close the app (swipe it away from recent apps), then ask someone to call you. This proves the phone lets Call Announcer work in the background." to
            "Fermez l'application (balayez-la des applications récentes), puis demandez à quelqu'un de vous appeler. Cela prouve que le téléphone laisse Call Announcer fonctionner en arrière-plan."
    )
}
