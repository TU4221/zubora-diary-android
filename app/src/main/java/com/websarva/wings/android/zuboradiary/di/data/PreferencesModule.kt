package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton



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
