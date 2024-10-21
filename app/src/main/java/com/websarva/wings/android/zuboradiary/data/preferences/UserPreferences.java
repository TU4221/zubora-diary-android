package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import java.time.DayOfWeek;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferences {
    // TODO:Key "is_~" ->"is_checked_~"に変更
    // TODO:PASSCODEはStringの方が良いかも
    private final RxDataStore<Preferences> dataStore;
    private final Preferences.Key<Integer> KEY_THEME_COLOR = PreferencesKeys.intKey("theme_color");
    private final Preferences.Key<Integer> KEY_CALENDAR_START_DAY_OF_WEEK =
                                        PreferencesKeys.intKey("calendar_start_day_of_week");
    private final Preferences.Key<Boolean> KEY_IS_REMINDER_NOTIFICATION =
                                         PreferencesKeys.booleanKey("is_reminder_notification");
    private final Preferences.Key<String> KEY_REMINDER_NOTIFICATION_TIME =
                                    PreferencesKeys.stringKey("reminder_notification_time");
    private final Preferences.Key<Boolean> KEY_IS_PASSCODE_LOCK =
                                                PreferencesKeys.booleanKey("is_passcode_lock");
    private final Preferences.Key<Integer> KEY_PASSCODE = PreferencesKeys.intKey("passcode");
    private final Preferences.Key<Boolean> KEY_IS_GETTING_WEATHER_INFORMATION =
                                    PreferencesKeys.booleanKey("is_getting_weather_information");
    @Inject
    public UserPreferences(RxDataStore<Preferences> preferencesRxDataStore) {
        this.dataStore = preferencesRxDataStore;
    }

    public Flowable<ThemeColorPreferenceValue> loadThemeColorPreferenceValue() {
        return this.dataStore.data().cache().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(KEY_THEME_COLOR);
            if (savedThemeColorNumber == null) {
                return new ThemeColorPreferenceValue(ThemeColor.values()[0].getNumber());
            }

            return new ThemeColorPreferenceValue(savedThemeColorNumber);
        });
    }

    public Single<Preferences> saveThemeColorPreferenceValue(ThemeColorPreferenceValue value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_THEME_COLOR, value.getThemeColorNumber());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<CalendarStartDayOfWeekPreferenceValue> loadCalendarStartDayOfWeekPreferenceValue() {
        return this.dataStore.data().map(preferences -> {
            Integer savedCalendarStartDayOfWeekNumber = preferences.get(KEY_CALENDAR_START_DAY_OF_WEEK);
            if (savedCalendarStartDayOfWeekNumber == null) {
                return new CalendarStartDayOfWeekPreferenceValue(DayOfWeek.SUNDAY.getValue());
            }

            return new CalendarStartDayOfWeekPreferenceValue(savedCalendarStartDayOfWeekNumber);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreferenceValue(CalendarStartDayOfWeekPreferenceValue value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_CALENDAR_START_DAY_OF_WEEK, value.getDayOfWeekNumber());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<ReminderNotificationPreferenceValue> loadReminderNotificationPreferenceValue() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsReminderNotification = preferences.get(KEY_IS_REMINDER_NOTIFICATION);
            String savedReminderNotificationTime = preferences.get(KEY_REMINDER_NOTIFICATION_TIME);
            if (savedIsReminderNotification == null || savedReminderNotificationTime == null) {
                return new ReminderNotificationPreferenceValue(Boolean.FALSE, "");
            }

            return new ReminderNotificationPreferenceValue(savedIsReminderNotification, savedReminderNotificationTime);
        });
    }

    public Single<Preferences> saveReminderNotificationPreferenceValue(ReminderNotificationPreferenceValue value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_REMINDER_NOTIFICATION, value.getIsChecked());
            mutablePreferences.set(KEY_REMINDER_NOTIFICATION_TIME, value.getNotificationTimeString());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<PassCodeLockPreferenceValue> loadPasscodeLockPreferenceValue() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsPasscodeLock = preferences.get(KEY_IS_PASSCODE_LOCK);
            Integer savedPasscode = preferences.get(KEY_PASSCODE);
            if (savedIsPasscodeLock == null || savedPasscode == null) {
                return new PassCodeLockPreferenceValue(false, -1);
            }
            return new PassCodeLockPreferenceValue(savedIsPasscodeLock, savedPasscode);
        });
    }

    public Single<Preferences> savePasscodeLockPreferenceValue(PassCodeLockPreferenceValue value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_PASSCODE_LOCK, value.getIsChecked());
            mutablePreferences.set(KEY_PASSCODE, value.getCode());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<WeatherInfoAcquisitionPreferenceValue> loadGettingWeatherInformationPreferenceValue() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsGettingWeatherInformation = preferences.get(KEY_IS_GETTING_WEATHER_INFORMATION);
            if (savedIsGettingWeatherInformation == null) {
                return new WeatherInfoAcquisitionPreferenceValue(false);
            }
            return new WeatherInfoAcquisitionPreferenceValue(savedIsGettingWeatherInformation);
        });
    }

    public Single<Preferences> saveGettingWeatherInformationPreferenceValue(WeatherInfoAcquisitionPreferenceValue value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_GETTING_WEATHER_INFORMATION, value.getIsChecked());
            return Single.just(mutablePreferences);
        });
    }



    // TODO:調整用(最終的に削除)
    public Single<Preferences> removeCalendarStartDayOfWeek() {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.remove(KEY_CALENDAR_START_DAY_OF_WEEK);
            return Single.just(mutablePreferences);
        });
    }

}
