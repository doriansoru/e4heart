.PHONY: build run clean log

# Comando per compilare l'APK
build:
	./gradlew assembleDebug

# Comando per installare e avviare l'app
run:
	./gradlew installDebug
	adb shell am start -n com.e4heart/.MainActivity

# Pulizia dei file di build
clean:
	./gradlew clean

# Visualizzazione dei log in tempo reale
log:
	adb logcat | grep -i "com.e4heart"
