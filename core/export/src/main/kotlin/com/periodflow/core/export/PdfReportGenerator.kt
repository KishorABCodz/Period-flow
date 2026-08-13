package com.periodflow.core.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : ReportExporter {

    override suspend fun generatePdfReport(
        cycles: List<Cycle>,
        days: List<CycleDay>,
        analysis: HealthAnalysisReport?
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = document.startPage(pageInfo)
            
            val canvas: Canvas = page.canvas
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
            }
            
            var currentY = 50f
            val startX = 50f
            
            canvas.drawText("PeriodFlow Health Report", startX, currentY, paint)
            currentY += 40f
            
            paint.textSize = 12f
            canvas.drawText("Cycles Analyzed: ${cycles.size}", startX, currentY, paint)
            currentY += 20f
            
            canvas.drawText("Days Logged: ${days.size}", startX, currentY, paint)
            currentY += 40f
            
            analysis?.let {
                paint.textSize = 14f
                canvas.drawText("Health Analysis:", startX, currentY, paint)
                currentY += 20f
                paint.textSize = 12f
                canvas.drawText(it.toString(), startX, currentY, paint)
            }
            
            document.finishPage(page)
            
            val fileName = "PeriodFlow_Report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { fileOutputStream ->
                document.writeTo(fileOutputStream)
            }
            document.close()
            
            ExportResult.Success(Uri.fromFile(file))
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error(e.message ?: "Unknown error occurred during PDF generation")
        }
    }
}
