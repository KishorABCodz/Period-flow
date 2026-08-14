# PeriodFlow — AI Integration & UI/UX Audit

## Original Problem Statement
Android Kotlin UI/UX expert audit with zero-hallucination protocol on the
existing `KishorABCodz/Period-flow` repo, plus Gemini 3 Flash integration
(AI Health Insights + Symptom Explainer). Then a second round of features:
Streaming, Room cache, AI in PDF, Cycle Chat.

## Architecture (verified, not assumed)
- Kotlin 2.0.0, AGP 8.6.0, compileSdk 36, minSdk 26.
- Jetpack Compose (BOM 2024.05), Material 3, Hilt 2.51.1, Room 2.6.1,
  DataStore, WorkManager, Biometric, Vico, kotlinx-datetime.
- Multi-module: `app`, `core:{common,domain,database,datastore,ui,security,
  health-analysis,network,notifications,export,ai(NEW)}`, `feature:{home,log,
  stats,settings,onboarding,health-insights}`.
- Convention plugins under `build-logic/convention`.

## Implemented (Jan 2026)

### Session 1 — AI baseline + UI audit
- New `core:ai` module (Gemini SDK 0.9.0, `gemini-2.5-flash`).
- `GeminiAiRepository` — insight narrative, symptom explainer.
- `HealthInsightsViewModel` — AI narrative state.
- `LogScreen` — long-press symptom → AI bottom sheet.
- UI/UX fixes: `ClayButton` contrast, emoji removal, Companion contrast,
  `ClayButton` unified export, Onboarding progress dots, `ClayChip` long-press.

### Session 2 — Streaming, cache, PDF, chat
1. **AI Streaming** — `streamInsightNarrative()` and `streamChatReply()`
   return `Flow<AiStreamEvent>`. UI shows tokens live with a blinking caret.
2. **Room cache** — new `ai_insight` table (single-row upsert),
   `AiInsightDao`, `AiInsightCache` domain interface, `AiInsightCacheImpl`.
   Bumped DB version 2→3 (fallbackToDestructiveMigration preserved).
   Health Insights hydrates from cache instantly with a
   "cached · refreshing" hint, then streams fresh text.
3. **AI in PDF** — `ReportExporter.generatePdfReport(cycles, days, analysis, aiNarrative)`.
   Multi-page A4 renderer with word wrap, headings, and an "AI Personal
   Insight" section.
4. **Cycle Chat** — floating Sparkle FAB on Home opens
   `CycleChatSheet` (streaming multi-turn Gemini chat named "Bloom"),
   with cycle-phase context.

## User Setup Required
1. `cp local.properties.example local.properties`
2. Get free key at https://aistudio.google.com/apikey
3. Add `GEMINI_API_KEY=your_key` to `local.properties`
4. Sync Gradle & build

## Backlog / Next Actions
- P1: cache TTL — invalidate the Room narrative if the underlying report
  changes (`riskScore` diff).
- P1: chat history persistence — currently in-memory only.
- P2: rich Markdown rendering for AI answers (bullets, bold).
- P2: Vertex AI Grounded search when medical topics appear in chat.
