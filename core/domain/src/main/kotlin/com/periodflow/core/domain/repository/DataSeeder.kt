package com.periodflow.core.domain.repository

/**
 * Debug/testing utility for populating and clearing app data.
 * Implementations should generate realistic period tracking data
 * spanning multiple cycles to exercise all UI features.
 */
interface DataSeeder {
    /** Insert ~6 months of realistic cycle + daily log data. */
    suspend fun seedSampleData()

    /** Delete ALL cycle and cycle-day records from the database. */
    suspend fun clearAllData()

    /** Returns true if any cycle data currently exists. */
    suspend fun hasData(): Boolean
}
