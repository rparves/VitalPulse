# VitaPulse — Android fitness & nutrition app

Android 14 (API 34) and newer · Kotlin · Jetpack Compose · Material 3

## Features
- Food scanning: on-device photo recognition (ML Kit image labeling) mapped to a built-in
  nutrition table, plus barcode scanning of packaged food via the Open Food Facts API.
- Bangladeshi food database: 100+ local dishes (kacchi, tehari, ilish, rui, bhorta, dal,
  fuchka, chotpoti, pitha, mishti…) with approximate nutrition; search in English or Bangla.
- Daily readiness score (0–100) from last night's sleep and yesterday's training load, food
  and water, with a per-factor breakdown and tip; logging streak with personal best.
- Workout logging: sets × reps × weight for strength, minutes for cardio/timed moves,
  estimated calories (MET), daily training card and weekly workout-minutes chart.
- Food diary with meals, calorie budget (Goal − Food + Exercise) and macro breakdown.
- BMI and body plan: right after creating a profile, see BMI (WHO or South Asian cut-offs),
  a healthy target weight, a safe timeline, daily targets (calories, macros, steps, active
  minutes, water, sleep) that can be applied in one tap, and a 7-day Bangladeshi-friendly
  meal plan scaled to your calories with one-tap logging.
- Personal fitness profile: BMI, BMR (Mifflin-St Jeor), TDEE and a suggested calorie target.
- Goals: steps, calories, water, active minutes, sleep, target weight.
- Activity, heart rate, sleep and workouts from Health Connect (phone sensors, Fitbit,
  Pixel/Galaxy Watch, Samsung Health, Google Fit and others).
- Today dashboard: activity rings + metric tiles + weekly highlights.
- Trends: 7-day steps and calorie charts with goal lines, weight trend line.
- Workouts library: 876 cardio, strength, stretching and plyometric exercises with
  animated demonstrations, muscles worked, equipment, level and step-by-step form instructions.
  40 essentials (treadmill, rowing, jump rope, plank, squats, lunges, deadlift, chest press,
  bench press, push-ups, pull-ups, lat pulldown, shoulder press, curls, triceps…) ship as GIFs
  inside the app and work offline; the rest load their images online. Search plus
  category and muscle filters.
- Notifications: step-goal celebration, hydration nudges, "steps to go", evening recap
  (quiet hours 22:00–08:00; details hidden on the lock screen).

## Security
- Local data in a SQLCipher (AES-256) encrypted Room database.
- Database key is random, wrapped by an AES-256-GCM key in the Android Keystore
  (StrongBox hardware when available); never exported or backed up.
- Optional app lock (biometric or device PIN) with re-lock after 30 s in background.
- Optional private screen (FLAG_SECURE: no screenshots, hidden in recents).
- HTTPS only; cleartext traffic disabled; user-installed CAs not trusted.
- Cloud backup and device transfer disabled for all app data.
- Health Connect access is read-only; only a barcode or food name is ever sent over the network.
- Release builds are minified and obfuscated with R8; debug logs stripped.
- Input validation on all user-entered and network data; response size capped.

## Install link
Latest build, always: https://github.com/rparves/VitalPulse/releases/latest/download/VitaPulse.apk

## Get the APK (no Android Studio needed)
1. Create a new GitHub repository and push this folder to the `main` branch.
2. Open the repo's **Actions** tab — the *Build APK* workflow runs automatically.
3. When it finishes, download **VitaPulse-APKs** from the run's Artifacts section.
4. Install `app-debug.apk` (or `app-release.apk`) on an Android 14+ phone
   (allow "Install unknown apps" for your browser or file manager).

To sign the release APK with your own key, add repository secrets
`VP_KEYSTORE_BASE64`, `VP_KEYSTORE_PASSWORD`, `VP_KEY_ALIAS`, `VP_KEY_PASSWORD`.
Without them the release build is signed with a debug key (fine for testing,
not for Play Store).

## Build locally
Open the folder in Android Studio (Ladybug or newer) and press Run, or:
```
./gradlew assembleDebug
```

## Open-source / free services used
- Open Food Facts API (ODbL) — packaged food nutrition, no API key.
- free-exercise-db (Unlicense / public domain) — exercise data and images.
- Coil (Apache 2.0) — image loading and GIF playback.
- Health Connect (Jetpack), CameraX, Room, WorkManager, Biometric — Apache 2.0.
- SQLCipher for Android — BSD-style license.
- ML Kit barcode scanning & image labeling — free on-device Google SDKs.

## Known limits
- Photo-based calorie estimates are approximate; the user confirms the food and portion.
- Steps require Health Connect data (most phones and wearables write it automatically).
