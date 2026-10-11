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
- **History** — a log of announced calls, with replay.

## Requirements

- Android 7.0 (API 24) or newer; tested on a TECNO phone running Android 15 (HiOS).
- A text-to-speech engine with an offline voice installed (e.g. Speech Services by Google).

## Setup on the phone

Open the **Permissions** tab in the app and complete each item:

| Item | Why it is needed |
| --- | --- |
| Phone, Call log, Contacts | Detect incoming calls, get the number, and look up the contact name |
| Notifications | Show the background "Monitoring incoming calls" notification |
| Set as Caller ID App | Lets Android wake the app for every call, even when the phone has frozen it |
| Unrestricted battery usage | Stops the system from killing the background service |
| Auto-start (TECNO, Xiaomi, Oppo, Vivo, Huawei…) | Allows the app to start itself after a reboot or when a call arrives |

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

Release builds are signed with `my-upload-key.jks` (or `KEYSTORE_PATH`) using the `STORE_PASSWORD` and `KEY_PASSWORD` environment variables.

### Installing over Wi-Fi

```powershell
adb pair <phone-ip>:<pairing-port>      # once, using the code shown on the phone
adb connect <phone-ip>:<port>
$env:ANDROID_SERIAL = "<phone-ip>:<port>"
.\gradlew.bat installDebug
```

If installation fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, an older copy signed with a different key is installed; uninstall it first (this deletes its settings and history).

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
├── util/       Volume control, number formatting/matching, contact lookup, OEM auto-start screens
├── data/       Settings and call history (Room)
└── ui/         Jetpack Compose screens: Dashboard, History, Settings, Permissions
```

## Troubleshooting

- **No announcement when the app is closed** — check every item in the Permissions tab, especially *Set as Caller ID App*, *Unrestricted battery*, and *Auto-start*.
- **Announcement is quiet** — the alarm volume is raised automatically, but if "Announce Louder Than Ringtone" is off, the current alarm volume is used.
- **Language missing from the list** — only voices downloaded on the phone are shown. Add one in Android *Settings → Text-to-speech → Install voice data*.
- **Debug logs** — detailed logs only appear in debug builds:

  ```powershell
  adb logcat -s IncomingCallHandler DirectTtsAnnouncer AnnouncementVolume CallScreening CallStateReceiver CallAnnouncerService
  ```
