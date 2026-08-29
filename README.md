# MultiAI Hub

A modern Android multi-AI browser hub built with **Kotlin + Jetpack Compose**.

Similar to OmniAI, but with extra features:

- 30+ AI platforms loaded via WebView (no official APIs)
- Mobile / Desktop site toggle (with proper User-Agent spoofing)
- Custom AI support (add any website)
- Categories + Search + Favorites + Recent
- Prompt Library (basic)
- Clean Material 3 UI
- Strong session persistence (cookies + DOM storage)
- **Hardened WebView security** (Safe Browsing, mixed-content blocking, dangerous scheme rejection)

---

## Features Included

| Feature                    | Status      |
|---------------------------|-------------|
| 30+ built-in AIs          | ✅          |
| WebView with custom UA    | ✅          |
| Mobile ↔ Desktop toggle   | ✅          |
| Add Custom AI             | ✅          |
| Categories & Search       | ✅          |
| Favorites                 | ✅          |
| Recent AIs                | ✅          |
| Prompt Library (basic)    | ✅          |
| Room Database             | ✅          |
| Material 3 + Dark mode    | ✅          |
| Safe Browsing enabled     | ✅          |
| Side-by-side comparison   | 🔜 Scaffold |
| Ask All comparison        | 🔜 Scaffold |

---

## Modules

| Module | Description | Java | compileSdk |
|--------|-------------|------|------------|
| `app` | Main product: Kotlin + Jetpack Compose, full feature set | 17 | 34 |
| `appJava` | Lightweight Java-only alternative (no Compose) | 21 | 35 |

Both modules can be installed on the same device simultaneously (different application IDs).

---

## How to Open the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/Devcode940/MuiltAI.git
   ```
2. Open **Android Studio** (Hedgehog or newer recommended)
3. **File → Open** → select the `MuiltAI` folder
4. Wait for Gradle sync
5. Run on an emulator or real device

---

## Building from Command Line

```bash
# Build only the Kotlin/Compose app (requires JDK 17)
./build.sh app

# Build only the Java app (requires JDK 21)
./build.sh appJava

# Build both modules
./build.sh all
```

---

## Project Structure

```
app/src/main/java/com/multaihub/app/
├── data/
│   ├── model/          → AiProvider, Prompt, Note, Tab
│   ├── local/          → Room DAOs + Database + Migrations
│   └── repository/     → AiRepository (single data boundary)
├── ui/
│   ├── home/           → HomeScreen
│   ├── webview/        → AiWebViewScreen
│   ├── components/     → AiCard, CategoryChip
│   └── theme/          → Material 3 theme
├── viewmodel/          → HomeViewModel, WebViewViewModel, NotesViewModel, SettingsViewModel
├── webview/            → WebViewPolicy, WebViewEngine, WebViewDownloadHandler, WebViewTabState
├── utils/              → AppConstants, UrlValidator, UserAgent, NetworkMonitor, DefaultAiProviders
├── MainActivity.kt     → Navigation host
└── MultiAIApp.kt       → Application entry point
```

---

## Key Technical Details

### WebView Security Model

The app takes a defense-in-depth approach to WebView security:

- **HTTPS enforcement** — all URLs are validated and upgraded to HTTPS
- **Mixed content blocked** — `MIXED_CONTENT_NEVER_ALLOW`
- **Dangerous schemes rejected** — `javascript:`, `file:`, `content:`, `data:`, `intent:` all blocked
- **File access disabled** — `allowFileAccess = false`, `allowContentAccess = false`
- **Popups disabled** — no window creation or JS popup opening
- **Safe Browsing enabled** — Google Safe Browsing protection (Android O+)
- **Form data & password saving disabled**
- **Per-provider data isolation API** — `WebView.setDataDirectorySuffix()` (Android P+)

### User-Agent Spoofing

```kotlin
// Mobile
Mozilla/5.0 (Linux; Android 14; Mobile) ... Chrome/126.0.0.0 Mobile Safari/537.36

// Desktop
Mozilla/5.0 (Windows NT 10.0; Win64; x64) ... Chrome/126.0.0.0 Safari/537.36
```

### Adding more AIs

Edit `DefaultAiProviders.kt` or use the **+** button inside the app. All custom provider
URLs are validated, normalized, and upgraded to HTTPS before persistence.

### Database Performance

Room indexes are applied to frequently queried columns (`category`, `isFavorite`,
`isHidden`, `lastUsed`, `url`, plus composite indexes) to keep catalog queries responsive
as the database grows. Schema version 3 includes a seamless migration that adds these
indexes without data loss.

---

## Next Steps You Can Add

1. Full Side-by-Side comparison screen
2. Ask All (multi-send) mode
3. Better prompt injection into WebView via JavaScript
4. Enhanced Notes screen with search
5. Full Settings screen (clear all data, force dark, etc.)
6. Tab system UI for multiple open AIs
7. Baseline profile generation for cold-start optimization
8. Hilt dependency injection

---

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- minSdk 26
- targetSdk 34
- JDK 17 (for `app` module)
- JDK 21 (for `appJava` module)

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on
our development workflow, coding conventions, and how to submit pull requests.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

Enjoy building! 🚀
