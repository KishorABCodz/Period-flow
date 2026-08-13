package com.periodflow.core.domain.usecase

import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.repository.CycleDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetDaysForMonthUseCase @Inject constructor(
    private val cycleDayRepository: CycleDayRepository,
) {
    operator fun invoke(year: Int, month: Int): Flow<List<CycleDay>> {
        val startDate = LocalDate(year, month, 1)
        val endDate = if (month == 12) {
            LocalDate(year + 1, 1, 1)
        } else {
            LocalDate(year, month + 1, 1)
        }
        // endDate is exclusive, get the last day of the month
        val lastDay = LocalDate.fromEpochDays(endDate.toEpochDays() - 1)
        return cycleDayRepository.getDaysInRange(startDate, lastDay)
    }
}
