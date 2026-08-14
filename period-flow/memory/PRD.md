# PeriodFlow — AI Integration Backlog

## Sessions summary

### Session 1 — Baseline
`core:ai` module + Gemini 2.5 Flash + insight/symptom features + UI/UX audit.

### Session 2 — Streaming, cache, PDF, chat
Token streaming; Room `ai_insight` cache; AI-in-PDF; Cycle Chat sheet.

### Session 3 — Chat history, cache invalidation, voice input
Room `chat_message`; risk-score-based invalidation; `SpeechRecognizer` mic.

### Session 4 — Voice companion, nightly, symptom-aware, data mask
Data masker (regex + bucketing); `RecentLogSummary` + use case; `NightlyInsightWorker`;
`FastLlmProvider` interface + `HeuristicFastProvider` (rule-based); TTS wrapper;
`VoiceCompanionOrchestrator` (fast+slow parallel).

### Session 5 — Gemma, download UX, voice preference, migrations, emotions
- **Gemma-2 2B (on-device)**: MediaPipe LLM Inference (`com.google.mediapipe:tasks-genai:0.10.16`).
  - `GemmaModelManager` — file presence + configured URL from `local.properties`.
  - `MediaPipeGemmaFastProvider` — lazy init, `LlmInference` engine, silent
    fallback on failure.
  - `CompositeFastProvider` — Gemma when ready, heuristic otherwise. Hilt binding
    updated in `AiBindingsModule`.
- **Model Download UX**: `ModelDownloader` wraps Android `DownloadManager`;
  progress banner in the chat sheet (running/success/failed with dismiss).
  Uses `GEMMA_MODEL_URL` from `local.properties`.
- **Voice preference persistence**: `UserPreferences.isVoiceModeEnabled` +
  `setVoiceModeEnabled`. `CycleChatViewModel` exposes `voiceModeEnabled: StateFlow<Boolean>`
  bound to DataStore. Chat sheet uses the persisted value; auto-triggers model
  download on first-time enable.
- **Real migrations**: `PeriodFlowMigrations` with `MIGRATION_2_3` (ai_insight)
  and `MIGRATION_3_4` (chat_message). `fallbackToDestructiveMigration()`
  replaced by `addMigrations(*ALL)`.
- **Bloom emotions**: `BloomEmotion` enum (NEUTRAL/WARM/THINKING/EXCITED/GENTLE/CONCERNED)
  with pitch, rate, and glyph. `EmotiveUtterance` return type on fast provider.
  `VoiceCompanionEvent` gains an `emotion` field. `BloomTts.speakEmotive`
  maps emotion → pitch/rate. Chat header shows the current emotion glyph;
  ViewModel exposes `currentEmotion: StateFlow<BloomEmotion>`.

## Setup
1. `cp local.properties.example local.properties`
2. `GEMINI_API_KEY=your_key`
3. Optional: `GEMMA_MODEL_URL=<direct .bin URL>` to enable the on-device fast provider.
4. `./gradlew assembleDebug`

## Backlog
- Move MediaPipe engine ownership into a Hilt-scoped singleton with proper
  lifecycle (release on Application.onTerminate / low memory).
- Markdown rendering for AI answers.
- Persist emotion history to visualise Bloom's arc across a chat.
