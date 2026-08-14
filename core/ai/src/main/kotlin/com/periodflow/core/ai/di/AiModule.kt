package com.periodflow.core.ai.di

import com.periodflow.core.ai.repository.GeminiAiRepository
import com.periodflow.core.ai.voice.CompositeFastProvider
import com.periodflow.core.ai.voice.FastLlmProvider
import com.periodflow.core.ai.voice.VoiceCompanionOrchestrator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBindingsModule {

    /**
     * Default fast provider = composite (Gemma-2 2B when downloaded, else heuristic).
     */
    @Binds
    @Singleton
    abstract fun bindFastLlmProvider(impl: CompositeFastProvider): FastLlmProvider
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiAiRepository(): GeminiAiRepository = GeminiAiRepository()

    @Provides
    @Singleton
    fun provideVoiceCompanionOrchestrator(
        fastProvider: FastLlmProvider,
        gemini: GeminiAiRepository,
    ): VoiceCompanionOrchestrator = VoiceCompanionOrchestrator(fastProvider, gemini)
}
