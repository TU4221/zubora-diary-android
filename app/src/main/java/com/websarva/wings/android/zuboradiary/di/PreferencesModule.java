package com.websarva.wings.android.zuboradiary.di;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class PreferencesModule {

    @Singleton
    @Provides
    public static RxDataStore<Preferences> providePreferencesRxDataStore(
            @ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, "settings").build();
    }

    @Singleton
    @Provides
    public static UserPreferences provideUserPreferences(RxDataStore<Preferences> preferencesRxDataStore) {
        return new UserPreferences(preferencesRxDataStore);
    }
}
