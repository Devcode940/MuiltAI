# MuiltAI — Build & Deployment

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK with API 34
- Gradle wrapper from the repository
- Android device/emulator for runtime testing

The project currently targets Java/Kotlin JVM 17, minSdk 26, compileSdk 34 and targetSdk 34.

## 1. Clone

```bash
git clone https://github.com/Devcode940/MuiltAI.git
cd MuiltAI
```

## 2. Verify the toolchain

```bash
java -version
./gradlew --version
```

The Java version should report 17.

## 3. Termux + Ubuntu

Install Termux packages:

```bash
pkg update
pkg install git wget unzip openjdk-17
```

Enter Ubuntu, then install the Android command-line tools and SDK as appropriate for your environment. Set:

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

Accept SDK licenses:

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

Then from the repository:

```bash
chmod +x gradlew
./gradlew test
./gradlew lintDebug
```

## 4. Debug APK

```bash
./gradlew assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/
```

Install on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 5. Release APK

```bash
./gradlew assembleRelease
```

The release build uses R8/minification and resource shrinking.

Do not commit signing keys, passwords, or keystore files.

## 6. Production signing

Create a keystore outside the repository:

```bash
keytool -genkeypair \
  -v \
  -keystore multai-release.jks \
  -alias multai \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

For CI, store the keystore and credentials as encrypted GitHub Actions secrets. Wire them into a dedicated release signing configuration rather than putting credentials in `build.gradle.kts`.

## 7. Google Play AAB

Build the bundle:

```bash
./gradlew bundleRelease
```

Output:

```text
app/build/outputs/bundle/release/
```

Upload the signed `.aab` through Google Play Console. Use Play App Signing for production distribution.

## 8. Test suite

JVM tests:

```bash
./gradlew test
```

Android instrumentation tests:

```bash
./gradlew connectedDebugAndroidTest
```

Lint:

```bash
./gradlew lintDebug
```

Full local validation:

```bash
./gradlew clean test lintDebug assembleDebug assembleRelease bundleRelease
```

## 9. CI

The repository workflow validates tests, lint and APK/AAB builds. Keep the workflow green before merging or publishing a release.

## 10. Security release checklist

- [ ] Confirm all provider URLs are HTTPS.
- [ ] Confirm no `javascript:`, `file:`, `content:`, `data:` or `intent:` navigation is accepted.
- [ ] Confirm WebView mixed content remains disabled.
- [ ] Confirm WebView file/content access remains disabled.
- [ ] Test renderer-crash recovery.
- [ ] Test downloads on API 26, 29 and current Android.
- [ ] Test Android back navigation.
- [ ] Test desktop/mobile UA switching.
- [ ] Test custom-provider validation.
- [ ] Test Room migration from every supported schema version.
- [ ] Verify release APK/AAB is signed correctly.
- [ ] Verify no secrets are present in the APK, repository or CI logs.

## 11. Performance checks

For a release candidate, profile:

- cold startup
- first WebView load
- WebView memory after opening/closing tabs
- provider search latency
- Room query count
- Compose recompositions
- APK/AAB size

The Profile Installer dependency is included so Android can consume baseline/profile information when provided.

## 12. Troubleshooting

### Gradle says Java is unsupported

```bash
java -version
```

Use JDK 17 and ensure `JAVA_HOME` points to it.

### Android SDK is not found

```bash
echo $ANDROID_HOME
adb version
```

Install API 34 and platform-tools, then restart the shell.

### WebView behaves differently on a device

Update Android System WebView/Chrome on the test device and repeat the test. WebView behavior depends on the installed Android WebView provider.

### Release build fails after R8

Inspect:

```text
app/build/outputs/mapping/release/mapping.txt
```

Add the smallest possible targeted keep rule to `app/proguard-rules.pro`; do not disable shrinking globally.
