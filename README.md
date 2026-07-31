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
| Side-by-side comparison   | 🔜 Scaffold |
| Ask All comparison        | 🔜 Scaffold |

---

## How to Open the Project

1. Unzip the file
2. Open **Android Studio** (Hedgehog or newer recommended)
3. **File → Open** → select the `MultiAIHub` folder
4. Wait for Gradle sync
5. Run on an emulator or real device

---

## Project Structure

```
app/src/main/java/com/multaihub/app/
├── data/
│   ├── model/          → AiProvider, Prompt, Note
│   ├── local/          → Room DAOs + Database
│   └── repository/     → AiRepository
├── ui/
│   ├── home/           → HomeScreen
│   ├── webview/        → AiWebViewScreen
│   ├── components/     → AiCard, CategoryChip
│   └── theme/          → Material 3 theme
├── viewmodel/          → HomeViewModel, WebViewViewModel
├── utils/              → DefaultAiProviders, UserAgent
├── MainActivity.kt
└── MultiAIApp.kt
```

---

## Key Technical Details

### User-Agent Spoofing
```kotlin
// Mobile
Mozilla/5.0 (Linux; Android 14; Pixel 8) ... Chrome/126.0.0.0 Mobile Safari/537.36

// Desktop
Mozilla/5.0 (Windows NT 10.0; Win64; x64) ... Chrome/126.0.0.0 Safari/537.36
```

### Adding more AIs
Edit `DefaultAiProviders.kt` or use the **+** button inside the app.

---

## Next Steps You Can Add

1. Full Side-by-Side comparison screen
2. Ask All (multi-send) mode
3. Better prompt injection into WebView via JavaScript
4. Notes screen
5. Settings screen (clear all data, force dark, etc.)
6. Tab system for multiple open AIs

---

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- minSdk 26
- targetSdk 34
- JDK 17

Enjoy building!
```
