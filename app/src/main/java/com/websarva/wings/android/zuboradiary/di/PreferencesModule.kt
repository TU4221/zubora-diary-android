package com.websarva.wings.android.zuboradiary.di

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
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
    ): UserPreferences {
        return UserPreferences(context, appScope)
    }
}
