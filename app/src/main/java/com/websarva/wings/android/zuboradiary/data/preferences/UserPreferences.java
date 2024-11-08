package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import java.time.DayOfWeek;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferences {

    private final RxDataStore<Preferences> dataStore;
    private final Preferences.Key<Integer> KEY_THEME_COLOR = PreferencesKeys.intKey("theme_color");
    private final Preferences.Key<Integer> KEY_CALENDAR_START_DAY_OF_WEEK =
                                        PreferencesKeys.intKey("calendar_start_day_of_week");
    private final Preferences.Key<Boolean> KEY_IS_CHECKED_REMINDER_NOTIFICATION =
                                         PreferencesKeys.booleanKey("is_checked_reminder_notification");
    private final Preferences.Key<String> KEY_REMINDER_NOTIFICATION_TIME =
                                    PreferencesKeys.stringKey("reminder_notification_time");
    private final Preferences.Key<Boolean> KEY_IS_CHECKED_PASSCODE_LOCK =
                                                PreferencesKeys.booleanKey("is_checked_passcode_lock");
    private final Preferences.Key<String> KEY_PASSCODE = PreferencesKeys.stringKey("passcode");
    private final Preferences.Key<Boolean> KEY_IS_CHECKED_WEATHER_INFO_ACQUISITION =
                                    PreferencesKeys.booleanKey("is_checked_weather_info_acquisition");

    @Inject
    public UserPreferences(RxDataStore<Preferences> preferencesRxDataStore) {
        Objects.requireNonNull(preferencesRxDataStore);

        this.dataStore = preferencesRxDataStore;
    }

    // MEMO:初回読込は"null"が返ってくるので、その場合は初期値を返す。(他のPreferenceValueも同様)
    public Flowable<ThemeColorPreferenceValue> loadThemeColorPreferenceValue() {
        return dataStore.data().cache().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(KEY_THEME_COLOR);
            if (savedThemeColorNumber == null) {
                return new ThemeColorPreferenceValue(ThemeColor.values()[0].toNumber());
            }

            return new ThemeColorPreferenceValue(savedThemeColorNumber);
        });
    }

    public Single<Preferences> saveThemeColorPreferenceValue(ThemeColorPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_THEME_COLOR, value.getThemeColorNumber());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<CalendarStartDayOfWeekPreferenceValue> loadCalendarStartDayOfWeekPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Integer savedCalendarStartDayOfWeekNumber = preferences.get(KEY_CALENDAR_START_DAY_OF_WEEK);
            if (savedCalendarStartDayOfWeekNumber == null) {
                return new CalendarStartDayOfWeekPreferenceValue(DayOfWeek.SUNDAY.getValue());
            }

            return new CalendarStartDayOfWeekPreferenceValue(savedCalendarStartDayOfWeekNumber);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreferenceValue(CalendarStartDayOfWeekPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_CALENDAR_START_DAY_OF_WEEK, value.getDayOfWeekNumber());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<ReminderNotificationPreferenceValue> loadReminderNotificationPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsReminderNotification = preferences.get(KEY_IS_CHECKED_REMINDER_NOTIFICATION);
            String savedReminderNotificationTime = preferences.get(KEY_REMINDER_NOTIFICATION_TIME);
            if (savedIsReminderNotification == null || savedReminderNotificationTime == null) {
                return new ReminderNotificationPreferenceValue(false, "");
            }

            return new ReminderNotificationPreferenceValue(savedIsReminderNotification, savedReminderNotificationTime);
        });
    }

    public Single<Preferences> saveReminderNotificationPreferenceValue(ReminderNotificationPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_CHECKED_REMINDER_NOTIFICATION, value.getIsChecked());
            mutablePreferences.set(KEY_REMINDER_NOTIFICATION_TIME, value.getNotificationTimeString());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<PassCodeLockPreferenceValue> loadPasscodeLockPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsPasscodeLock = preferences.get(KEY_IS_CHECKED_PASSCODE_LOCK);
            String savedPasscode = preferences.get(KEY_PASSCODE);
            if (savedIsPasscodeLock == null || savedPasscode == null) {
                return new PassCodeLockPreferenceValue(false, "");
            }
            return new PassCodeLockPreferenceValue(savedIsPasscodeLock, savedPasscode);
        });
    }

    public Single<Preferences> savePasscodeLockPreferenceValue(PassCodeLockPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_CHECKED_PASSCODE_LOCK, value.getIsChecked());
            mutablePreferences.set(KEY_PASSCODE, value.getCode());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<WeatherInfoAcquisitionPreferenceValue> loadWeatherInfoAcquisitionPreferenceValue() {
        return dataStore.data().map(preferences -> {
            Boolean savedIsGettingWeatherInformation = preferences.get(KEY_IS_CHECKED_WEATHER_INFO_ACQUISITION);
            if (savedIsGettingWeatherInformation == null) {
                return new WeatherInfoAcquisitionPreferenceValue(false);
            }
            return new WeatherInfoAcquisitionPreferenceValue(savedIsGettingWeatherInformation);
        });
    }

    public Single<Preferences> saveWeatherInfoAcquisitionPreferenceValue(WeatherInfoAcquisitionPreferenceValue value) {
        Objects.requireNonNull(value);

        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_CHECKED_WEATHER_INFO_ACQUISITION, value.getIsChecked());
            return Single.just(mutablePreferences);
        });
    }
}
