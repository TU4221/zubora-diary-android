package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferences {

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public UserPreferences(RxDataStore<Preferences> preferencesRxDataStore) {
        Objects.requireNonNull(preferencesRxDataStore);

        this.dataStore = preferencesRxDataStore;
    }

    // MEMO:初回読込は"null"が返ってくるので、その場合は初期値を返す。(他のPreferenceValueも同様)
    public Flowable<ThemeColorPreferenceValue> loadThemeColorPreferenceValue() {
        return dataStore.data().cache().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(ThemeColorPreferenceValue.PREFERENCES_KEY_COLOR);
            if (savedThemeColorNumber == null) return new ThemeColorPreferenceValue();

            return new ThemeColorPreferenceValue(savedThemeColorNumber);
        });
    }

    public Single<Preferences> saveThemeColorPreferenceValue(ThemeColorPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<CalendarStartDayOfWeekPreferenceValue> loadCalendarStartDayOfWeekPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Integer savedCalendarStartDayOfWeekNumber =
                    preferences.get(CalendarStartDayOfWeekPreferenceValue.PREFERENCES_KEY_DAY_OF_WEEK);
            if (savedCalendarStartDayOfWeekNumber == null) {
                return new CalendarStartDayOfWeekPreferenceValue();
            }

            return new CalendarStartDayOfWeekPreferenceValue(savedCalendarStartDayOfWeekNumber);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreferenceValue(CalendarStartDayOfWeekPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<ReminderNotificationPreferenceValue> loadReminderNotificationPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsReminderNotification =
                    preferences.get(ReminderNotificationPreferenceValue.PREFERENCES_KEY_IS_CHECKED);
            String savedReminderNotificationTime =
                    preferences.get(ReminderNotificationPreferenceValue.PREFERENCES_KEY_TIME);
            if (savedIsReminderNotification == null || savedReminderNotificationTime == null) {
                return new ReminderNotificationPreferenceValue();
            }

            return new ReminderNotificationPreferenceValue(savedIsReminderNotification, savedReminderNotificationTime);
        });
    }

    public Single<Preferences> saveReminderNotificationPreferenceValue(ReminderNotificationPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<PassCodeLockPreferenceValue> loadPasscodeLockPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsPasscodeLock =
                    preferences.get(PassCodeLockPreferenceValue.PREFERENCES_KEY_IS_CHECKED);
            String savedPasscode =
                    preferences.get(PassCodeLockPreferenceValue.PREFERENCES_KEY_PASSCODE);
            if (savedIsPasscodeLock == null || savedPasscode == null) {
                return new PassCodeLockPreferenceValue();
            }

            return new PassCodeLockPreferenceValue(savedIsPasscodeLock, savedPasscode);
        });
    }

    public Single<Preferences> savePasscodeLockPreferenceValue(PassCodeLockPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<WeatherInfoAcquisitionPreferenceValue> loadWeatherInfoAcquisitionPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsGettingWeatherInformation =
                    preferences.get(WeatherInfoAcquisitionPreferenceValue.PREFERENCES_KEY_IS_CHECKED);
            if (savedIsGettingWeatherInformation == null) {
                return new WeatherInfoAcquisitionPreferenceValue();
            }

            return new WeatherInfoAcquisitionPreferenceValue(savedIsGettingWeatherInformation);
        });
    }

    public Single<Preferences> saveWeatherInfoAcquisitionPreferenceValue(WeatherInfoAcquisitionPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Single<Preferences> initialize() {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            new ThemeColorPreferenceValue().setUpPreferences(mutablePreferences);
            new CalendarStartDayOfWeekPreferenceValue().setUpPreferences(mutablePreferences);
            new ReminderNotificationPreferenceValue().setUpPreferences(mutablePreferences);
            new PassCodeLockPreferenceValue().setUpPreferences(mutablePreferences);
            new WeatherInfoAcquisitionPreferenceValue().setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }
}
