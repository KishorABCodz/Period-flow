package com.periodflow.core.domain.usecase

import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCycleHistoryUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
) {
    operator fun invoke(): Flow<List<Cycle>> {
        return cycleRepository.getAllCycles()
    }
}
