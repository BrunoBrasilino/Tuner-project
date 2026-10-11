# Tuner 1.0.0

The first consolidated version of a personal chromatic tuner for Android, designed primarily for guitar and acoustic guitar.

## What's new

- Real-time note, octave, frequency in Hz, and deviation from −50 to +50 cents. The last valid reading remains visible for up to five seconds.
- A wider cents scale with marks at every cent, reference marks at −50, −25, 0, +25, and +50, and a continuously positioned pointer.
- Automatic or manual sensitivity, with an editable dBFS threshold and an input-level marker.
- An Auto/Manual switch that toggles from anywhere on the capsule, provides rounded touch feedback, and appears as one accessibility control.
- Adjustable A4 reference from 400 to 480 Hz, with 440 Hz as the default and up to three saved presets.
- Portuguese, English, and French interfaces. Audio is processed on the device; the app does not save recordings.

## Installation

Download `tuner-v1.0.0-debug.apk` from the release assets and install it on Android. Grant microphone permission when prompted. This is a development build signed with a debug key; future updates must use the same signing key to install over this version.

APK SHA-256: `0f52a774f3c1d4f4bac82cf2ad0ff3c126c1b993d79ec4023a077ca8f3e49490`.

## Validation and current limits

The interface was checked in emulators with compact and large screens, including readings near −50, 0, and +50 cents and taps on the left, center, and right of the switch. The user also tested the switch fix on a physical phone. Pitch accuracy, latency, and sensitivity still need systematic measurement with real instruments on the Samsung S24 Ultra; results may vary with the microphone and Android audio processing. Accessibility focus was checked with a test service, but spoken TalkBack output has not yet been verified.
