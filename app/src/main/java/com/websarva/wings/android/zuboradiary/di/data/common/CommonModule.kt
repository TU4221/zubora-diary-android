package com.websarva.wings.android.zuboradiary.di.data.common

import android.app.Application
import com.websarva.wings.android.zuboradiary.ZuboraDiaryApplication
import com.websarva.wings.android.zuboradiary.data.common.AppForegroundStateProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * データ層全体で利用される共通的な依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * アプリケーション関連の生成を担当する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CommonModule {

    @Singleton
    @Provides
    fun provideAppForegroundStateProvider(
        application: Application
    ): AppForegroundStateProvider =
        AppForegroundStateProviderImpl(application as ZuboraDiaryApplication)
}
