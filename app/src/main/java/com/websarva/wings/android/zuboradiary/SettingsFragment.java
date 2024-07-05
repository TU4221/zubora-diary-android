package com.websarva.wings.android.zuboradiary;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Set;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        RxDataStore<Preferences> dataStore;
        DataStoreSingleton dataStoreSingleton = DataStoreSingleton.getInstance();
        if (dataStoreSingleton.getDataStore() == null) {
            dataStore = new RxPreferenceDataStoreBuilder(requireContext(), /*name=*/ "settings").build();
        } else {
            dataStore = dataStoreSingleton.getDataStore();
        }
        dataStoreSingleton.setDataStore(dataStore);

        getPreferenceManager().setPreferenceDataStore(new customPreferenceDataStore(dataStore));
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    private class customPreferenceDataStore extends PreferenceDataStore {
        RxDataStore<Preferences> dataStore;
        public customPreferenceDataStore(RxDataStore<Preferences> dataStore) {
            this.dataStore = dataStore;
        }

        @Override
        public void putString(String key, @Nullable String value) {
            Preferences.Key<String> dataStoreKey = PreferencesKeys.stringKey(key);
            this.dataStore.updateDataAsync(new Function<Preferences, Single<Preferences>>() {
                @Override
                public Single<Preferences> apply(Preferences preferences) throws Throwable {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(dataStoreKey, value);
                    return Single.just(mutablePreferences);
                }
            });
        }

        @Override
        public void putStringSet(String key, @Nullable Set<String> values) {
            throw new UnsupportedOperationException("Not implemented on this data store");
        }

        @Override
        public void putInt(String key, int value) {
            throw new UnsupportedOperationException("Not implemented on this data store");
        }

        @Override
        public void putLong(String key, long value) {
            throw new UnsupportedOperationException("Not implemented on this data store");
        }

        @Override
        public void putFloat(String key, float value) {
            throw new UnsupportedOperationException("Not implemented on this data store");
        }

        @Override
        public void putBoolean(String key, boolean value) {
            throw new UnsupportedOperationException("Not implemented on this data store");
        }

        @Override
        @Nullable
        public String getString(String key, @Nullable String defValue) {
            Preferences.Key<String> dataStoreKey = PreferencesKeys.stringKey(key);
            Preferences preferences = this.dataStore.data().blockingFirst();
            return preferences.get(dataStoreKey) != null ? preferences.get(dataStoreKey) : defValue;
        }

        @Override
        @Nullable
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return defValue;
        }

    }
}
