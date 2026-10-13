# Call Announcer

An Android app that speaks the caller's name (or reads the number two digits at a time) while the phone rings, so you know who is calling without looking at the screen.

It keeps working when the app is closed, swiped away from recent apps, or after a reboot, and it speaks louder than the ringtone while briefly lowering it.

## Features

- **Announces every incoming call** — contact name if saved, otherwise the number in pairs ("07 85 46 …").
- **Works in the background** — even when the app is closed, removed from recents, or the phone was just restarted and not yet unlocked.
- **Louder than the ringtone** — reads your ringtone volume, speaks about 30% louder, and lowers the ringtone while speaking (like a video ducking for a notification). All volumes are restored when the announcement ends or the call is answered/missed.
- **Respects the situation** — stays quiet when the phone is on silent/vibrate, when Do Not Disturb would silence the caller, or when you are already on another call.
- **Fast repeat callers** — the spoken audio is cached, so frequent callers are announced sooner.
- **Customizable** — announcement prefix ("Incoming call from…"), repeat count, delay before speaking, voice language, speed and pitch, and whether to announce contacts and/or unknown numbers.
- **Rwandan & French names** — Kinyarwanda names are spoken with the Swahili voice (or respelled for English), French names with the French voice. A contact's phonetic name from Contacts is used when saved.
- **Announcements in Kinyarwanda, French or English** — *"Uguhamagara ni …"*, *"Appel de …"*, *"Incoming call from …"*.
- **Network and spam hints** — unsaved Rwandan numbers can start with "an MTN number" / "an Airtel number"; numbers you marked as spam (or your phone blocked before) start with a warning.
- **Per-contact rules** — fix how a name is pronounced, always announce someone (even in quiet hours), or never announce them.
- **Quiet hours** — stay silent at night; the phone still rings.
- **Stop speaking** — press a volume or power button, turn the phone face down, or shake it.
- **Headphones and car** — announce on the speaker and headphones, only in headphones / the car, or not at all.
- **Messages and missed calls** — optionally announce new WhatsApp, SMS, Telegram, Signal… messages, and say who you missed when you unlock the phone.
- **Quick Settings tile and home-screen widget** — turn announcing on or off in one tap.
- **App in Kinyarwanda, French or English** — follows the phone language or can be chosen in Settings → Appearance.
- **Backup and restore** — save settings, contact pronunciations and spam numbers to a file and restore them on a new phone.
- **History** — a log of announced calls, with replay, mark as spam and pronunciation fixes.

## Requirements

- Android 7.0 (API 24) or newer; tested on a TECNO phone running Android 15 (HiOS).
- A text-to-speech engine with an offline voice installed (e.g. Speech Services by Google).

## Setup on the phone

On first launch the app shows its privacy policy. Accepting is remembered, so it's asked only once (or again if the policy version changes). Declining closes the app, and no calls are announced until the policy is accepted. The policy can be reread anytime from **Settings → About → Privacy policy**.

After that, the app opens on the **Setup** tab until the phone is fully prepared. It checks everything that can stop announcements on any Android brand, and the status only shows **Active** once every required item passes. Tap **Fix everything** to open each Android setting in turn; approve it, come back, and the next one opens automatically.

Required:

| Item | Why it is needed |
| --- | --- |
| Phone and call log access | Detect incoming calls and get the number |
| Contacts access | Speak saved names; lets Android wake the app for calls from contacts |
| Caller ID app (Android 10+) | Android wakes the app for every call, even when the phone has frozen it |
| Unrestricted battery use | Stops the system from putting the app to sleep |
| Background activity allowed | Shown only if the user restricted the app's background use |
| Manufacturer auto-start (TECNO, Xiaomi, Oppo, Vivo, Huawei, Asus) | These phones stop closed apps that are not on their auto-start list. Android cannot report this switch, so the user confirms it (a successful real call while closed also confirms it) |
| Offline voice installed | A speech engine with a downloaded voice |
| Announcer switched on | The background service is running |

Recommended: notifications, "Pause app activity if unused" turned off, battery saver warning, ringer on, and a **real call test**: close the app and receive a call; once it is announced, the checklist records that this phone really works in the background.

If a required item breaks later (for example after a system update), the background notification shows "Setup incomplete" with the item to fix.

**Caller ID role and Truecaller:** only one app can hold the caller ID role. If Call Announcer takes it, Truecaller's spam blocking and caller pop-up may stop working; if Truecaller takes it back, announcements become unreliable while this app is closed. Calls are never blocked by Call Announcer.

## Building

You need JDK 17+ and the Android SDK (API 36). Android Studio is the easiest option.

