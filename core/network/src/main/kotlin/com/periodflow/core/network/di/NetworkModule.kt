package com.periodflow.core.network.di

import com.periodflow.core.network.api.DietRecommendationResponse
import com.periodflow.core.network.api.OpenNutritionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOpenNutritionApi(): OpenNutritionApi {
        // Return a mock implementation until real backend is configured
        return object : OpenNutritionApi {
            override suspend fun getDietRecommendation(
                symptoms: List<String>,
                cycleDay: Int,
                phase: String
            ): DietRecommendationResponse {
                delay(500) // Simulate network delay
                
                val mockTip = when (phase) {
                    "MENSTRUAL" -> "Focus on iron-rich foods like spinach, lentils, and red meat. (Mocked via OpenNutrition)"
                    "FOLLICULAR" -> "Incorporate lean proteins, complex carbs, and fermented foods. (Mocked via OpenNutrition)"
                    "OVULATION" -> "Eat anti-inflammatory foods like berries, nuts, and leafy greens. (Mocked via OpenNutrition)"
                    "LUTEAL" -> "Focus on complex carbs and magnesium to ease PMS. (Mocked via OpenNutrition)"
                    else -> "Maintain a balanced diet of whole foods, plenty of water, and regular meals. (Mocked via OpenNutrition)"
                }
                
                return DietRecommendationResponse(
                    tip = mockTip,
                    recommendedNutrients = listOf("Iron", "Magnesium"),
                    foodsToAvoid = listOf("High sugar", "Processed foods")
                )
            }
        }
    }
}
