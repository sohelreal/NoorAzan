# NoorAzan v8

A clean, lightweight, ad-free Android Prayer & Qibla companion built with Jetpack Compose.

## Key Improvements in v8
1. **GitHub Actions CI/CD Fixed:**
   - Configured `gradle/actions/setup-gradle@v4` with Gradle `8.5` and automatic Debug APK artifact upload (`NoorAzan-debug-apk`), eliminating missing `gradlew` wrapper failures.
   - Added `gradle.properties` and `gradle/wrapper/gradle-wrapper.properties` for seamless Android Studio compatibility.
2. **Bug Fixes & Stability:**
   - **Runtime Notification Permission:** Requests `POST_NOTIFICATIONS` on Android 13+ (`TIRAMISU`) so foreground Azan alerts appear reliably.
   - **WakeLock & Resource Safety (`PlayerService.kt`):** Added non-reference-counted WakeLock management, `use {}` block on `AssetFileDescriptor` to prevent file descriptor leaks, and a direct Stop button in the UI.
   - **Sensor Null-Safety (`QiblaSensorManager.kt`):** Safely guards sensor registration and ensures rotation matrix calculations only run after both accelerometer and magnetometer readings arrive.
   - **Release Shrinking:** Enabled R8 minification and resource shrinking in `app/build.gradle.kts` to keep the APK lightweight.

## Custom Azan Audio
- Optional: Place `azan.mp3` (and optionally `azan_fajr.mp3`) inside `app/src/main/res/raw/`. If omitted, the app automatically falls back to the device's default alarm sound.
