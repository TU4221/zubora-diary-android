package com.websarva.wings.android.zuboradiary.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences;

import java.util.Objects;

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
    @NonNull
    public static RxDataStore<Preferences> providePreferencesRxDataStore(
            @ApplicationContext Context context) {
        Objects.requireNonNull(context);

        RxDataStore<Preferences> rxDataStore =
                new RxPreferenceDataStoreBuilder(context, "user_preferences").build();
        return Objects.requireNonNull(rxDataStore);
    }

    @Singleton
    @Provides
    @NonNull
    public static UserPreferences provideUserPreferences(RxDataStore<Preferences> preferencesRxDataStore) {
        Objects.requireNonNull(preferencesRxDataStore);

        UserPreferences userPreferences = new UserPreferences(preferencesRxDataStore);
        return Objects.requireNonNull(userPreferences);
    }
}
