package com.periodflow.core.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.periodflow.core.domain.model.Cycle
import com.periodflow.core.domain.model.CycleDay
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.repository.ExportResult
import com.periodflow.core.domain.repository.ReportExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * A4 PDF report with:
 *  - Title & basic counts
 *  - Deterministic Health Analysis (score, level, indicators, recommendations)
 *  - Optional AI-generated personalised narrative
 *  - Disclaimer
 *
 * Multi-page: when the pen crosses the bottom margin, a new page is started
 * automatically. Text is word-wrapped to page width.
 */
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : ReportExporter {

    private companion object {
        const val PAGE_WIDTH = 595   // A4
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val LINE_SPACING = 4f
        const val PARAGRAPH_SPACING = 12f
    }

    override suspend fun generatePdfReport(
        cycles: List<Cycle>,
        days: List<CycleDay>,
        analysis: HealthAnalysisReport?,
        aiNarrative: String?,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val body = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }
            val h1 = Paint(body).apply { textSize = 22f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
            val h2 = Paint(body).apply { textSize = 15f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
            val muted = Paint(body).apply { color = Color.rgb(90, 90, 90) }

            val writer = PdfWriter(document)

            // Title
            writer.drawLine("PeriodFlow Health Report", h1)
            writer.drawLine("Cycles analyzed: ${cycles.size}   |   Days logged: ${days.size}", muted)
            writer.paragraph()

            // Deterministic analysis
            analysis?.let { r ->
                writer.drawLine("Health Analysis", h2)
                writer.drawLine("Risk score: ${r.riskScore}/100 (${r.riskLevel.displayName})", body)
                writer.drawLine("Cycles used in analysis: ${r.cyclesAnalyzed}", muted)
                writer.paragraph()

                if (r.indicators.isNotEmpty()) {
                    writer.drawLine("Key indicators", h2)
                    r.indicators.forEach { i ->
                        writer.wrapped("• ${i.name} (score ${i.score}) — ${i.description}", body)
                        writer.wrapped("   Data: ${i.dataPoints}", muted)
                    }
                    writer.paragraph()
                }

                if (r.recommendations.isNotEmpty()) {
                    writer.drawLine("Recommendations", h2)
                    r.recommendations.forEach { rec -> writer.wrapped("• $rec", body) }
                    writer.paragraph()
                }
            }

            // AI narrative
            if (!aiNarrative.isNullOrBlank()) {
                writer.drawLine("AI Personal Insight", h2)
                aiNarrative.split("\n\n").forEach { para ->
                    if (para.isNotBlank()) {
                        writer.wrapped(para.trim(), body)
                        writer.paragraph()
                    }
                }
            }

            // Disclaimer
            analysis?.let {
                writer.paragraph()
                writer.wrapped(it.disclaimer, muted)
            }

            writer.finish()

            val fileName = "PeriodFlow_Report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            ExportResult.Success(Uri.fromFile(file))
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error(e.message ?: "Unknown error occurred during PDF generation")
        }
    }

    /**
     * Small helper that keeps a running pen position and lazily starts pages
     * so callers can draw text linearly without worrying about layout math.
     */
    private class PdfWriter(private val document: PdfDocument) {
        private var pageIndex = 1
        private var page: PdfDocument.Page = document.startPage(pageInfo())
        private var canvas: Canvas = page.canvas
        private var y = MARGIN + 16f

        private fun pageInfo() =
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()

        private fun newPageIfNeeded(neededHeight: Float) {
            if (y + neededHeight > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageIndex += 1
                page = document.startPage(pageInfo())
                canvas = page.canvas
                y = MARGIN + 16f
            }
        }

        fun drawLine(text: String, paint: Paint) {
            newPageIfNeeded(paint.textSize + LINE_SPACING)
            canvas.drawText(text, MARGIN, y, paint)
            y += paint.textSize + LINE_SPACING
        }

        fun wrapped(text: String, paint: Paint) {
            val maxWidth = PAGE_WIDTH - 2 * MARGIN
            val words = text.split(' ')
            val line = StringBuilder()
            for (word in words) {
                val trial = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(trial) > maxWidth) {
                    drawLine(line.toString(), paint)
                    line.clear()
                    line.append(word)
                } else {
                    line.setLength(0)
                    line.append(trial)
                }
            }
            if (line.isNotEmpty()) drawLine(line.toString(), paint)
        }

        fun paragraph() {
            y += PARAGRAPH_SPACING
        }

        fun finish() {
            document.finishPage(page)
        }
    }
}
