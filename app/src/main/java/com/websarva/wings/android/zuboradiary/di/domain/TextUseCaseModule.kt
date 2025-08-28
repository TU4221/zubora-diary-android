package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.usecase.text.ValidateInputTextUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TextUseCaseModule {

    @Singleton
    @Provides
    fun provideValidateInputTextUseCase(): ValidateInputTextUseCase = ValidateInputTextUseCase()
}
