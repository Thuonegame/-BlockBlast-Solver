# Block Blast Solver

Android application project for an on-device Block Blast assistant.

## Development status

This is an early implementation milestone, **not a finished auto-player**. The repository currently contains:
- Android Gradle project configuration and launcher screen.
- Android manifest, theme, and Accessibility Service declaration.
- A pure Kotlin 8x8 board search core.

Still required before an APK can be called complete:
- Screen capture implementation and frame acquisition.
- Reliable board and tray-piece recognition across device sizes and themes.
- Calibrated drag gestures and floating overlay controls.
- Per-move verification, recalculation, pause/resume, and statistics.
- Unit/instrumentation tests, CI build verification, and real-device testing.

No release APK is claimed until CI succeeds and the app has been tested on a physical device.

## Build

Open in Android Studio and build the `app` module. CI workflow will be added after the app skeleton is complete.

## Permissions

Screen capture and Accessibility permissions are sensitive. They must be enabled explicitly by the user, and automated gestures must remain disabled until recognition and move validation are implemented.
