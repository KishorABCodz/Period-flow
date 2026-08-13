package com.periodflow.core.domain.repository

import android.net.Uri
import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.model.HealthAnalysisReport

sealed interface ExportResult {
    data class Success(val fileUri: Uri) : ExportResult
    data class Error(val message: String) : ExportResult
}

interface ReportExporter {
    suspend fun generatePdfReport(
        cycles: List<Cycle>,
        days: List<CycleDay>,
        analysis: HealthAnalysisReport?
    ): ExportResult
}
