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
