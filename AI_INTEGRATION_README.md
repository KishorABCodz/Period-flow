# PeriodFlow — Gemini AI Integration & UI/UX Audit

This document describes the AI features and UI/UX fixes added on top of the
original PeriodFlow codebase.

## Feature summary

### AI Health Insights (streaming + cached)
- Personalised narrative generated from your cycle data via Gemini.
- **Streaming** — tokens appear word by word (`streamInsightNarrative`).
- **Cached in Room** — the latest narrative is persisted in table `ai_insight`.
  On reopen, the cached text is shown instantly with a subtle
  "cached · refreshing" badge while a fresh version streams in.
- **Included in PDF export** — the AI narrative is rendered inside the
  exported PDF right after the deterministic recommendations.

### Symptom Explainer
Long-press any symptom chip in the Log screen to open a `ModalBottomSheet`
with a Gemini-generated, phase-aware explanation.

### Cycle Chat (Bloom)
A small secondary floating button on the Home screen opens a chat sheet
where users can ask Gemini follow-up questions about their cycle. Streaming,
multi-turn, **persisted in Room** (`chat_message` table) so history survives
process death and app restarts. Header has a "Clear chat" action.

### Voice Input
Tap the mic button in the composer to dictate a message — Android's
platform `SpeechRecognizer` streams partial transcripts into the draft box.
Requires `RECORD_AUDIO` (requested at runtime). No third-party speech
dependency.

## Architecture

### `core/ai` module (new)
- **`GeminiClient`** — thin wrapper around Google's official Kotlin SDK
  (`com.google.ai.client.generativeai:generativeai:0.9.0`).
- **`GeminiAiRepository`** — public repository with these functions:
  - `streamInsightNarrative(...)` → `Flow<AiStreamEvent>`
  - `generateInsightNarrative(...)` (non-streaming variant, still exposed)
  - `explainSymptom(symptom, phase)` (non-streaming, small payload)
  - `streamChatReply(history, cyclePhase, cycleDayNumber)` → `Flow<AiStreamEvent>`
- **`AiResult<T>`** — sealed state (`Idle` / `Loading` / `Success` / `Error`).
- **`AiStreamEvent`** — sealed stream event (`Delta` / `Done` / `Error`).
- **`ChatTurn(isUser, text)`** — shared chat data class.
- **`AiModule` (Hilt)** — provides a singleton repository.

Model: `gemini-2.5-flash` (current SDK-supported "Gemini Flash" family;
change `MODEL_NAME` in `GeminiClient.kt` when Google ships
`gemini-3-flash-preview` in the Kotlin SDK).

### Room cache (`ai_insight` table + `chat_message` table)
- `AiInsightEntity` — single-row singleton (fixed id=1) with
  `basedOnRiskScore` for cache invalidation.
- `AiInsightDao` — `observe()`, `getOnce()`, `upsert()`, `clear()`.
- `AiInsightCache` — save takes `(narrative, basedOnRiskScore)`; the ViewModel
  clears the cache automatically when the deterministic risk score changes.
- `ChatMessageEntity` — auto-generated id, chronological order.
- `ChatMessageDao` — `observeAll()`, `insert()`, `update()`, `clear()`.
- `ChatHistoryRepository` — domain interface with `observeMessages()`,
  `addMessage()`, `updateMessage()`, `clear()`.
- Database version bumped `2 → 3 → 4`. `fallbackToDestructiveMigration()`
  is preserved so upgrades won't crash (data is wiped — fine for a WIP app;
  swap in real `Migration` classes before release).

### Guardrails (zero-hallucination protocol)
- System prompts explicitly forbid diagnosis and require grounding in the
  provided data.
- Empty API key short-circuits with a clear `Error` state — the UI never
  invents content.
- Safety settings block medium-and-above HARM categories.
- All calls run on `Dispatchers.IO`; errors are wrapped, never thrown.
- Streaming falls back to cached text if the connection drops mid-stream.

## UI/UX audit fixes (initial pass)

