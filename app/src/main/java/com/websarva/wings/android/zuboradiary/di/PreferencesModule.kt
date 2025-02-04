package com.websarva.wings.android.zuboradiary.di

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @JvmStatic
    @Singleton
    @Provides
    fun providePreferencesRxDataStore(
        @ApplicationContext context: Context
    ): RxDataStore<Preferences> {
        return RxPreferenceDataStoreBuilder(context, "user_preferences").build()
    }

    @Singleton
    @Provides
    fun provideUserPreferences(preferencesRxDataStore: RxDataStore<Preferences>): UserPreferences {
        return UserPreferences(preferencesRxDataStore)
    }
}
