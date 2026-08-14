package com.periodflow.core.ai.repository

import com.periodflow.core.ai.client.GeminiClient
import com.periodflow.core.ai.model.AiResult
import com.periodflow.core.ai.model.AiStreamEvent
import com.periodflow.core.ai.privacy.DataMasker
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.model.RecentLogSummary
import com.periodflow.core.domain.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI repository powered by Gemini.
 *
 * Zero-hallucination guardrails:
 * - System prompt strictly forbids medical diagnoses and unsupported claims.
 * - Empty API key short-circuits with a clear Error state (no fake output).
 * - All calls run on IO dispatcher; errors are wrapped, never thrown to UI.
 * - Streaming and non-streaming variants are both offered; UIs prefer streaming.
 */
@Singleton
class GeminiAiRepository @Inject constructor() {

    private val model by lazy { GeminiClient.create() }

    private val insightSystemPrompt = """
        You are a supportive wellness companion inside a private period-tracking app.
        Rules:
        - Never diagnose conditions. Never claim medical certainty.
        - Speak warmly, in second person ("you"), 3 short paragraphs max.
        - Ground every observation in the data provided. If data is thin, say so.
        - End with 1-line gentle encouragement to consult a clinician for concerns.
    """.trimIndent()

    private val symptomSystemPrompt = """
        You are a calm wellness educator inside a period-tracking app.
        Explain the symptom in 4-6 short sentences using plain language.
        Cover: what it commonly feels like, why it can happen around the cycle phase provided,
        and 2 practical self-care ideas. Never diagnose. End with:
        "Talk to a healthcare professional if it persists or worsens."
    """.trimIndent()

    private val chatSystemPrompt = """
        You are Bloom, a warm wellness companion inside a period-tracking app.
        Rules:
        - Never diagnose. Never invent facts. Ask a clarifying question if unsure.
        - Keep answers concise: 3-6 sentences.
        - Use the cycle context provided when relevant.
        - When symptoms sound serious, gently suggest consulting a clinician.
    """.trimIndent()

    // ---------- Insight narrative ----------

    private fun buildInsightPrompt(
        report: HealthAnalysisReport,
        averageCycleLength: Int,
        averagePeriodLength: Int,
        totalCycles: Int,
    ): String = buildString {
        appendLine(insightSystemPrompt)
        appendLine()
        appendLine("USER DATA:")
        appendLine("- Cycles analyzed: ${report.cyclesAnalyzed}")
        appendLine("- Average cycle length: $averageCycleLength days")
        appendLine("- Average period length: $averagePeriodLength days")
        appendLine("- Total cycles tracked: $totalCycles")
        appendLine("- Overall risk score: ${report.riskScore}/100 (${report.riskLevel.displayName})")
        appendLine("- Key indicators:")
        report.indicators.forEach { i ->
            appendLine("    • ${i.name} (score ${i.score}): ${i.description}. Data: ${i.dataPoints}")
        }
        appendLine()
        appendLine("Write a personalised, warm insight narrative now.")
    }

