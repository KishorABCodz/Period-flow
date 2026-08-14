package com.periodflow.core.database

import com.periodflow.core.database.dao.CycleDao
import com.periodflow.core.database.dao.CycleDayDao
import com.periodflow.core.database.entity.CycleDayEntity
import com.periodflow.core.database.entity.CycleEntity
import com.periodflow.core.domain.repository.DataSeeder
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates 6 completed cycles + 1 ongoing cycle of realistic period data
 * going back ~7 months from today. Each cycle has:
 * - Varied cycle lengths (26–31 days)
 * - Period days with escalating/tapering flow intensity
 * - Mood, symptom, temperature, and occasional ovulation-test logs
 */
@Singleton
class SeedDataManagerImpl @Inject constructor(
    private val cycleDao: CycleDao,
    private val cycleDayDao: CycleDayDao,
) : DataSeeder {

    override suspend fun seedSampleData() {
        // Clear first to avoid duplicates
        clearAllData()

        val today = java.time.LocalDate.now()
        val todayEpoch = today.toEpochDay()

        // Define 6 completed cycles + 1 ongoing, working backwards from today
        data class CycleDef(val length: Int, val periodLength: Int)

        val cycleDefs = listOf(
            CycleDef(28, 5), // oldest → cycle 1
            CycleDef(30, 6),
            CycleDef(27, 4),
            CycleDef(29, 5),
            CycleDef(26, 5),
            CycleDef(28, 4), // most recent completed
        )

        // Calculate start dates by working backwards
        // Ongoing cycle started ~8 days ago
        val ongoingCycleStart = todayEpoch - 8

        // Work backwards from ongoing cycle start for the completed cycles
        var cursor = ongoingCycleStart
        val cycleStarts = mutableListOf<Long>()
        for (def in cycleDefs.reversed()) {
            cursor -= def.length
            cycleStarts.add(0, cursor)
        }

        // === Insert completed cycles ===
        for ((i, def) in cycleDefs.withIndex()) {
            val startEpoch = cycleStarts[i]
            val endEpoch = startEpoch + def.length - 1

            cycleDao.upsertCycle(
                CycleEntity(
                    startDateEpochDay = startEpoch,
                    endDateEpochDay = endEpoch,
                    periodLength = def.periodLength,
                    cycleLength = def.length,
                )
            )

            // Generate daily logs for period days
            generatePeriodDays(startEpoch, def.periodLength)

            // Generate some mid-cycle logs (ovulation window, random moods)
            generateMidCycleDays(startEpoch, def.length)
        }

        // === Insert the ongoing (current) cycle ===
        cycleDao.upsertCycle(
            CycleEntity(
                startDateEpochDay = ongoingCycleStart,
                endDateEpochDay = null, // ongoing
                periodLength = null,
                cycleLength = null,
            )
        )

        // Ongoing cycle period days (first 5 days, but only up to today)
        val ongoingPeriodLength = minOf(5, (todayEpoch - ongoingCycleStart + 1).toInt())
        generatePeriodDays(ongoingCycleStart, ongoingPeriodLength)
    }

    /**
     * Generate realistic CycleDayEntity records for period days.
     * Flow pattern: MEDIUM → HEAVY → HEAVY → MEDIUM → LIGHT → SPOTTING
     */
    private suspend fun generatePeriodDays(cycleStartEpoch: Long, periodLength: Int) {
        val flowPattern = listOf("MEDIUM", "HEAVY", "HEAVY", "MEDIUM", "LIGHT", "SPOTTING")
        val periodMoods = listOf("TIRED", "SENSITIVE", "IRRITABLE", "SAD", "CALM", "CALM")
        val periodSymptomSets = listOf(
            listOf("CRAMPS", "BLOATING", "FATIGUE"),
            listOf("CRAMPS", "HEADACHE", "BACKACHE", "FATIGUE"),
            listOf("CRAMPS", "BLOATING", "CRAVINGS"),
            listOf("BACKACHE", "FATIGUE", "CRAVINGS"),
            listOf("FATIGUE", "BLOATING"),
            listOf("FATIGUE"),
        )

        for (dayOffset in 0 until periodLength) {
            val epochDay = cycleStartEpoch + dayOffset
            val patternIdx = dayOffset.coerceAtMost(flowPattern.lastIndex)

            // BBT temperature: lower during period (36.1–36.4°C)
            val temp = 36.1f + (Math.random() * 0.3f).toFloat()

            cycleDayDao.upsertDay(
                CycleDayEntity(
                    dateEpochDay = epochDay,
                    flowIntensity = flowPattern[patternIdx],
                    symptoms = Json.encodeToString(periodSymptomSets[patternIdx]),
                    mood = periodMoods[patternIdx],
                    notes = if (dayOffset == 0) "Period started" else "",
                    temperature = (Math.round(temp * 10f) / 10f),
                    weightKg = null,
                    ovulationTestResult = null,
                )
            )
        }
    }

    /**
     * Generate mid-cycle logs: ovulation window (days 12–16) and a few
     * random follicular/luteal logs with mood + temperature data.
     */
    private suspend fun generateMidCycleDays(cycleStartEpoch: Long, cycleLength: Int) {
        // Ovulation window: days 12-16 of cycle
        val ovulationDay = cycleLength / 2 - 1 // ~day 13-14
        for (dayOffset in (ovulationDay - 2)..(ovulationDay + 1)) {
            if (dayOffset < 7 || dayOffset >= cycleLength) continue
            val epochDay = cycleStartEpoch + dayOffset

            // Check if day already exists (from period generation)
            if (cycleDayDao.getDayByDate(epochDay) != null) continue

            // Higher BBT post-ovulation (36.4–36.8°C)
            val isPostOvulation = dayOffset >= ovulationDay
            val baseTemp = if (isPostOvulation) 36.5f else 36.2f
            val temp = baseTemp + (Math.random() * 0.3f).toFloat()

            val ovulationResult = when (dayOffset) {
                ovulationDay - 1 -> "NEGATIVE"
                ovulationDay -> "POSITIVE"
                ovulationDay + 1 -> "NEGATIVE"
                else -> null
            }

            val mood = when {
                dayOffset < ovulationDay -> listOf("ENERGETIC", "HAPPY", "CALM").random()
                else -> listOf("CALM", "SENSITIVE", "HAPPY").random()
            }

            val symptoms = when {
                dayOffset == ovulationDay -> listOf("BLOATING", "BREAST_TENDERNESS")
                dayOffset > ovulationDay + 5 -> listOf("CRAVINGS", "BREAST_TENDERNESS", "ACNE")
                else -> emptyList()
            }

            cycleDayDao.upsertDay(
                CycleDayEntity(
                    dateEpochDay = epochDay,
                    flowIntensity = null,
                    symptoms = Json.encodeToString(symptoms),
                    mood = mood,
                    notes = if (dayOffset == ovulationDay) "Ovulation day 🌸" else "",
                    temperature = (Math.round(temp * 10f) / 10f),
                    weightKg = null,
                    ovulationTestResult = ovulationResult,
                )
            )
        }

        // Add a couple of luteal phase entries (PMS symptoms)
        val lutealStart = ovulationDay + 5
        for (dayOffset in lutealStart..(lutealStart + 2)) {
            if (dayOffset >= cycleLength) break
            val epochDay = cycleStartEpoch + dayOffset
            if (cycleDayDao.getDayByDate(epochDay) != null) continue

            val temp = 36.5f + (Math.random() * 0.3f).toFloat()

            cycleDayDao.upsertDay(
                CycleDayEntity(
                    dateEpochDay = epochDay,
                    flowIntensity = null,
                    symptoms = Json.encodeToString(
                        listOf("CRAVINGS", "BREAST_TENDERNESS", "IRRITABLE", "BLOATING")
                            .shuffled().take(2 + (Math.random() * 2).toInt())
                    ),
                    mood = listOf("IRRITABLE", "SENSITIVE", "ANXIOUS", "TIRED").random(),
                    notes = "",
                    temperature = (Math.round(temp * 10f) / 10f),
                    weightKg = null,
                    ovulationTestResult = null,
                )
            )
        }
    }

    override suspend fun clearAllData() {
        // Room doesn't have a built-in "delete all", so we use the DAOs
        // Delete all cycle days
        val allDays = cycleDayDao.getAllDays().first()
        for (day in allDays) {
            cycleDayDao.deleteByDate(day.dateEpochDay)
        }

        // Delete all cycles
        val allCycles = cycleDao.getAllCycles().first()
        for (cycle in allCycles) {
            cycleDao.deleteCycle(cycle.id)
        }
    }

    override suspend fun hasData(): Boolean {
        return cycleDao.getCurrentCycle() != null ||
                cycleDao.getAllCycles().first().isNotEmpty()
    }
}
