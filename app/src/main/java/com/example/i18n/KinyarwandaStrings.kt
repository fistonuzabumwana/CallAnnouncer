package com.example.i18n

/** Kinyarwanda screen texts, keyed by the English text passed to [tr]. */
object KinyarwandaStrings {
    val table: Map<String, String> = mapOf(
        // Tabs, top bar, app languages, enum labels
        "Home" to "Ahabanza",
        "Call Announcer" to "Call Announcer",
        "Settings" to "Igenamiterere",
        "History" to "Amateka",
        "Setup" to "Gutegura",
        "Paused" to "Byahagaritswe",
        "Setup needed" to "Bikeneye gutegurwa",
        "Active" to "Birakora",
        "Phone language" to "Ururimi rwa telefone",
        "English" to "English",
        "Kinyarwanda" to "Kinyarwanda",
        "Français" to "Français",
        "Speaker and headphones" to "Indangururamajwi n'écouteurs",
        "Only in headphones / car" to "Muri écouteurs / mu modoka gusa",
        "Don't announce" to "Ntuvuge",
        "Saved contact" to "Nimero yabitswe",
        "Rwandan name" to "Izina ry'Ikinyarwanda",
        "MTN contact" to "Nimero ya MTN yabitswe",

        // Notification, tile, widget
        "Announcing calls" to "Ivuga abaguhamagara",
        "Paused · tap to turn on" to "Byahagaritswe · kanda ubifungure",
        "On" to "Birafunguye",
        "Announcer Active" to "Call Announcer irakora",
        "Announcer Paused" to "Call Announcer yahagaritswe",
        "Announcing: %s" to "Ivuga: %s",
        "Monitoring incoming calls" to "Ikurikirana abaguhamagara",
        "Setup incomplete: %s. Tap to fix." to "Igenamiterere ntiryuzuye: %s. Kanda ubikosore.",
        "Pause" to "Hagarika",
        "Resume" to "Komeza",
        "Backup saved" to "Kopi yabitswe",
        "Backup failed: %s" to "Kubika kopi byanze: %s",
        "Settings restored" to "Igenamiterere ryagaruwe",
        "Restore failed: %s" to "Kugarura byanze: %s",

        // Simulated call
        "Stop" to "Hagarika",
        "Simulated call" to "Umuhamagaro w'igerageza",
        "Mobile · saved contact" to "Telefone · nimero yabitswe",
        "Unknown number" to "Nimero itazwi",
        "ANNOUNCING" to "IRAVUGA",
        "WILL SAY" to "IZAVUGA",
        "Ringtone lowered while speaking" to "Inzogera igabanywa mu gihe ivuga",
        "Decline" to "Anga",
        "Answer" to "Itaba",

        // Contacts & pronunciation
        "Contacts & pronunciation" to "Abantu n'imivugire",
        "Back" to "Subira inyuma",
        "Add contact" to "Ongeraho umuntu",
        "%d contacts" to "Abantu %d",
        "Sounds wrong? Write the name the way it sounds, for example \u201CChoo-sah\u201D for Cyusa." to
            "Ntiryumvikana neza? Andika izina uko rivugwa, urugero \u201CChoo-sah\u201D kuri Cyusa.",
        "Say it as" to "Rivuge nka",
        "Listen" to "Umva",
        "Announce" to "Vuga",
        "Save" to "Bika",
        "Reset" to "Siba ibyahinduwe",
        "Cancel" to "Reka",
        "Make every name sound right" to "Buri zina rivugwe neza",
        "Fix how a name is pronounced, always announce important people, or never announce someone. You can also do this from History." to
            "Kosora uko izina rivugwa, vuga buri gihe abantu b'ingenzi, cyangwa ntuzigere uvuga umuntu runaka. Ushobora no kubikora uhereye mu Amateka.",
        "Said as \u201C%s\u201D" to "Rivugwa nka \u201C%s\u201D",
        "Always announced, even in quiet hours" to "Avugwa buri gihe, no mu masaha y'ituze",
        "Never announced" to "Ntavugwa na rimwe",
        "Normal" to "Bisanzwe",
        "Always" to "Buri gihe",
        "Never" to "Nta na rimwe",
        "Follows your settings." to "Bikurikiza igenamiterere ryawe.",
        "Announced even during quiet hours or when contacts are switched off." to
            "Avugwa no mu masaha y'ituze cyangwa igihe kuvuga abantu wabitse bifunze.",
        "Their calls ring normally but are never announced." to "Iyo ahamagaye irasona bisanzwe ariko ntavugwa.",

        // Dashboard
        "Try it" to "Gerageza",
        "Phone number" to "Nimero ya telefone",
        "Contact name (optional)" to "Izina (si ngombwa)",
        "Play voice" to "Umva ijwi",
        "Simulate call" to "Gerageza umuhamagaro",
        "How it works" to "Uko ikora",
        "Saved contacts" to "Abantu wabitse",
        "Says the name you saved, e.g. \"Incoming call from Alice\"." to
            "Ivuga izina wabitse, urugero \"Uguhamagara ni Alice\".",
        "Unknown numbers" to "Nimero zitazwi",
        "Reads the number two digits at a time: 07 85 46 12 34." to "Isoma nimero imibare ibiri ibiri: 07 85 46 12 34.",
        "Restored as soon as you answer, decline, or the call ends." to "Isubizwa uko yari iri iyo witabye, wanze, cyangwa umuhamagaro urangiye.",
        "Customize voice and wording" to "Hindura ijwi n'amagambo",
        "Announcer is paused" to "Call Announcer yahagaritswe",
        "Turn it on to hear who is calling." to "Yifungure wumve ukuhamagara.",
        "Almost ready" to "Hafi kurangira",
        "1 setup step left before calls are announced while the app is closed." to
            "Hasigaye intambwe 1 kugira ngo abaguhamagara bavugwe porogaramu ifunze.",
        "%d setup steps left before calls are announced while the app is closed." to
            "Hasigaye intambwe %d kugira ngo abaguhamagara bavugwe porogaramu ifunze.",
        "You're covered" to "Byose biteguye",
        "Every incoming call will be announced, even when the app is closed." to
            "Buri wese uguhamagara azavugwa, n'iyo porogaramu ifunze.",
        "Finish setup" to "Rangiza gutegura",
        "YOU'LL HEAR" to "UZUMVA",
        "Saved contact · \"MTN\" is spelled out as M-T-N" to "Nimero yabitswe · \"MTN\" ivugwa inyuguti ku yindi M-T-N",
        "Saved contact · name is spoken" to "Nimero yabitswe · izina riravugwa",
        "Unknown number · read in pairs" to "Nimero itazwi · isomwa imibare ibiri ibiri",

        // History
        "Clear all" to "Siba byose",
        "Clear history?" to "Gusiba amateka?",
        "All %d announced calls will be removed from this list." to "Imihamagaro %d yose yavuzwe izavanwa kuri uru rutonde.",
        "Clear" to "Siba",
        "Marked as spam" to "Yashyizwe mu butekamutwe",
        "Replay announcement" to "Ongera wumve",
        "More" to "Ibindi",
        "Pronunciation & rules" to "Imivugire n'amategeko",
        "Not spam" to "Si ubutekamutwe",
        "Mark as spam" to "Shyira mu butekamutwe",
        "Delete" to "Siba",
        "No calls yet" to "Nta muhamagaro urabaho",
        "Announced calls appear here, so you can replay what was said." to
            "Imihamagaro yavuzwe igaragara hano, ushobora kongera kumva ibyavuzwe.",
        "Today" to "Uyu munsi",
        "Yesterday" to "Ejo hashize",

        // Privacy policy
        "Phone state and call log" to "Imiterere ya telefone n'urutonde rw'imihamagaro",
        "Used only to notice an incoming call, read the caller's number so it can be spoken, check whether your phone blocked that number before, and list missed calls if you turn on the summary." to
            "Bikoreshwa gusa mu kumenya ko uhamagawe, gusoma nimero y'ukuhamagara ngo ivugwe, kureba niba telefone yawe yarigeze guhagarika iyo nimero, no kubara imihamagaro utitabye niba warafunguye incamake.",
        "Contacts" to "Abantu wabitse",
        "Used only to look up the caller's name (and phonetic name, if saved) on your phone. Your contact list is never copied or uploaded." to
            "Bikoreshwa gusa mu gushaka izina ry'ukuhamagara (n'uko rivugwa, niba ryabitswe) muri telefone yawe. Urutonde rw'abantu bawe ntirwigera rwandukurwa cyangwa ngo rwoherezwe.",
        "Notification access (optional)" to "Kugera ku menyesha (si ngombwa)",
        "Used to spot incoming-call notifications from WhatsApp, Telegram, Signal and similar apps. Only if you turn on message announcements, new-message notifications of chat apps are read aloud. Nothing from notifications is stored." to
            "Bikoreshwa mu kubona amenyesha y'imihamagaro ya WhatsApp, Telegram, Signal n'izindi nkazo. Amenyesha y'ubutumwa bushya asomwa mu ijwi gusa iyo wafunguye kuvuga ubutumwa. Nta kintu cyo mu menyesha kibikwa.",
        "Notifications" to "Amenyesha",
        "Shows that the announcer is running and lets you pause it." to "Igaragaza ko Call Announcer ikora kandi ikwemerera kuyihagarika.",
        "Text-to-speech" to "Guhindura inyandiko ijwi",
        "Announcements are spoken by your phone's speech engine. Spoken audio is cached on the phone to start faster next time." to
            "Ibivugwa bivugwa na moteri y'ijwi ya telefone yawe. Amajwi abikwa muri telefone kugira ngo ubutaha atangire vuba.",
        "Announcement history" to "Amateka y'ibyavuzwe",
        "Date, number or name, the calling app (for app calls) and what was spoken, for each announced call. Kept only on this phone." to
            "Itariki, nimero cyangwa izina, porogaramu yahamagaye (ku mihamagaro ya porogaramu) n'ibyavuzwe, kuri buri muhamagaro wavuzwe. Bibikwa kuri iyi telefone gusa.",
        "Your settings" to "Igenamiterere ryawe",
        "Voice, wording, volume and theme preferences, how you want some contacts announced, and numbers you marked as spam." to
            "Ijwi, amagambo, ingano y'ijwi n'insanganyamatsiko wahisemo, uko ushaka ko abantu bamwe bavugwa, na nimero washyize mu butekamutwe.",
        "Nothing leaves your phone" to "Nta kintu kiva muri telefone yawe",
        "The app has no internet access. No analytics, no ads, no accounts, and no data is sold or shared." to
            "Porogaramu ntikoresha interineti. Nta bipimo, nta matangazo, nta konti, kandi nta makuru agurishwa cyangwa asangizwa abandi.",
        "Backups" to "Kopi z'igenamiterere",
        "A backup file is only created when you save one, in the place you choose. If you turned on Android's own backup, it may include this app's settings and history, like any other app." to
            "Dosiye ya kopi ikorwa gusa iyo uyibitse, aho wihitiyemo. Niba warafunguye kopi ya Android ubwayo, ishobora gushyiramo igenamiterere n'amateka by'iyi porogaramu, nk'izindi porogaramu zose.",
        "Delete anytime" to "Siba igihe cyose",
        "Clear the history in the app, revoke permissions in Android settings, or uninstall to remove everything." to
            "Siba amateka muri porogaramu, kuraho uburenganzira mu igenamiterere rya Android, cyangwa uyikuremo kugira ngo byose bivaho.",
        "Privacy policy" to "Politiki y'ibanga",
        "What the app uses" to "Ibyo porogaramu ikoresha",
        "What is stored" to "Ibibikwa",
        "Your data, your control" to "Amakuru yawe, ugenga wowe",
        "Policy version %d. If this policy changes, you'll be asked to review it again." to
            "Politiki verisiyo ya %d. Niyihinduka, uzasabwa kongera kuyisoma.",
        "Your privacy" to "Ibanga ryawe",
        "Call Announcer speaks the name or number of whoever is calling. Here's exactly what it needs to do that." to
            "Call Announcer ivuga izina cyangwa nimero y'ukuhamagara. Dore neza ibyo ikeneye kugira ngo ibikore.",
        "Everything stays on this phone" to "Byose biguma kuri iyi telefone",
        "By tapping Accept, you agree to this privacy policy. The app can't announce calls without it." to
            "Ukanze Emera, uba wemeye iyi politiki y'ibanga. Porogaramu ntishobora kuvuga abaguhamagara utayemeye.",
        "Accept" to "Emera",

        // Settings: wording and caller details
        "App language" to "Ururimi rwa porogaramu",
        "Language of the words" to "Ururimi rw'ibivugwa",
        "\u201CIncoming call from\u201D, \u201Con WhatsApp\u201D, \u201Cunknown caller\u201D\u2026" to
            "\u201CUguhamagara ni\u201D, \u201Ckuri WhatsApp\u201D, \u201Cnimero itazwi\u201D\u2026",
        "Say the network" to "Vuga umuyoboro",
        "\u201CAn MTN number\u201D or \u201Can Airtel number\u201D before unsaved Rwandan numbers." to
            "\u201CNimero ya MTN\u201D cyangwa \u201Cnimero ya Airtel\u201D mbere ya nimero z'u Rwanda utabitse.",
        "Warn about spam" to "Buriza ku butekamutwe",
        "Says \u201CPossible spam\u201D first for numbers you marked as spam in History or that your phone blocked before." to
            "Ibanza kuvuga \u201CWitondere\u201D kuri nimero washyize mu butekamutwe mu Amateka cyangwa telefone yawe yigeze guhagarika.",
        "Fix a name that sounds wrong, always or never announce someone" to "Kosora izina rivugwa nabi, vuga umuntu buri gihe cyangwa ntuzigere umuvuga",
        "1 contact with its own settings" to "Umuntu 1 ufite igenamiterere rye",
        "%d contacts with their own settings" to "Abantu %d bafite igenamiterere ryabo",

        // Quiet times, headphones, stopping
        "Quiet times" to "Ibihe by'ituze",
        "Quiet hours" to "Amasaha y'ituze",
        "Silent from %s to %s. Contacts set to \u201CAlways\u201D are still announced." to
            "Ituze kuva %s kugeza %s. Abantu bashyizwe kuri \u201CBuri gihe\u201D baravugwa.",
        "Stay silent at night; the phone still rings." to "Ituze nijoro; telefone ikomeza gusona.",
        "From" to "Kuva",
        "Until" to "Kugeza",
        "With headphones or in the car" to "Ukoresheje écouteurs cyangwa mu modoka",
        "Same as without headphones" to "Kimwe no hatari écouteurs",
        "Private, at a comfortable volume" to "Wenyine, mu ijwi riringaniye",
        "Only the ringtone" to "Inzogera gusa",
        "Stop speaking" to "Guhagarika kuvuga",
        "Volume or power button" to "Buto y'ijwi cyangwa yo kuzimya",
        "Silencing the ringtone also stops the announcement." to "Gucecekesha inzogera binahagarika ibivugwa.",
        "Turn the phone face down" to "Ubika telefone",
        "Shake the phone" to "Nyeganyeza telefone",

        // Messages, missed calls, backup
        "Messages & missed calls" to "Ubutumwa n'imihamagaro utitabye",
        "Announce messages" to "Vuga ubutumwa",
        "\u201CMessage from Jean, on WhatsApp\u201D for WhatsApp, SMS, Telegram, Signal and more." to
            "\u201CUbutumwa bwa Jean, kuri WhatsApp\u201D kuri WhatsApp, SMS, Telegram, Signal n'ibindi.",
        "Read the message too" to "Soma n'ubutumwa ubwabwo",
        "Anyone near the phone will hear it." to "Uri hafi ya telefone wese azabwumva.",
        "Notification access needed" to "Hakenewe kugera ku menyesha",
        "Messages are found through their notifications." to "Ubutumwa bubonwa binyuze mu menyesha yabwo.",
        "Allow" to "Emera",
        "Missed call summary" to "Incamake y'imihamagaro utitabye",
        "When you unlock the phone: \u201CYou missed calls from Jean and Paul\u201D." to
            "Iyo ufunguye telefone: \u201CNtiwitabye umuhamagaro wa Jean na Paul\u201D.",
        "Backup" to "Kopi",
        "Save settings to a file" to "Bika igenamiterere muri dosiye",
        "Settings, contact pronunciations and spam numbers. Keep it for a new phone." to
            "Igenamiterere, imivugire y'amazina na nimero z'ubutekamutwe. Yibike uzayikoreshe kuri telefone nshya.",
        "Restore from a file" to "Garura uhereye kuri dosiye",

        // Settings: voice and numbers
        "Number pairs" to "Imibare ibiri ibiri",
        "Digit by digit" to "Umubare ku wundi",
        "Spaced digits" to "Imibare itandukanyijwe",
        "\u201Czero seven, eighty-five, forty-six\u201D" to "\u201Czero seven, eighty-five, forty-six\u201D (mu Cyongereza)",
        "\u201Czero seven, eight five, four six\u201D" to "\u201Czero seven, eight five, four six\u201D (mu Cyongereza)",
        "\u201Czero seven, 8 5, 4 6\u201D with short pauses" to "\u201Czero seven, 8 5, 4 6\u201D n'uturuhuko duto",
        "Voice" to "Ijwi",
        "Speed" to "Umuvuduko",
        "Pitch" to "Ubutumburuke bw'ijwi",
        "Play sample" to "Umva urugero",
        "What's said" to "Ibivugwa",
        "Before the caller's name" to "Mbere y'izina ry'uguhamagara",
        "Reading unknown numbers" to "Gusoma nimero zitazwi",
        "Say it" to "Bivuge",
        "Once" to "Rimwe",
        "Wait before speaking" to "Tegereza mbere yo kuvuga",
        "The ringtone plays normally first, then lowers while the name is spoken." to
            "Inzogera ibanza gusona bisanzwe, hanyuma ikagabanuka mu gihe izina rivugwa.",
        "Volume" to "Ingano y'ijwi",
        "Louder than the ringtone" to "Hejuru y'inzogera",
        "Speaks about 30% louder than your ringtone volume." to "Ivuga hejuru ho nka 30% ugereranyije n'ijwi ry'inzogera yawe.",
        "Lower ringtone while speaking" to "Gabanya inzogera mu gihe ivuga",
        "Restored when the announcement ends or the call is answered." to "Isubizwa uko yari iri iyo kuvuga birangiye cyangwa witabye.",
        "Who to announce" to "Abo kuvuga",
        "Speaks the contact's name." to "Ivuga izina wabitse.",
        "Reads the number in pairs." to "Isoma nimero imibare ibiri ibiri.",
        "Calls from apps" to "Imihamagaro ya porogaramu",
        "WhatsApp" to "WhatsApp",
        "Voice and video calls, including WhatsApp Business." to "Imihamagaro y'ijwi n'amashusho, harimo na WhatsApp Business.",
        "Other calling apps" to "Izindi porogaramu zihamagara",
        "Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, Zoom and more." to
            "Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, Zoom n'izindi.",
        "App calls are found through their call notifications. Only those are read." to
            "Imihamagaro ya porogaramu ibonwa binyuze mu menyesha yayo. Ayo yonyine ni yo asomwa.",
        "Appearance" to "Imigaragarire",
        "Theme" to "Insanganyamatsiko",
        "About" to "Ibyerekeye",
        "System" to "Nk'iya telefone",
        "Light" to "Urumuri",
        "Dark" to "Umwijima",
        "Rwandan & French names" to "Amazina y'Ikinyarwanda n'Igifaransa",
        "Names are read by the main voice." to "Amazina asomwa n'ijwi nyamukuru.",
        "Rwandan names use the Swahili voice, French names the French voice." to
            "Amazina y'Ikinyarwanda akoresha ijwi ry'Igiswahili, ay'Igifaransa ijwi ry'Igifaransa.",
        "Rwandan names use the Swahili voice." to "Amazina y'Ikinyarwanda akoresha ijwi ry'Igiswahili.",
        "Rwandan names are respelled for the English voice. The Swahili voice sounds much closer." to
            "Amazina y'Ikinyarwanda yandikwa ukundi ku ijwi ry'Icyongereza. Ijwi ry'Igiswahili ryumvikana neza kurushaho.",
        "Get Swahili voice" to "Shaka ijwi ry'Igiswahili",
        "Phone default" to "Nk'uko telefone iteguye",
        "Language" to "Ururimi",
        "Download voices in Android Settings → Text-to-speech" to "Kuramo amajwi muri Igenamiterere rya Android → Text-to-speech",

        // Setup
        "Required" to "Ibya ngombwa",
        "Recommended" to "Ibyiza gukora",
        "All set" to "Byose biteguye",
        "%d of %d done" to "%d kuri %d byarangiye",
        "Calls will be announced, even when the app is closed." to "Abaguhamagara bazavugwa, n'iyo porogaramu ifunze.",
        "Finish the steps below so calls are announced when the app is closed." to
            "Rangiza intambwe zikurikira kugira ngo abaguhamagara bavugwe porogaramu ifunze.",
        "Approve each screen…" to "Emeza buri paji…",
        "Fix everything" to "Kosora byose",
        "Opens each setting in turn. Tap Allow, then come back; the next one opens automatically." to
            "Ifungura buri genamiterere ku rindi. Kanda Emera, hanyuma ugaruke; irikurikira rizifungura ubwaryo.",
        "It's on" to "Birafunguye",
        "Phone and call log access" to "Kugera kuri telefone n'imihamagaro",
        "Detects incoming calls and reads the caller's number." to "Imenya abaguhamagara kandi igasoma nimero yabo.",
        "Contacts access" to "Kugera ku bantu wabitse",
        "Speaks saved names instead of numbers, and lets Android wake the app for calls from contacts." to
            "Ivuga amazina wabitse aho kuvuga nimero, kandi ituma Android ikangura porogaramu iyo uhamagawe n'uwo wabitse.",
        "Caller ID app" to "Porogaramu imenya uhamagara",
        "Android then wakes Call Announcer for every call, even when the phone has frozen or closed it. Calls are never blocked. Only one app can hold this: it replaces Truecaller or similar apps for spam blocking." to
            "Android ikangura Call Announcer kuri buri muhamagaro, n'iyo telefone yayihagaritse cyangwa yayifunze. Nta muhamagaro uhagarikwa. Porogaramu imwe gusa ni yo ishobora kubikora: isimbura Truecaller cyangwa izindi nkayo zihagarika ubutekamutwe.",
        "Set as caller ID app" to "Yigire porogaramu imenya uhamagara",
        "Unrestricted battery use" to "Bateri nta mbogamizi",
        "Stops Android from putting the announcer to sleep to save battery." to "Ibuza Android gusinziriza Call Announcer igamije kuzigama bateri.",
        "Background activity allowed" to "Gukorera inyuma biremewe",
        "Background use is restricted for this app. Open App info → Battery and choose \"Unrestricted\"." to
            "Gukorera inyuma birabujijwe kuri iyi porogaramu. Fungura App info → Battery uhitemo \"Unrestricted\".",
        "Open app battery settings" to "Fungura igenamiterere rya bateri",
        "%s auto-start" to "Kwifungura (auto-start) kwa %s",
        "%s phones stop apps that are not on their auto-start list when they are closed. Turn Call Announcer on in that list, then confirm here." to
            "Telefone za %s zihagarika porogaramu zitari ku rutonde rwa auto-start iyo zifunzwe. Fungura Call Announcer kuri urwo rutonde, hanyuma wemeze hano.",
        "Open settings" to "Fungura igenamiterere",
        "Offline voice installed" to "Ijwi rikora nta interineti ririmo",
        "No working text-to-speech engine. Install \"Speech Services by Google\"." to
            "Nta moteri y'ijwi ikora ihari. Shyiramo \"Speech Services by Google\".",
        "The speech engine has no downloaded voice. Download one so calls are announced without internet." to
            "Moteri y'ijwi nta jwi ryakuweho rifite. Kuramo rimwe kugira ngo abaguhamagara bavugwe nta interineti.",
        "A voice is installed and works without internet." to "Ijwi ririmo kandi rikora nta interineti.",
        "Install speech engine" to "Shyiramo moteri y'ijwi",
        "Download a voice" to "Kuramo ijwi",
        "Announcer switched on" to "Call Announcer irafunguye",
        "Announcements are paused." to "Kuvuga byahagaritswe.",
        "The background announcer is running." to "Call Announcer irakorera inyuma.",
        "Turn on" to "Fungura",
        "Shows the small \"Monitoring incoming calls\" notification that keeps the announcer visible to Android." to
            "Igaragaza imenyesha rito \"Ikurikirana abaguhamagara\" rituma Android ikomeza kubona Call Announcer.",
        "WhatsApp and app calls" to "WhatsApp n'imihamagaro ya porogaramu",
        "Calls from WhatsApp, Telegram, Signal and similar apps are announced too." to
            "Imihamagaro ya WhatsApp, Telegram, Signal n'izindi nkazo na yo iravugwa.",
        "Allow notification access so calls from WhatsApp, Telegram, Signal and similar apps are announced. Only call notifications (and messages, if you turn that on) are read." to
            "Emera kugera ku menyesha kugira ngo imihamagaro ya WhatsApp, Telegram, Signal n'izindi nkazo ivugwe. Amenyesha y'imihamagaro gusa (n'ubutumwa, niba wabifunguye) ni yo asomwa.",
        "If Android says \"Restricted setting\", open App info → ⋮ → Allow restricted settings, then try again." to
            "Niba Android yanditse \"Restricted setting\", fungura App info → ⋮ → Allow restricted settings, hanyuma wongere ugerageze.",
        "Keep app active when unused" to "Komeza porogaramu ikore n'iyo idakoreshwa",
        "Turn off \"Pause app activity if unused\" so Android never removes the permissions after months without opening the app." to
            "Funga \"Pause app activity if unused\" kugira ngo Android itazakuraho uburenganzira nyuma y'amezi utafungura porogaramu.",
        "Battery saver is on" to "Kuzigama bateri birafunguye",
        "Some phones stop background apps in battery saver. If calls are missed, turn it off or add Call Announcer to its exceptions." to
            "Telefone zimwe zihagarika porogaramu zikorera inyuma iyo bateri izigamwa. Niba abaguhamagara batavugwa, bifunge cyangwa ushyire Call Announcer mu zisonewe.",
        "Open battery saver" to "Fungura kuzigama bateri",
        "Ringer on" to "Inzogera irafunguye",
        "The phone is on silent or vibrate, so calls are not announced until the ringer is turned back on." to
            "Telefone iri ku ituze cyangwa ku kunyeganyega, bityo abaguhamagara ntibavugwa kugeza inzogera yongeye gufungurwa.",
        "Calls are announced while the ringer is on." to "Abaguhamagara bavugwa iyo inzogera ifunguye.",
        "Real call test" to "Igerageza n'umuhamagaro nyawo",
        "A real call was announced while the app was closed on this phone." to
            "Umuhamagaro nyawo wavuzwe porogaramu ifunze kuri iyi telefone.",
        "Close the app (swipe it away from recent apps), then ask someone to call you. This proves the phone lets Call Announcer work in the background." to
            "Funga porogaramu (uyikure mu zifunguye vuba), hanyuma usabe umuntu aguhamagare. Ibi bigaragaza ko telefone yemerera Call Announcer gukorera inyuma."
    )
}
