package com.periodflow.core.domain.usecase

import com.periodflow.core.common.Result
import com.periodflow.core.common.runCatchingResult
import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.repository.CycleDayRepository
import javax.inject.Inject

class LogCycleDayUseCase @Inject constructor(
    private val cycleDayRepository: CycleDayRepository,
) {
    suspend operator fun invoke(day: CycleDay): Result<Unit> {
        return runCatchingResult {
            cycleDayRepository.upsertDay(day)
        }
    }
}