    /** Non-streaming variant (used by tests / PDF export). */
    suspend fun generateInsightNarrative(
        report: HealthAnalysisReport,
        averageCycleLength: Int,
        averagePeriodLength: Int,
        totalCycles: Int,
    ): AiResult<String> {
        if (!GeminiClient.isConfigured) {
            return AiResult.Error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties.")
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                val prompt = buildInsightPrompt(report, averageCycleLength, averagePeriodLength, totalCycles)
                val response = model.generateContent(prompt)
                val text = response.text?.trim().orEmpty()
                if (text.isBlank()) AiResult.Error("Empty response from Gemini.")
                else AiResult.Success(text)
            }.getOrElse { AiResult.Error(it.message ?: "Gemini request failed.") }
        }
    }

    /** Streaming variant — emits deltas as tokens arrive, then Done or Error. */
    fun streamInsightNarrative(
        report: HealthAnalysisReport,
        averageCycleLength: Int,
        averagePeriodLength: Int,
        totalCycles: Int,
    ): Flow<AiStreamEvent> = flow {
        if (!GeminiClient.isConfigured) {
            emit(AiStreamEvent.Error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties."))
            return@flow
        }
        val prompt = buildInsightPrompt(report, averageCycleLength, averagePeriodLength, totalCycles)
        val upstream = model.generateContentStream(prompt)
            .map { chunk -> AiStreamEvent.Delta(chunk.text.orEmpty()) as AiStreamEvent }
            .catch { emit(AiStreamEvent.Error(it.message ?: "Gemini stream failed.")) }
            .onCompletion { cause -> if (cause == null) emit(AiStreamEvent.Done) }
        emitAll(upstream)
    }.flowOn(Dispatchers.IO)

    // ---------- Symptom explainer ----------

    suspend fun explainSymptom(
        symptom: Symptom,
        phase: CyclePhase?,
    ): AiResult<String> {
        if (!GeminiClient.isConfigured) {
            return AiResult.Error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties.")
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                val prompt = buildString {
                    appendLine(symptomSystemPrompt)
                    appendLine()
                    appendLine("Symptom: ${symptom.displayName}")
                    appendLine("Current cycle phase: ${phase?.name ?: "Unknown"}")
                }
                val response = model.generateContent(prompt)
                val text = response.text?.trim().orEmpty()
                if (text.isBlank()) AiResult.Error("Empty response from Gemini.")
                else AiResult.Success(text)
            }.getOrElse { AiResult.Error(it.message ?: "Gemini request failed.") }
        }
    }

    // ---------- Cycle chat ----------

    /**
     * Streaming multi-turn chat about the user's cycle.
     * `history` is a list of alternating user/assistant messages (chronological).
     * `recent` is an anonymised summary of the last few days of logs.
     * All free-text user messages are run through [DataMasker] before leaving the device.
     */
    fun streamChatReply(
        history: List<ChatTurn>,
        cyclePhase: CyclePhase?,
        cycleDayNumber: Int,
        recent: RecentLogSummary? = null,
    ): Flow<AiStreamEvent> = flow {
        if (!GeminiClient.isConfigured) {
            emit(AiStreamEvent.Error("Gemini API key not configured. Add GEMINI_API_KEY to local.properties."))
            return@flow
        }
        val prompt = buildString {
            appendLine(chatSystemPrompt)
            appendLine()
            appendLine("CURRENT CYCLE CONTEXT:")
            appendLine("- Phase: ${cyclePhase?.name ?: "Unknown"}")
            appendLine("- Day in cycle: $cycleDayNumber")
            if (recent != null && recent.daysLogged > 0) {
                appendLine()
                appendLine("RECENT LOG SNAPSHOT (last ${recent.windowDays} days, ${recent.daysLogged} logged):")
                if (recent.topSymptoms.isNotEmpty()) {
                    appendLine("- Top symptoms: ${recent.topSymptoms.joinToString { it.displayName }}")
                }
                recent.dominantMood?.let { appendLine("- Dominant mood: ${it.displayName}") }
                recent.strongestFlow?.let { appendLine("- Strongest flow observed: ${it.displayName}") }
                if (recent.notesDigest.isNotBlank()) {
                    appendLine("- Notes digest: ${DataMasker.redactUserText(recent.notesDigest)}")
                }
            }
            appendLine()
            appendLine("CONVERSATION SO FAR:")
            history.forEach { turn ->
                val speaker = if (turn.isUser) "User" else "Bloom"
                val safeText = if (turn.isUser) DataMasker.redactUserText(turn.text) else turn.text
                appendLine("$speaker: $safeText")
            }
            appendLine()
            appendLine("Reply as Bloom now.")
        }
        val upstream = model.generateContentStream(prompt)
            .map { chunk -> AiStreamEvent.Delta(chunk.text.orEmpty()) as AiStreamEvent }
            .catch { emit(AiStreamEvent.Error(it.message ?: "Gemini stream failed.")) }
            .onCompletion { cause -> if (cause == null) emit(AiStreamEvent.Done) }
        emitAll(upstream)
    }.flowOn(Dispatchers.IO)
}

/** Chat turn used by both the ViewModel and the repository prompt builder. */
data class ChatTurn(val isUser: Boolean, val text: String)
