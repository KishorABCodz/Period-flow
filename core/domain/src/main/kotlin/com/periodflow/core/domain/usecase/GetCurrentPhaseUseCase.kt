package com.periodflow.core.domain.usecase

import com.periodflow.core.common.today
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.repository.CycleRepository
import com.periodflow.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetCurrentPhaseUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val prefsRepository: UserPreferencesRepository,
) {
    /**
     * Determines the current cycle phase based on the day within the cycle.
     *
     * Phase determination:
     * - Day 1 to periodLength → MENSTRUAL
     * - Day (periodLength+1) to (ovulationDay-5) → FOLLICULAR
     * - Day (ovulationDay-5) to (ovulationDay+1) → OVULATION
     * - Day (ovulationDay+2) to cycleLength → LUTEAL
     *
     * Where ovulationDay = cycleLength - 14 (standard luteal phase)
     */
    suspend operator fun invoke(): Pair<CyclePhase, Int> {
        val today = LocalDate.today()
        val currentCycle = cycleRepository.getCurrentCycle()
        val prefs = prefsRepository.userPreferences.first()

        if (currentCycle == null) {
            return CyclePhase.UNKNOWN to 0
        }

        val dayInCycle = today.toEpochDays() - currentCycle.startDate.toEpochDays() + 1
        val cycleLen = prefs.defaultCycleLength
        val periodLen = prefs.defaultPeriodLength
        val ovulationDay = cycleLen - 14

        val phase = when {
            dayInCycle <= 0 -> CyclePhase.UNKNOWN
            dayInCycle <= periodLen -> CyclePhase.MENSTRUAL
            dayInCycle <= (ovulationDay - 5) -> CyclePhase.FOLLICULAR
            dayInCycle <= (ovulationDay + 1) -> CyclePhase.OVULATION
            dayInCycle <= cycleLen -> CyclePhase.LUTEAL
            else -> CyclePhase.UNKNOWN // past expected cycle length
        }

        return phase to dayInCycle
    }
}
