# Dayline — native Android schedule planner

A Kotlin Android app using native Android Views. Includes a Monday-first month calendar, daily agenda, today shortcut, event markers, categories, notes, and create/edit/delete dialogs. Plans are stored privately on the device and survive restarts. No account, network permission, or backend is required.

## Open in Android Studio

1. Install Android Studio with Android SDK support.
2. Choose **Open** and select this **Dayline** folder (the folder containing `settings.gradle.kts`).
3. Use **JDK 17** or a compatible bundled Android Studio JDK for Gradle. Install **Android SDK Platform 36** through SDK Manager if prompted.
4. Allow Gradle sync to download the pinned build dependencies. Internet access is needed for the first sync.
5. Select or create an emulator running **Android 8.0 / API 26 or newer**, or connect an Android device with USB debugging enabled. Press **Run**.

The official Gradle wrapper is included for macOS/Linux and Windows. Command-line build: `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). Android Studio generates `local.properties` for your own SDK path; no machine-specific SDK path is included.

Build configuration: Android Gradle Plugin 8.13.2, Kotlin 2.2.21, Gradle 8.13, Java bytecode 17, compile/target SDK 36, minimum SDK 26. Build compatibility reference: https://developer.android.com/build/releases/gradle-plugin

## Use

Tap a date, then **Add a plan**. Enter a title, date, start/end time, category, and optional notes. Tap a saved card to edit it. Delete asks for confirmation. The end time must be later than the start time on the same day. Overlapping plans are allowed. Dates and times are local wall-clock values; they do not shift with timezone changes.

This version supports one-time plans. It does not include reminders, recurring events, cloud sync, or Android system-calendar integration. Uninstalling the app or clearing storage removes its data. Android backup is disabled.

## Verification

Project structure, XML resources, Gradle wrapper archive, and shell launcher syntax were checked. Compilation and emulator testing were **not performed** because the authoring environment did not contain a Java runtime or Android SDK.

After opening in Android Studio, verify:

- Create plans on different dates and confirm calendar markers and time ordering.
- Edit title, date, time, category, and notes; ensure the old entry is replaced.
- Cancel a deletion, then confirm deletion.
- Reject an empty title and an end time at/before the start time.
- Restart the app and verify plans remain.
- Navigate between months, use Today, rotate the screen, and test large system text.

Source entry point: `app/src/main/java/com/dayline/planner/MainActivity.kt`.
