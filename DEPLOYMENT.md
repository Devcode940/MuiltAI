# MuiltAI — Build, Test & Deployment Guide

## 1. Requirements

- Android Studio with an Android SDK installed.
- JDK 17.
- Android SDK Platform 34 or the compile SDK declared by `app/build.gradle.kts`.
- Android SDK Build Tools matching the installed Android Studio setup.
- Git.
- A physical Android device or emulator for instrumentation tests.

The project currently targets Android API 34 and uses Java/Kotlin JVM 17. Keep the Gradle wrapper as the source of truth for the Gradle version.

## 2. Clone

```bash
git clone https://github.com/Devcode940/MuiltAI.git
cd MuiltAI
```

## 3. Verify the JDK

```bash
java -version
./gradlew -version
```

The JVM used by Gradle must be Java 17.

### Termux / Ubuntu

If using Ubuntu inside Termux, install Java 17 and basic build tools:

```bash
sudo apt update
sudo apt install -y git unzip openjdk-17-jdk
java -version
```

If Android SDK tools are installed outside the Ubuntu prefix, export `ANDROID_HOME`/`ANDROID_SDK_ROOT` to that SDK location before building.

## 4. Local validation

Run the JVM tests:

```bash
./gradlew test
```

Run Android lint:

```bash
./gradlew lintDebug
```

Run instrumentation tests on a connected device/emulator:

```bash
./gradlew connectedDebugAndroidTest
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

The APK is produced under:

```text
app/build/outputs/apk/
```

## 5. Install a debug build

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If an old installation causes state/schema problems during development:

```bash
adb uninstall com.multaihub.app
./gradlew clean assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 6. Release build

The release build enables R8 minification and resource shrinking.

```bash
./gradlew clean test lintDebug assembleRelease
```

For a release APK:

```text
app/build/outputs/apk/release/
```

For Google Play distribution, prefer an Android App Bundle:

```bash
./gradlew bundleRelease
```

The AAB is produced under:

```text
app/build/outputs/bundle/release/
```

## 7. Signing

Never commit a keystore or passwords to Git.

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

For CI, store the keystore as an encrypted GitHub Actions secret or use the platform's protected signing configuration. Inject passwords through environment variables or Gradle properties, not source code.

A typical local `~/.gradle/gradle.properties` setup is:

```properties
MULTAI_STORE_FILE=/absolute/path/to/multai-release.jks
MULTAI_STORE_PASSWORD=use-your-local-secret
MULTAI_KEY_ALIAS=multai
MULTAI_KEY_PASSWORD=use-your-local-secret
```

Then wire those values into `signingConfigs` only in a private release configuration. Do not copy the example secrets into the repository.

## 8. Recommended release pipeline

Run these checks in order:

```bash
./gradlew clean
./gradlew test
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
./gradlew bundleRelease
```

Then manually verify:

- Provider search and filtering.
- Add/delete custom provider.
- Favorites and recently used providers.
- Mobile/desktop mode.
- Back/forward navigation.
- Popup/new-window behavior.
- File upload/download behavior.
- Full-screen media.
- Network loss/recovery.
- Process death and tab restoration.
- Notes and prompts.
- Export/import.
- Clear-all-data.
- Dark/light theme.
- Rotation and configuration changes.
- Accessibility with TalkBack.

## 9. WebView security release gate

Before publishing, verify that the WebView:

- Rejects `javascript:` URLs.
- Rejects `file:` URLs.
- Rejects `content:` URLs.
- Rejects `data:` URLs as top-level navigation.
- Rejects arbitrary custom URI schemes.
- Does not allow mixed HTTP content inside HTTPS pages.
- Does not expose file/content access unnecessarily.
- Does not create unmanaged popup WebViews.
- Does not log cookies, tokens, request headers, or page contents.
- Handles renderer crashes without taking down the whole activity.

## 10. GitHub Actions

CI should execute at least:

```bash
./gradlew test lintDebug assembleDebug
```

A device/emulator runner can additionally execute:

```bash
./gradlew connectedDebugAndroidTest
```

Release CI should build `bundleRelease` only after tests and lint pass.

## 11. GitHub release

Recommended sequence:

1. Merge the production branch after CI is green.
2. Update `versionCode` and `versionName`.
3. Update `CHANGES.md`.
4. Run the complete release gate.
5. Create a Git tag such as `v2.1.0`.
6. Publish the AAB through Google Play Console.
7. Attach the signed APK/AAB to the GitHub release if desired.

## 12. Rollback

If a release introduces a regression:

1. Stop staged rollout in Google Play Console.
2. Restore the previous known-good release.
3. Do not downgrade Room without a tested migration path.
4. Preserve user data whenever possible.
5. Reproduce the failure with a regression test before the next release.

## 13. Production checklist

- [ ] Release keystore backed up securely.
- [ ] Signing credentials are not in Git.
- [ ] `test` passes.
- [ ] Lint passes.
- [ ] Instrumentation tests pass.
- [ ] R8 release build passes.
- [ ] WebView security tests pass.
- [ ] Database migrations tested.
- [ ] No debug logging in release.
- [ ] Privacy policy reviewed.
- [ ] Data deletion behavior verified.
- [ ] Backup policy reviewed.
- [ ] Play Console data-safety declaration reviewed.
- [ ] Release notes updated.

## 14. Troubleshooting

### Gradle cannot find Java

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew -version
```

### SDK not found

```bash
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
```

Adjust the path to your actual SDK installation.

### Build cache problems

```bash
./gradlew --stop
./gradlew clean --refresh-dependencies
```

### Room schema/test failures

Do not delete the production database or use destructive migration just to make a test pass. Add and test the appropriate migration.

### WebView appears blank

Check:

```bash
adb logcat | grep -i -E 'chromium|WebView|crash|multai'
```

Then verify network connectivity, provider URL validation, Android System WebView/Chrome availability, and renderer-crash handling.
