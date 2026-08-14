package com.periodflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_days")
data class CycleDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val flowIntensity: String?,
    val symptoms: String, // JSON array of symptom enum names
    val mood: String?,
    val notes: String,
    val temperature: Float?,
    val weightKg: Float?,
    val ovulationTestResult: String?,
)
