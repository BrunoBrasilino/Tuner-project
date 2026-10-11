# Tuner

An Android chromatic tuner for musical instruments, built with Kotlin and Jetpack Compose. It detects one note at a time through the microphone and displays its frequency, note, octave, and deviation in cents.

## Features

- Adjustable A4 reference from 400 to 480 Hz, with up to three saved presets.
- Automatic sensitivity or a manual dBFS threshold, with an input-level indicator.
- The last valid reading remains visible, dimmed, for up to five seconds.
- Portuguese, English, and French interfaces.
- Local audio processing: recordings are neither saved nor sent to a server.

## Run the app

1. Open [`code/android`](code/android) in Android Studio.
2. Wait for Gradle Sync, then run the app on an Android device or emulator.
3. Grant microphone permission when the app opens.

The project requires JDK 17 and Android SDK 35. You can also build it with the Gradle wrapper in `code/android`. Development APKs are generated locally and are not committed to this repository.

See [docs/README.md](docs/README.md) for more details, the [1.0.0 release notes](docs/RELEASE_V1.0.0.md) for this version, and the [backlog](docs/BACKLOG.md) for known limits and planned validation.
