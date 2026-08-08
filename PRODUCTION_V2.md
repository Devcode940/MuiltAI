# MuiltAI Production V2

## Implemented in this pass

### P1 — Browser engine
- Central WebView security policy.
- HTTPS enforcement for provider loads.
- Dangerous URI scheme rejection.
- Mixed-content blocking.
- File/content access disabled.
- Popup creation disabled.
- Desktop/mobile user-agent switching.
- Renderer crash recovery UI.
- Managed Android downloads.
- Deterministic WebView destruction.
- Browser tab state model and lifecycle controller primitives.
- Back navigation state exposed to the browser UI.

### P1 — AI provider system
- UUID custom-provider IDs.
- Repository-level URL validation.
- Duplicate URL detection through a direct Room query.
- Favorites.
- Recent providers.
- Category filtering.
- Database-backed provider search.
- Custom provider input limits.
- Built-in provider protection during custom-provider deletion.

### P2 — Performance
- Search/filter work moved from Compose collection filtering into Room queries.
- Search input debounced.
- Release R8/resource shrinking enabled.
- Profile Installer added for startup/profile delivery.
- WebView native resources explicitly released.
- Download work delegated to Android DownloadManager.
- Dependency update automation enabled.

### P2 — UI/UX
- Favorites category.
- Recent-provider shortcuts.
- Adaptive provider grid.
- Bounded provider name and URL fields.
- Actionable loading/empty/error states.
- Desktop/mobile indicator.
- Prompt library bottom sheet.
- Comparison-mode entry point.
- Clear-cache action.
- Renderer-crash recovery action.

### P3 — Engineering quality
- Repository persistence boundary.
- Explicit repository exceptions.
- JUnit 5 configuration.
- WebView URL regression tests.
- Room instrumentation-test dependencies.
- EditorConfig.
- Detekt policy configuration.
- Dependabot for Gradle and GitHub Actions.
- GitHub Actions build/test/lint/release pipeline.
- Architecture and deployment documentation.

## Intentionally not faked

Some roadmap items require product-specific integrations and cannot be safely implemented as generic code:

- Provider-specific prompt injection adapters.
- File chooser UX for arbitrary AI websites.
- Provider-specific health checks and authentication flows.
- Cross-provider response extraction.
- Full multi-WebView tab manager UI.
- Full-screen video implementation.
- Android permission prompts for every provider.
- Macrobenchmark/Baseline Profile generation from measured traces.
- Play App Signing credentials and production signing secrets.

These require real provider behavior, device testing, or project credentials. The codebase now exposes the boundaries needed to implement them without weakening the WebView security model.

## Validation

Run locally:

```bash
./gradlew test
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
```

Run Android instrumentation tests on a connected/emulated device:

```bash
./gradlew connectedDebugAndroidTest
```