| # | Issue                                                             | Fix                                                                                |
|---|-------------------------------------------------------------------|------------------------------------------------------------------------------------|
| 1 | `ClayButton` always used `onPrimary` → invisible `-`/`+` steppers | Luminance-aware `contentColorFor(background)` in `Claymorphism.kt`                 |
| 2 | Emojis in Settings status pills & section title                   | Replaced with `Canvas` `StatusDot` and plain text                                  |
| 3 | Emoji in Risk badge (`🟢🟡🟠🔴`)                                    | New `RiskBadge` composable with `Canvas`-drawn dot in themed colour                |
| 4 | Home Companion card had low-contrast `secondary.copy(alpha=0.1f)` | Switched to `secondaryContainer`/`onSecondaryContainer` per Material 3             |
| 5 | Health Insights used Material `Button` wrapped in claymorphism    | Replaced with `ClayButton` for consistent press animation & shape                  |
| 6 | Onboarding "Step X of 3" plain text                               | Animated pill-shaped progress dots (12dp → 32dp for active)                        |
| 7 | Log screen offered no way to learn about a symptom                | Long-press → Gemini-powered bottom-sheet explainer                                 |

## Setup

1. Copy `local.properties.example` to `local.properties`.
2. Get a free Gemini API key from https://aistudio.google.com/apikey.
3. Add the line: `GEMINI_API_KEY=your_key_here`.
4. Sync Gradle & run the app.

If `GEMINI_API_KEY` is empty the AI cards will show a clear "not configured"
error message — the rest of the app is unaffected.

## File map (new / modified)

New:
- `core/ai/**`
- `core/database/src/main/kotlin/com/periodflow/core/database/entity/AiInsightEntity.kt`
- `core/database/src/main/kotlin/com/periodflow/core/database/dao/AiInsightDao.kt`
- `core/database/src/main/kotlin/com/periodflow/core/database/repository/AiInsightCacheImpl.kt`
- `core/domain/src/main/kotlin/com/periodflow/core/domain/repository/AiInsightCache.kt`
- `feature/home/src/main/kotlin/com/periodflow/feature/home/chat/CycleChatViewModel.kt`
- `feature/home/src/main/kotlin/com/periodflow/feature/home/chat/CycleChatSheet.kt`
- `local.properties.example`
- `AI_INTEGRATION_README.md` (this file)

Modified:
- `settings.gradle.kts` — includes `:core:ai`
- `gradle/libs.versions.toml` — Gemini SDK + coroutines aliases
- `core/ui/.../Claymorphism.kt` — luminance-aware content color, long-press chip
- `core/ui/.../SymptomChipGrid.kt` — optional `onExplain` parameter
- `core/database/.../PeriodFlowDatabase.kt` — v3, `aiInsightDao()`
- `core/database/di/DatabaseModule.kt` — provides `AiInsightDao`
- `core/database/di/RepositoryModule.kt` — binds `AiInsightCache`
- `core/domain/.../ReportExporter.kt` — new `aiNarrative` parameter
- `core/export/.../PdfReportGenerator.kt` — multi-page, wrapped text, AI section
- `feature/health-insights/build.gradle.kts` — depends on `:core:ai`
- `feature/health-insights/.../HealthInsightsViewModel.kt` — streaming + cache + PDF
- `feature/health-insights/.../HealthInsightsScreen.kt` — streaming caret + cached hint
- `feature/log/build.gradle.kts` — depends on `:core:ai`
- `feature/log/.../LogViewModel.kt` — explainer state + `openSymptomExplainer`
- `feature/log/.../LogScreen.kt` — bottom sheet + long-press hint
- `feature/home/build.gradle.kts` — depends on `:core:ai`
- `feature/home/.../HomeScreen.kt` — Companion contrast fix + Cycle Chat FAB
- `feature/settings/.../SettingsScreen.kt` — emoji removed, `StatusDot`
- `feature/onboarding/.../OnboardingScreen.kt` — progress dots
