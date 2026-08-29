# Contributing to MuiltAI

Thank you for your interest in contributing to MuiltAI! This document outlines the
process for contributing code, reporting issues, and submitting pull requests.

## Code of Conduct

By participating in this project, you agree to be respectful and constructive in all
interactions. We welcome contributors of all backgrounds and experience levels.

## How to Contribute

### 1. Report Issues

- Use the GitHub issue tracker
- Choose the appropriate template (bug report or feature request)
- Provide as much detail as possible (device, Android version, steps to reproduce)

### 2. Submit Code Changes

#### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (for the `app` module) or JDK 21 (for the `appJava` module)
- Android SDK with API 34

#### Development Workflow

1. **Fork and clone** the repository
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following the coding conventions below
4. **Verify locally**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ./gradlew :app:lintDebug
   ./gradlew :app:assembleDebug
   ```
5. **Commit** with a clear, descriptive message
6. **Push** to your fork and open a pull request

#### Pull Request Guidelines

- **One change per PR**: Keep pull requests focused on a single feature or fix
- **Describe your changes**: Explain what you changed and why
- **Reference issues**: If your PR fixes an issue, link it in the description
- **Keep it clean**: Squash or rebase your commits if needed to maintain a clean history
- **Pass CI**: Ensure all GitHub Actions checks pass before requesting review

### 3. Coding Conventions

#### Kotlin (`app` module)

- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4-space indentation (configured in `.editorconfig`)
- Maximum line length: 120 characters
- **Document public APIs** with KDoc comments
- Explain *why* for non-obvious design decisions (use `// WHY:` comments)
- **Validate inputs** at trust boundaries (URLs, user input)
- **Handle errors gracefully**: Use `runCatching` in ViewModels; never let persistence
  failures crash the UI coroutine
- Prefer Room queries over in-memory collection filtering for performance

#### Java (`appJava` module)

- Follow standard Java conventions (camelCase, PascalCase for types)
- Keep the module lightweight and dependency-free where possible

#### Security

- Never disable WebView security features without a documented reason
- Always validate URLs before loading them in a WebView
- Never commit secrets, keystores, or credentials
- Use HTTPS for all external URLs

#### Architecture

- **MVVM pattern**: UI in Composables, state in ViewModels, data in Repository
- **Repository pattern**: All persistence goes through `AiRepository`
- **Immutability**: Prefer `data class` + `copy()` for state updates
- **Reactive**: Use `Flow` and `StateFlow` for observable state

### 4. Adding New AI Providers

To add a new built-in AI provider, edit `DefaultAiProviders.kt`:

```kotlin
AiProvider("unique-id", "Display Name", "https://example.com", category = "Chat", sortOrder = N),
```

Guidelines:
- Use a stable, unique `id` (lowercase, no spaces)
- Ensure the URL uses HTTPS
- Choose an appropriate category: Chat, Search, Coding, Writing, Image, or Free
- Place it at a reasonable `sortOrder` position within its category

### 5. Testing

- **Unit tests** go in `app/src/test/` (JVM, no device needed)
- **Instrumentation tests** go in `app/src/androidTest/` (requires device/emulator)
- Test public behavior, not implementation details
- Cover edge cases: empty input, invalid URLs, database failures

### 6. Documentation

- Update the `README.md` for user-facing changes
- Update `CHANGES.md` for significant new features or fixes
- Add KDoc to all new public classes and functions

## Build Commands

```bash
# Build the Kotlin/Compose app
./gradlew :app:assembleDebug

# Build the Java app
./gradlew :appJava:assembleDebug

# Run all checks
./gradlew :app:testDebugUnitTest :app:lintDebug

# Build both modules (requires both JDK 17 and 21)
./build.sh all
```

## Questions?

If you have questions about contributing, feel free to open an issue with the label
"question" or start a discussion.

---

Thank you for contributing to MuiltAI! 🎉
