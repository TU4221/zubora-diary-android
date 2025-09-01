package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * 設定関連の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * Preferences DataStore関連の生成を担当する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object PreferencesModule {

    @Singleton
    @Provides
    fun provideUserPreferences(
        @ApplicationContext context: Context,
        @ApplicationScope appScope: CoroutineScope
    ): UserPreferencesDataSource = UserPreferencesDataSource(context, appScope)
}
