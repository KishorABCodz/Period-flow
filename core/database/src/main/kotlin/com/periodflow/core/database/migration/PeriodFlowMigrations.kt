package com.periodflow.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations preserving user data across upgrades.
 *
 * v1 → v2 : historical (already deployed before AI features).
 * v2 → v3 : add `ai_insight` table for cached AI narrative.
 * v3 → v4 : add `chat_message` table for persisted Bloom chat history.
 *
 * IMPORTANT: keep these in sync with entity classes. If you ever change a
 * column, add a new migration rather than mutating an existing one.
 */
object PeriodFlowMigrations {

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `ai_insight` (
                    `id` INTEGER NOT NULL,
                    `narrative` TEXT NOT NULL,
                    `updatedAtEpochMilli` INTEGER NOT NULL,
                    `basedOnRiskScore` INTEGER NOT NULL DEFAULT -1,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chat_message` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `isUser` INTEGER NOT NULL,
                    `text` TEXT NOT NULL,
                    `createdAtEpochMilli` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3, MIGRATION_3_4)
}
