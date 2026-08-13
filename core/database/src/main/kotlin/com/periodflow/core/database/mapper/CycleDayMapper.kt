package com.periodflow.core.database.mapper

import com.periodflow.core.common.toEpochDay
import com.periodflow.core.common.toLocalDate
import com.periodflow.core.database.entity.CycleDayEntity
import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.model.FlowIntensity
import com.periodflow.core.domain.model.Mood
import com.periodflow.core.domain.model.Symptom
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

fun CycleDayEntity.toDomain(): CycleDay {
    return CycleDay(
        id = id,
        date = dateEpochDay.toLocalDate(),
        flowIntensity = FlowIntensity.fromName(flowIntensity),
        symptoms = try {
            if (symptoms.isBlank()) emptyList()
            else Json.decodeFromString<List<String>>(symptoms).mapNotNull { Symptom.fromName(it) }
        } catch (e: Exception) {
            emptyList()
        },
        mood = Mood.fromName(mood),
        notes = notes,
        temperature = temperature,
        weightKg = weightKg,
        ovulationTestResult = com.periodflow.core.domain.model.OvulationTestResult.fromName(ovulationTestResult)
    )
}

fun CycleDay.toEntity(): CycleDayEntity {
    return CycleDayEntity(
        id = id,
        dateEpochDay = date.toEpochDay(),
        flowIntensity = flowIntensity?.name,
        symptoms = Json.encodeToString(symptoms.map { it.name }),
        mood = mood?.name,
        notes = notes,
        temperature = temperature,
        weightKg = weightKg,
        ovulationTestResult = ovulationTestResult?.name,
    )
}
