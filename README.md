# VOXFLOW Android starter project

This is an original Android Studio project scaffold for VOXFLOW. It contains:
- A dark purple voice dictation dashboard.
- Android SpeechRecognizer integration in the app.
- A custom Android input method (keyboard) that can dictate into the currently focused text field.
- Copy and clear controls.
- Basic local text cleanup (not cloud AI).

## Build an APK
1. Install Android Studio (Hedgehog or newer) on a computer.
2. Open this `voxflow` folder in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Connect an Android phone with USB debugging enabled, or use an emulator.
5. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Android Studio will show the generated APK location, usually `app/build/outputs/apk/debug/app-debug.apk`.

## Enable the keyboard on your phone
1. Open VOXFLOW and grant microphone permission.
2. Tap **ACTIVATE VOXFLOW KEYBOARD**.
3. Enable VOXFLOW in Android's available keyboards settings.
4. In a text field, open the keyboard picker and select VOXFLOW.
5. Tap **TAP TO DICTATE** to insert recognised speech into the active field.

## Current limitations
- This is a starter MVP project, not a finished commercial release.
- Speech recognition uses Android's installed speech recognition provider; language availability and offline support vary by device/provider.
- The current "Enhance" button performs simple local cleanup only. Advanced AI rewriting needs a secure backend and an AI provider API key; do not put secret API keys in the Android app.
- The keyboard is intentionally a minimal voice-input keyboard, not a full QWERTY keyboard yet. Users can switch to their normal keyboard when they need to type manually.
- Test on your specific Android model and OS before distributing. A release APK must be signed before public distribution.
