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
    public Flowable<ThemeColorPreference> loadThemeColorPreference() {
        return dataStore.data().cache().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(ThemeColorPreference.PREFERENCES_KEY_COLOR);
            if (savedThemeColorNumber == null) return new ThemeColorPreference();

            return new ThemeColorPreference(savedThemeColorNumber);
        });
    }

    public Single<Preferences> saveThemeColorPreference(ThemeColorPreference value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<CalendarStartDayOfWeekPreference> loadCalendarStartDayOfWeekPreference() {
        return dataStore.data().map(preferences -> {
            Integer savedCalendarStartDayOfWeekNumber =
                    preferences.get(CalendarStartDayOfWeekPreference.PREFERENCES_KEY_DAY_OF_WEEK);
            if (savedCalendarStartDayOfWeekNumber == null) {
                return new CalendarStartDayOfWeekPreference();
            }

            return new CalendarStartDayOfWeekPreference(savedCalendarStartDayOfWeekNumber);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreference(CalendarStartDayOfWeekPreference value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<ReminderNotificationPreference> loadReminderNotificationPreference() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsReminderNotification =
                    preferences.get(ReminderNotificationPreference.PREFERENCES_KEY_IS_CHECKED);
            String savedReminderNotificationTime =
                    preferences.get(ReminderNotificationPreference.PREFERENCES_KEY_TIME);
            if (savedIsReminderNotification == null || savedReminderNotificationTime == null) {
                return new ReminderNotificationPreference();
            }

            return new ReminderNotificationPreference(savedIsReminderNotification, savedReminderNotificationTime);
        });
    }

    public Single<Preferences> saveReminderNotificationPreference(ReminderNotificationPreference value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<PassCodeLockPreference> loadPasscodeLockPreference() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsPasscodeLock =
                    preferences.get(PassCodeLockPreference.PREFERENCES_KEY_IS_CHECKED);
            String savedPasscode =
                    preferences.get(PassCodeLockPreference.PREFERENCES_KEY_PASSCODE);
            if (savedIsPasscodeLock == null || savedPasscode == null) {
                return new PassCodeLockPreference();
            }

            return new PassCodeLockPreference(savedIsPasscodeLock, savedPasscode);
        });
    }

    public Single<Preferences> savePasscodeLockPreference(PassCodeLockPreference value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<WeatherInfoAcquisitionPreference> loadWeatherInfoAcquisitionPreference() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsGettingWeatherInformation =
                    preferences.get(WeatherInfoAcquisitionPreference.PREFERENCES_KEY_IS_CHECKED);
            if (savedIsGettingWeatherInformation == null) {
                return new WeatherInfoAcquisitionPreference();
            }

            return new WeatherInfoAcquisitionPreference(savedIsGettingWeatherInformation);
        });
    }

    public Single<Preferences> saveWeatherInfoAcquisitionPreference(WeatherInfoAcquisitionPreference value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            value.setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }

    public Single<Preferences> initializeAllPreferences() {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            new ThemeColorPreference().setUpPreferences(mutablePreferences);
            new CalendarStartDayOfWeekPreference().setUpPreferences(mutablePreferences);
            new ReminderNotificationPreference().setUpPreferences(mutablePreferences);
            new PassCodeLockPreference().setUpPreferences(mutablePreferences);
            new WeatherInfoAcquisitionPreference().setUpPreferences(mutablePreferences);
            return Single.just(mutablePreferences);
        });
    }
}
