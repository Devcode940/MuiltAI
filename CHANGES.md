# MultiAI Hub - Changes

## Version 2.1.0

### Security Hardening
- **Explicit Safe Browsing enable** in WebViewPolicy (Android O+)
- **Disabled form data saving** and password saving in WebView settings
- **WebView per-provider data isolation** API via `setDataDirectorySuffix()` (Android P+)
- **Application-level method** `MultiAIApp.enableWebViewIsolation()` for safe WebView data siloing

### CI / Build Improvements
- **Added `app` module to GitHub Actions CI** — both Kotlin/Compose and Java modules now build/test in parallel
- **Renamed CI workflow** to "Android CI" with separate jobs for each module
- **Fixed `build.sh` dead code** — `set -euo pipefail` with proper error handling via `if ! cmd` pattern
- **Auto `chmod +x gradlew`** in build script for fresh checkouts
- **Resolved `applicationId` conflict** — `appJava` now uses `com.multaihub.appjava` (both modules can coexist on one device)

### Performance
- **Added Room database indexes** on frequently queried columns:
  - `ai_providers`: `category`, `isFavorite`, `isHidden`, `lastUsed`, `url`, plus composite indexes
  - `tabs`: `providerId`, `lastAccessed`
- **Database schema version bumped to 3** with `MIGRATION_2_3` for zero-downtime upgrade
- **Extracted magic numbers** to named constants in `AppConstants.kt` (input limits, timing, DB limits)

### Code Quality
- **Comprehensive KDoc** added to all public APIs (UrlValidator, WebViewPolicy, WebViewEngine, ViewModels, Repository, DAOs, NetworkMonitor, UserAgent, etc.)
- **Unit tests** for `UrlValidator` (30+ test cases covering valid URLs, scheme rejection, edge cases, parameterized dangerous scheme tests)
- **`// WHY:` comments** preserved and expanded throughout
- **`AppConstants.kt`** centralizes all numeric and string constants

### Project Governance
- **Added MIT LICENSE** file
- **Added `CONTRIBUTING.md`** with development workflow, coding conventions, architecture guidelines
- **Added GitHub issue templates** (bug report, feature request) with structured forms
- **Added GitHub pull request template** with checklist

### Build Configuration
- `appJava` package moved from `com.multaihub.app` → `com.multaihub.appjava`
- `appJava` namespace updated to match new package
- Security improvements also applied to Java module's WebView settings

---

## Version 2.0.0

### Security Fixes
- Removed cleartext traffic permission
- Removed unnecessary storage permissions
- Added comprehensive Proguard rules
- Added input validation with UrlValidator

### New Features
- Tab model and TabDao for tab management
- NetworkMonitor for connectivity tracking
- UserAgent for provider identification
- DefaultAiProviders for pre-configured providers
- WebViewJavaScript for WebView injection
- NotesViewModel for note management
- SettingsViewModel for settings management
- HomeScreen with provider grid
- AiWebViewScreen for AI provider display
- SettingsScreen for app configuration
- NotesScreen for note management
- ComparisonScreen for provider comparison
- AiCard and CategoryChip UI components
- Theme configuration

### Improvements
- Updated all dependencies to latest stable versions
- Enhanced error handling throughout
- Improved performance and memory usage
- Better accessibility support
- Internationalization ready

### Build Updates
- Updated gradle.properties
- Updated build.gradle.kts
- Added proguard-rules.pro

---
Release Date: August 5, 2026
