package com.periodflow.core.health_analysis

import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.model.RiskLevel
import com.periodflow.core.network.api.OpenNutritionApi
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DietRecommender @Inject constructor(
    private val openNutritionApi: OpenNutritionApi
) {

    fun getDailyDietTip(phase: CyclePhase, pcosRisk: RiskLevel? = null): String {
        // Fallback or cached tip strategy
        if (pcosRisk == RiskLevel.HIGH || pcosRisk == RiskLevel.ELEVATED) {
            return getPcosDietTip(phase)
        }
        
        return when (phase) {
            CyclePhase.MENSTRUAL -> "Focus on iron-rich foods like spinach, lentils, and red meat to replenish iron lost during your period. Hydrate well!"
            CyclePhase.FOLLICULAR -> "Incorporate lean proteins, complex carbs, and fermented foods to support rising estrogen and energy levels."
            CyclePhase.OVULATION -> "Eat anti-inflammatory foods like berries, nuts, and leafy greens to support ovulation and liver health."
            CyclePhase.LUTEAL -> "Cravings are normal! Focus on complex carbs (sweet potatoes) and magnesium (dark chocolate, pumpkin seeds) to ease PMS."
            CyclePhase.UNKNOWN -> "Maintain a balanced diet of whole foods, plenty of water, and regular meals to support hormonal health."
        }
    }

    suspend fun fetchDynamicDietTip(
        symptoms: List<String>,
        cycleDay: Int,
        phase: CyclePhase
    ): String = withContext(Dispatchers.IO) {
        try {
            val response = openNutritionApi.getDietRecommendation(
                symptoms = symptoms,
                cycleDay = cycleDay,
                phase = phase.name
            )
            response.tip
        } catch (e: Exception) {
            // Fallback to static tips if network fails
            getDailyDietTip(phase)
        }
    }

    private fun getPcosDietTip(phase: CyclePhase): String {
        return when (phase) {
            CyclePhase.MENSTRUAL -> "PCOS Tip: Focus on anti-inflammatory, iron-rich foods. Avoid highly processed sugary foods to help manage insulin."
            CyclePhase.FOLLICULAR -> "PCOS Tip: Support insulin sensitivity with high-fiber foods like broccoli, oats, and chia seeds."
            CyclePhase.OVULATION -> "PCOS Tip: A low-glycemic index diet with healthy fats (avocado, olive oil) can help regulate hormones during this window."
            CyclePhase.LUTEAL -> "PCOS Tip: Combat cravings and potential insulin resistance by pairing complex carbs with protein or fat (e.g., apple with almond butter)."
            CyclePhase.UNKNOWN -> "PCOS Tip: Focus on a low-GI, anti-inflammatory diet to support insulin sensitivity and hormonal balance."
        }
    }
}
