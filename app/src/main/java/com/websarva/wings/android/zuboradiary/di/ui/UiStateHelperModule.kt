package com.websarva.wings.android.zuboradiary.di.ui

import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * UiStateヘルパークラスの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object UiStateHelperModule {

    @Singleton
    @Provides
    fun provideDiaryUiStateHelper(
        buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
    ): DiaryUiStateHelper = DiaryUiStateHelper(buildDiaryImageFilePathUseCase)
}
