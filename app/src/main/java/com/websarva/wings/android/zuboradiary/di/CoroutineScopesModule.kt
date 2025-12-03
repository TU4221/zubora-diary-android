package com.websarva.wings.android.zuboradiary.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * アプリケーション全体で使用するコルーチンスコープを識別するためのカスタム修飾子アノテーション。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * アプリケーション全体で共有されるコルーチンスコープを提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、アプリケーションのライフサイクルと
 * 同じ期間生存する [CoroutineScope] を提供する。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CoroutineScopesModule {

    /**
     * アプリケーションスコープの [CoroutineScope] を提供する。
     *
     * @return アプリケーション全体で共有されるシングルトンの [CoroutineScope] インスタンス。
     */
    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
