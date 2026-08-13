package com.periodflow.core.export.di

import com.periodflow.core.domain.repository.ReportExporter
import com.periodflow.core.export.PdfReportGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportModule {

    @Binds
    @Singleton
    abstract fun bindReportExporter(
        pdfReportGenerator: PdfReportGenerator
    ): ReportExporter
}
