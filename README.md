# LSFG Mali 2.0

Frame generation for **Mali** GPUs on Android, using the Lossless Scaling
frame-generation engine and an in-app **PanVK** Vulkan driver — no root.

This is a rewrite of the original LSFG-for-Mali shell with a Material 3 UI,
adaptive target-FPS pacing, an optional accessibility touch bridge, and a
debugging setup that captures everything a bug report needs.

- Engine: `lsfg-vk` (MIT), Android port by the LSFG-Android project
- Driver: PanVK (MIT), bundled as an in-app ICD
- Shell: GPL-3.0 (see `LICENSE`)
- `Lossless.dll` is **never** bundled — you import your own licensed copy

## Install

Grab `app-release.apk` from the [releases page](https://github.com/synapzsee/Lsfg-mali/releases)
and install it on an **arm64-v8a** device running Android 10 (API 29) or newer.

## First run

1. **Import your Lossless.dll**
   *Game files → Import*, then pick `Lossless.dll` from your Lossless Scaling
   installation. The app copies it into its private storage, hashes it and
   extracts the embedded frame-generation shaders.

   | DLL version | Result |
   |---|---|
   | 3.2.1.0 | works — FP32 shader path |
   | ≥ 3.2.2.0 | works — adds the optional FP16 shader set |

   The import screen shows the detected version and which path will be used.

2. **Grant the overlay permission** — *Display over other apps*. The generated
   frames are presented by a full-screen overlay window.

3. **Start the session**, then pick the game app in the Android capture dialog.

   > On Android 14+ the capture dialog can scope the session to a **single
   > app**. Choose the game instead of "Entire screen": the generated overlay
   > is then not part of the captured window, which removes the feedback loop
   > that otherwise shows up as a smeared or lagging image.

4. **Optional: the touch bridge.** Some ROMs swallow touches aimed at overlay
   windows. Enable *LSFG Mali touch bridge* in Accessibility settings and the
   app replays those touches as accessibility gestures. It asks for no window
   content and reacts to no accessibility events.

## What the app does

- Captures the screen through MediaProjection and feeds frames to the
  lsfg-vk engine.
- Presents generated frames through a full-screen overlay with a draggable
  bubble and an in-game control panel (bypass, target FPS, stop).
- Adapts the internal multiplier and the target-FPS cap to the source frame
  rate, with EMA smoothing and outlier rejection.
- Recreates the virtual display on rotation and keeps the overlay attached.
- Captures its own logcat, the engine's native ring log and every shell event
  into a rotating session log.

## Debugging

The app is built to be debuggable:

- **Debug log** card — live tail of the shell log, copy to clipboard.
- **Diagnostics report** card — one tap copies every setting the engine sees,
  plus driver, DLL, shader-cache and permission state.
- **Session log** — `logcat --pid`, the full logcat buffer filtered to this
  app, and the engine's own ring log, merged into one rotating file.
  Share it or save it to `Downloads/LSFGMali`.
- The session log ends with a summary line: duration, generated/posted/unique
  frames, average source and output FPS, average latency, multiplier, FP16/FP32.

## Building

```bash
./gradlew :app:assembleRelease
```

Requirements: JDK 17, Android SDK 36, Gradle 8.14.4 (wrapper included). The
release build is signed with the debug key unless a `keystore.properties` file
is present:

```properties
storeFile=keys/lsfgmali-release.jks
storePassword=...
keyAlias=lsfgmali
keyPassword=...
```

GitHub Actions builds and publishes the APK on every `v*` tag.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `extractShaders rc=-4` | The shader cache directory was missing — update to 2.0.2 or later. |
| `extractShaders rc=-1` | The file is not a genuine `Lossless.dll`. |
| Smeared or lagging picture | Capture feedback loop — pick the game app in the capture dialog instead of "Entire screen". |
| Overlay never appears | "Display over other apps" permission missing, or the source app is static (the overlay hides itself when no new frames arrive). |
| Touches do nothing | Enable the touch bridge in Accessibility settings, or use a ROM that passes touches through. |
| `INIT FAILED rc=…` | Try the Stock Mali driver, a lower capture scale, or read the session log. |

## Credits

- Lossless Scaling (Lossless Scaling GmbH) — the frame-generation technology
  and `Lossless.dll`, which you supply yourself.
- The original LSFG-for-Mali shell by Syed Hassan.
- The lsfg-vk and LSFG-Android projects.
- The Panfrost / PanVK project.

See `THIRD_PARTY_NOTICES` for the full licence texts.
