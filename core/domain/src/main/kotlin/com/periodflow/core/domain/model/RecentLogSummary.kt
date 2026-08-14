package com.periodflow.core.domain.model

/**
 * Aggregated snapshot of the user's recent logs used to give LLMs
 * relevant, non-identifiable context. Populated by GetRecentLogSummaryUseCase.
 */
data class RecentLogSummary(
    /** Number of days looked at (typically 7). */
    val windowDays: Int,
    /** How many of those days had any log entry. */
    val daysLogged: Int,
    /** Top-3 most frequent symptoms in the window (empty when nothing logged). */
    val topSymptoms: List<Symptom>,
    /** Most frequent mood in the window, or null. */
    val dominantMood: Mood?,
    /** Highest recent flow intensity in the window, or null. */
    val strongestFlow: FlowIntensity?,
    /** Short natural-language digest of the notes (concatenated, truncated). */
    val notesDigest: String,
)
