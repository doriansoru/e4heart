# e4heart (Native Wear OS)

[Italiano 🇮🇹](./README.it.md) | **English 🇺🇸**

<p align="center">
  <img src="app/src/main/res/drawable/preview.png" width="250" alt="e4heart Preview">
</p>

Native Wear OS application developed in Kotlin and Jetpack Compose for real-time heart rate monitoring, optimized for **PEM pacing** (Post-Exertional Malaise).

## Description and Purpose
The app is specifically designed to support individuals suffering from **ME/CFS** and **Long COVID** in managing their energy through heart rate monitoring. The goal is to prevent exceeding the ventilatory anaerobic threshold (V/AT), thereby reducing the risk of exertion-related relapses (crashes).

## Monitoring Logic and Scientific Basis
The app's logic is based on the **Workwell Foundation** guidelines for heart rate pacing:

1.  **Threshold Calculation (Alert)**: The alert threshold is set to **RHR + 15 BPM** (Resting Heart Rate + 15). This value is a conservative approximation of the anaerobic threshold for those suffering from PEM-related conditions.
2.  **Two-Minute Rule**: Following scientific guidelines ("Avoid spending time above the V/AT for more than two minutes"), the app activates vibration only if the heart rate remains above the threshold continuously for at least 120 seconds. This avoids false positives due to brief momentary spikes.
3.  **Recovery Threshold**: Following the foundation's recommendations, the alert stops only when the heart rate falls within 10 BPM of the resting rate (**RHR + 10 BPM**). This ensures the body has recovered sufficiently before resuming activity.

**Source**: [Workwell Foundation - Pacing with a heart rate monitor](https://workwellfoundation.org/pacing-with-a-heart-rate-monitor-to-minimize-post-exertional-malaise-pem-in-me-cfs-and-long-covid/)

## Features
- Continuous heart rate monitoring.
- Interface optimized for circular screens.
- **Internationalization**: Full support for Italian and English.
- Precision slider (1 BPM steps) to set your Resting Heart Rate (RHR).
- Vibration alerts: discrete initial alarm and persistent reminders if the heart rate doesn't drop.
- **Ambient mode**: Night brightness optimization for continuous visibility without glare.
- **Pause/Resume**: Quick toggle (app or notification) to stop monitoring during charging, physically disconnecting the sensor to save energy.
- **Energy Efficiency**: Smart background update logic for Tile and Complications to preserve battery.
- **Reactive UI**: `StateFlow` architecture for superior smoothness and lower foreground power consumption.

## Installation
To install the app on your Wear OS smartwatch without compiling it from source, follow these steps:

1. **Download the APK**: Download the latest `app-debug.apk` file from the [Releases](https://github.com/doriansoru/e4heart/releases) section of this repository directly onto your Android smartphone.
2. **Install Easy Fire Tools**: On your Android phone, install **Easy Fire Tools** from the Google Play Store (a free app used to transfer files to wearable devices).
3. **Prepare your watch**: Make sure your Wear OS watch is connected to the same Wi-Fi network as your phone. On the watch, go to **Settings** > **Connectivity** > **Wi-Fi**.
4. **Send the app to your watch**:
   - Open **Easy Fire Tools** on your phone.
   - Tap the menu icon (top left), go to **Settings** > **Fire TV / Wear OS IP Address** and select your watch (or tap **Discover** to search for it automatically).
   - Go back to the main screen of the app, tap the **Custom APK** tab, and select the downloaded `app-debug.apk` file.
   - Tap the **Connect** icon at the top right, then tap **Install**.

Once completed, the app will automatically appear in your Wear OS watch's app list.

## Development and Compilation Guide
The project is a native Android application based on Gradle. You can compile it using simplified `Makefile` commands or directly via Gradle.

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Android SDK**: API 34 (Android 14) installed.
- **ADB (Android Debug Bridge)**: Required for installation and debugging on the watch.
- **Android Studio (optional)**: Recommended for development and UI preview.

### Compilation and Installation
Open the terminal in the project folder and use the following commands:

1.  **Compilation (APK Generation)**:
    ```bash
    make build
    ```
    (Or `./gradlew assembleDebug`)

2.  **Installation and Execution**:
    Ensure the watch is connected via ADB (Wi-Fi or Bluetooth Debugging) and type:
    ```bash
    make run
    ```
    (This command installs the APK and starts the main activity on the device).

3.  **Log Monitoring**:
    To view the app's system messages (filtered for `e4heart`):
    ```bash
    make log
    ```

4.  **Cleaning**:
    If you encounter build issues, clean the temporary files:
    ```bash
    make clean
    ```

## Hardware Requirements
- Any Wear OS watch with a heart rate sensor.
- Android 9.0 (API 28) or higher.

## ⚠️ Medical Disclaimer
This application is **not a medical device**. The data provided is for informational and personal monitoring support purposes only (e.g., PEM pacing). The app must not be used to diagnose, treat, or prevent any medical condition. Always consult a medical professional for health-related decisions.

## License
Distributed under the **GNU GPLv3** license. See the `LICENSE` file for details.

## Development Notes
This project was assisted by **Gemini CLI**, which handled the implementation of internationalization, the optimization of pacing logic based on scientific sources, and the documentation update.
