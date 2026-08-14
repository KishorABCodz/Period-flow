package com.periodflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycles")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long?,
    val periodLength: Int?,
    val cycleLength: Int?,
)
