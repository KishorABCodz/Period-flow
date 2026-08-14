# PeriodFlow — AI Integration Backlog

## Sessions summary

### Session 1 — Baseline
- `core:ai` module with Gemini 2.5 Flash, insight narrative + symptom explainer.
- Full UI/UX audit fixes.

### Session 2 — Streaming, cache, PDF, chat
- Token streaming; Room `ai_insight` cache; AI-in-PDF; Cycle Chat sheet.

### Session 3 — Chat history, cache invalidation, voice input
- Room `chat_message` table; risk-score-based cache invalidation;
  `VoiceInputController` (SpeechRecognizer).

### Session 4 — Voice companion, nightly refresh, symptom-aware chat
- **Data masker** (`core/ai/privacy/DataMasker.kt`) — regex-scrubs
  emails, URLs, dates, long numbers; buckets weight/height/cycle-day into
  fuzzy bands. Applied to every prompt egress in `GeminiAiRepository`.
- **Symptom-aware chat** — new `RecentLogSummary` domain model +
  `GetRecentLogSummaryUseCase` aggregates the last 7 days into top-3
  symptoms, dominant mood, strongest flow and a 200-char notes digest.
  `CycleChatViewModel` fetches it before every reply and injects it into
  the prompt (still masked).
- **Nightly Insight** — `NightlyInsightWorker` (`@HiltWorker`,
  `CoroutineWorker`) refreshes the cached AI insight at ~03:00 local time
  under `Connected + battery-not-low` constraints. Scheduled once from
  `PeriodFlowApplication.onCreate()` via `NightlyInsightScheduler`.
  App implements `Configuration.Provider` with a `HiltWorkerFactory`.
- **Voice companion scaffold** — `FastLlmProvider` interface,
  `HeuristicFastProvider` default (no ML, rule-based greetings & thinking
  fillers), `VoiceCompanionOrchestrator` (parallel fast + slow legs via
  `channelFlow`), `BloomTts` (Android built-in TextToSpeech with two
  cadences: faster/warmer for fillers, calm for grounded answer).
  Chat sheet gains a `VolumeOff/RecordVoiceOver` toggle to enable voice
  mode; Send button routes through `sendVoiceMessage()` when active.

## Voice companion extension points

The default fast provider is heuristic-only (zero risk, zero hallucination).
Two documented upgrade paths live in the header of `FastLlmProvider.kt`:
- **MediaPipe LLM Inference** (Google, Android-native) with Gemma-2 2B.
- **MLC-LLM Qwen 2.5 0.5B/1.5B** (Android).

Either requires:
1. Adding the SDK dependency to `core:ai`.
2. Implementing `FastLlmProvider` and swapping the `AiBindingsModule` binding.
3. Downloading the model file at first run (~1-1.4 GB).

## Setup
1. `cp local.properties.example local.properties`
2. Paste `GEMINI_API_KEY=...` (https://aistudio.google.com/apikey)
3. Sync Gradle & build (`./gradlew assembleDebug`)

## Backlog
- Ship a Migration(3, 4) instead of destructive fallback before release.
- Bundle-download flow for on-device fast LLM.
- Markdown rendering for AI answers.
- Persist voice-mode preference per user.