1. Copy the debug signing key into the project root (it is not committed):

   ```powershell
   Copy-Item "$env:USERPROFILE\.android\debug.keystore" .\debug.keystore
   ```

   Any `debug.keystore` with alias `androiddebugkey` and password `android` works. If you don't have one, building any app once in Android Studio creates it.

2. Build and install on a connected phone:

   ```powershell
   .\gradlew.bat installDebug
   ```

   On macOS/Linux use `./gradlew installDebug`.

3. Run the unit tests:

   ```powershell
   .\gradlew.bat :app:testDebugUnitTest
   ```

Release builds are signed with `my-upload-key.jks` (or `KEYSTORE_PATH`) using the `STORE_PASSWORD` and `KEY_PASSWORD` environment variables. `assembleRelease` produces one APK per CPU architecture (`armeabi-v7a`, `arm64-v8a`, `x86_64`) plus a `universal` APK in `app/build/outputs/apk/release/`. Most modern phones use `arm64-v8a`; pick `universal` if unsure.

### Installing over Wi-Fi

```powershell
adb pair <phone-ip>:<pairing-port>      # once, using the code shown on the phone
adb connect <phone-ip>:<port>
$env:ANDROID_SERIAL = "<phone-ip>:<port>"
.\gradlew.bat installDebug
```

If installation fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, an older copy signed with a different key is installed; uninstall it first (this deletes its settings and history).

## WhatsApp and other app calls

Internet calls (WhatsApp, WhatsApp Business, Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, LINE, Discord, Zoom, imo, BOTIM) never reach Android's phone APIs. With **Notification access** granted (Setup → "WhatsApp and app calls"), `VoipCallListenerService` watches only those apps' incoming-call notifications and announces e.g. *"Incoming call from Jean, on WhatsApp"* or *"…, WhatsApp video call"*. Announcing stops when the call notification is answered, declined or removed. Each group can be turned off in **Settings → Calls from apps**.

Limits: the caller is whatever name the app shows (a number only when the app shows one); lowering the ringtone depends on the calling app using the ringer volume; nothing is announced if that app's notifications are off. On Android 13+, sideloaded APKs may need **App info → ⋮ → Allow restricted settings** before notification access can be switched on.

## How it works

```
Incoming call
   ├── CallAnnouncerScreeningService   (Android binds it before ringing; wakes a frozen app)
   ├── CallAnnouncerService            (foreground service listening to call state)
   └── CallStateReceiver               (PHONE_STATE broadcast fallback)
            │
            ▼
   IncomingCallHandler  ── dedupes the same call, checks settings / DND / other calls,
            │               looks up the contact and builds the sentence
            ▼
   DirectTtsAnnouncer   ── synthesizes (or reuses cached) audio, then
            │               AnnouncementVolumeController raises the announcement,
            │               lowers the ringtone, and plays the audio in-process
            ▼
   Call answered / ended ── speech stops and all volumes are restored
```

Notes on design choices:

- Announcements play on the **alarm** stream, so they are independent of media volume and still audible when media is muted.
- Audio is synthesized to a file and played by the app itself, together with a silent keep-alive track. Some phones (e.g. TECNO's "Hiber") freeze background apps mid-speech; an app that is playing audio is not frozen.
- Original volumes are saved to storage before being changed, so they are restored even if the app is killed during a call.
- Settings live in device-protected storage (Direct Boot), which is readable right after a reboot before the first unlock. Call history is only written after the phone has been unlocked.

## Project structure

```
app/src/main/java/com/example/
├── service/    Call screening service, foreground service, incoming call handling
├── receiver/   Phone state and boot receivers
├── tts/        Text-to-speech playback, audio cache, keep-alive track
├── util/       Setup checklist (ReadinessChecker), volume control, number matching, contact lookup, OEM auto-start screens
├── data/       Settings, contact rules, backups and call history (Room)
├── i18n/       Kinyarwanda and French screen texts
└── ui/         Jetpack Compose screens: Home, Settings, History, Setup (with SetupFixer), contact rules, privacy policy
```

## Troubleshooting

- **No announcement when the app is closed** — open the Setup tab and make sure every required item is green, especially *Caller ID app*, *Unrestricted battery use*, and *auto-start*.
- **Announcement is quiet** — the alarm volume is raised automatically, but if "Announce Louder Than Ringtone" is off, the current alarm volume is used.
- **Language missing from the list** — only voices downloaded on the phone are shown. Add one in Android *Settings → Text-to-speech → Install voice data*.
- **Debug logs** — detailed logs only appear in debug builds:

  ```powershell
  adb logcat -s IncomingCallHandler DirectTtsAnnouncer AnnouncementVolume CallScreening CallStateReceiver CallAnnouncerService
  ```
