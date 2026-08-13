package com.periodflow.core.network.api

/**
 * Interface representing an Open Source Nutrition API (e.g., Open Food Facts).
 * Provides endpoints for retrieving nutritional info and diet suggestions.
 */
interface OpenNutritionApi {
    /**
     * Fetches diet recommendations based on symptoms, cycle day, and phase.
     * Uses open-source data to provide nutritional tips.
     */
    suspend fun getDietRecommendation(
        symptoms: List<String>,
        cycleDay: Int,
        phase: String
    ): DietRecommendationResponse
}

data class DietRecommendationResponse(
    val tip: String,
    val recommendedNutrients: List<String>,
    val foodsToAvoid: List<String>
)
