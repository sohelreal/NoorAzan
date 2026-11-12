# NoorAzan v2 (Updated)

## Fixes Included in v2
1. **Reliable Background Azan Audio on Non-Pixel Devices (Samsung, Xiaomi, Oppo, Vivo):**
   - Configured `PlayerService` as a `mediaPlayback` Foreground Service with `AudioAttributes.USAGE_ALARM` and `AudioFocusRequest`.
   - Added `PARTIAL_WAKE_LOCK` and `AlarmManager.setExactAndAllowWhileIdle()` in `AlarmScheduler.kt` to bypass aggressive OEM Doze/battery restrictions.
   - Added `BootReceiver.kt` to restore alarms after device reboot.
2. **Accurate Qibla Compass Across All Devices:**
   - Added `GeomagneticField` declination correction in `PrayerCalc.kt` to align Magnetic North with True North.
   - Added circular low-pass exponential smoothing and sensor accuracy monitoring (`SENSOR_STATUS_UNRELIABLE` calibration alerts) in `QiblaSensorManager.kt`.

## Setup Note
- Place your audio file at `app/src/main/res/raw/azan.mp3` before building.
