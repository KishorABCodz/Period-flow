package com.periodflow.core.common

import kotlinx.datetime.*

fun LocalDate.toEpochDay(): Long = this.toEpochDays().toLong()

fun Long.toLocalDate(): LocalDate = LocalDate.fromEpochDays(this.toInt())

fun LocalDate.Companion.today(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun LocalDate.daysUntil(other: LocalDate): Int =
    other.toEpochDays() - this.toEpochDays()

fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(this.toEpochDays() + days)

fun LocalDate.minusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(this.toEpochDays() - days)

fun LocalDate.isInRange(start: LocalDate, end: LocalDate): Boolean =
    this >= start && this <= end

fun LocalDate.yearMonth(): String = "${this.year}-${this.monthNumber.toString().padStart(2, '0')}"
