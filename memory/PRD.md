# PeriodFlow — AI Integration & UI/UX Audit

## Sessions implemented

### Session 1 — AI baseline + UI audit
- New `core:ai` module (Gemini SDK 0.9.0, `gemini-2.5-flash`).
- `GeminiAiRepository` — insight narrative, symptom explainer.
- `HealthInsightsViewModel` — AI narrative state.
- `LogScreen` — long-press symptom → AI bottom sheet.
- UI/UX fixes: `ClayButton` contrast, emoji removal, Companion contrast,
  `ClayButton` unified export, Onboarding progress dots, `ClayChip` long-press.

### Session 2 — Streaming, cache, PDF, chat
- Token streaming for insight + chat.
- Room-cached insight (`ai_insight`, single-row upsert).
- AI narrative rendered inside the exported PDF.
- Cycle Chat (Bloom) bottom sheet from Home FAB.

### Session 3 — Chat history, cache invalidation, voice input
- **Chat History** — new `chat_message` Room table; `ChatHistoryRepository`
  interface + impl; `CycleChatViewModel` loads history on init, persists user
  and assistant messages; Bloom greeting only shown on empty history; header
  "Clear chat" icon calls `ChatHistoryRepository.clear()`.
- **Cache Invalidation** — `AiInsightEntity` now stores `basedOnRiskScore`;
  `HealthInsightsViewModel.analyzeHealth()` compares the fresh
  `report.riskScore` to the cached score and clears the cache before
  streaming a new narrative. `save()` records the current score.
- **Voice Input** — `VoiceInputController` wraps Android's
  `SpeechRecognizer`; a mic button in the chat composer requests
  `RECORD_AUDIO` at runtime, streams partial results into the draft box, and
  swaps to a red "Stop" state while listening. Manifest updated with
  permission + `<queries>` block for the RecognitionService.
- Database schema bumped `3 → 4` (existing `fallbackToDestructiveMigration`
  handles the upgrade).

## Setup
1. `cp local.properties.example local.properties`
2. Add `GEMINI_API_KEY=your_key` (https://aistudio.google.com/apikey).
3. Sync Gradle & build.

## Backlog
- P1: proper Room migrations (currently destructive fallback wipes DB on
  upgrade). Add `Migration(3, 4)` when the app has real users.
- P1: Markdown rendering for AI replies (bold, bullets).
- P2: Attach recent symptoms/mood to `sendMessage` context for more relevant chat.
- P2: On-device Whisper fallback if platform speech unavailable.
