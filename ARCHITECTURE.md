# MuiltAI Production Architecture

## Goal

MuiltAI is an Android AI-browser application. The architecture therefore prioritizes Android lifecycle correctness, WebView isolation, local persistence, Compose state stability, and predictable recovery after process death.

## Layers

```text
Compose UI
   |
   v
ViewModels / UI State
   |
   v
Use Cases / Domain Rules
   |
   +-----------------------+
   |                       |
   v                       v
Repository             WebView Core
   |                       |
   v                       v
Room / DataStore       Security / Navigation
```

## WebView boundary

All remote provider content is treated as untrusted input.

The WebView boundary is responsible for:

- Safe URI validation.
- HTTPS enforcement where configured.
- Blocking unsupported URI schemes.
- Mixed-content policy.
- File/content access policy.
- Popup policy.
- Download policy.
- Permission policy.
- Renderer crash recovery.
- Lifecycle cleanup.
- Navigation state reporting.

Do not put arbitrary JavaScript execution into generic UI code. Provider-specific automation must use an explicit adapter and a documented allowlist.

## Persistence

Room stores:

- AI providers.
- Tabs.
- Prompts.
- Notes.

Data access goes through `AiRepository`. UI code must not access DAOs directly.

Data deletion is performed by explicit bulk DAO operations so a clear-all action does not load and iterate over complete tables.

## State management

Long-lived state belongs in ViewModels and repositories. WebView instances remain lifecycle-owned by the UI host and are explicitly destroyed when released.

Avoid doing persistence work from Compose recomposition. User actions call ViewModel methods; ViewModels call repositories from `viewModelScope`.

## Performance rules

- Do filtering in SQL when the dataset can grow.
- Avoid loading complete tables solely to test whether one record exists.
- Use `Flow` for continuously observed Room state.
- Keep WebView creation stable across recompositions.
- Avoid unnecessary reloads.
- Destroy WebViews explicitly.
- Use R8/resource shrinking for release builds.
- Profile startup and WebView memory before optimizing blindly.

## Security rules

- Never expose raw persistence exceptions to users.
- Never log secrets, cookies, tokens, or page contents.
- Treat custom provider URLs as untrusted.
- Keep the repository validation boundary even if the UI already validates input.
- Do not allow WebView file access unless a concrete feature requires it.
- Do not allow mixed content by default.
- Do not trust a URL merely because it came from Room.

## Feature roadmap

### Implemented foundation

- Hardened WebView navigation.
- HTTP/HTTPS navigation policy.
- Mixed-content blocking.
- File/content access restrictions.
- Popup restrictions.
- Repository-level provider URL validation.
- Efficient provider URL lookup.
- Atomic bulk data deletion.
- Safer user-facing errors.
- Desktop/mobile mode synchronization improvements.
- JUnit/MockK/coroutine test infrastructure.
- Android Room test infrastructure.
- Release R8 configuration.
- CI validation and release build documentation.

### Next WebView engine milestones

1. Renderer crash recovery.
2. Download manager.
3. File chooser support.
4. Full-screen media handling.
5. Permission request policy.
6. SSL-error policy.
7. Tab manager with bounded WebView retention.
8. Process-death tab restoration.
9. Find-in-page.
10. Pull-to-refresh.

### AI productivity milestones

1. Prompt templates with variables.
2. Prompt folders and tags.
3. Share-to-AI action.
4. Save selected page text as notes.
5. Provider-specific adapters.
6. Cross-provider comparison.
7. Provider health checks.
8. Versioned provider import/export.

### Quality milestones

1. Baseline Profile.
2. Macrobenchmark.
3. Compose stability audit.
4. Room migration test suite.
5. Accessibility audit.
6. Signed CI releases.
7. Automated GitHub release artifacts.

## Non-goals

MuiltAI should not become a general-purpose unrestricted browser. Features that increase WebView attack surface require an explicit threat model, regression tests, and a safe default.
