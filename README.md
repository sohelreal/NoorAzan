# Noor – Azan & Prayer Times (Android, Kotlin)

Ad-free, no login, no subscription, no analytics. Kotlin + Jetpack Compose, minSdk 26, no Google Play Services.

## Features
- GPS prayer times (offline astronomical calculation): MWL, ISNA, Egypt, Makkah, Karachi; Standard/Hanafi Asr; per-prayer minute adjustment; manual coordinates.
- Five-time Azan: exact alarms, Azan audio / notification sound / silent, per-prayer on/off, reschedules after reboot, time-zone change and app update.
- Home dashboard: next prayer + live countdown, today's times, Hijri date, continue-reading card.
- Quran: 114 surahs, Arabic + English + Bangla, full-Surah audio (5 reciters, auto-plays next Surah, background playback), "Download all" for offline reading.
- Salah Guide: Fard / Sunnah / Nafl / Witr per prayer, Niyat, wudu and prayer steps, Duas.
- Qibla compass (rotation-vector sensor + magnetic declination).
- Hijri calendar with Islamic events; Ramadan calendar with Sehri and Iftar times.
- Bangla / English / Arabic (RTL), green & white design, light / dark / system theme.

## Build
- Android Studio (Hedgehog+): open this folder, press Run.
- Command line: `./gradlew assembleDebug` or `./gradlew assembleRelease` (JDK 17).
- GitHub: push to GitHub; Actions → "Build APK" builds debug and release APKs and uploads them as artifacts
  (tag `v*` also attaches the release APK to a GitHub Release).
- Release APK is signed with the debug key so it installs directly; add your own signing config before publishing to a store.

## Online data (only these, no accounts)
- Quran text per surah: api.alquran.cloud (Uthmani, Saheeh International, Bengali), cached on device after first open.
- Quran audio: cdn.islamic.network (full-Surah mp3, streamed).
- Azan audio: downloaded once on first launch from cdn.aladhan.com and stored on the device. You can replace it in
  Settings → Notifications (URL or any audio file from your phone).
Prayer times, Qibla, Hijri calendar and Salah guide work fully offline.

## Notes
- Prayer-time methods vary by region; verify against your local mosque and use the minute adjustment if needed.
- Hijri dates use the Umm al-Qura calendar with a ±2 day adjustment for local moon sighting.
- On Android 12+, allow "Alarms & reminders"; on some phones also disable battery optimisation (both are prompted in-app).
