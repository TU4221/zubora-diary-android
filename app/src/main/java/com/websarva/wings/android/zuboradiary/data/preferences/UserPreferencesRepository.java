package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.Preferences;

import java.util.Objects;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferencesRepository {

    private final UserPreferences userPreferences;

    public UserPreferencesRepository(UserPreferences userPreferences) {
        Objects.requireNonNull(userPreferences);

        this.userPreferences = userPreferences;
    }

    public Flowable<ThemeColorPreferenceValue> loadThemeColorSettingValue() {
        return userPreferences.loadThemeColorPreferenceValue();
    }

    public Single<Preferences> saveThemeColorPreferenceValue(ThemeColorPreferenceValue value) {
        return userPreferences.saveThemeColorPreferenceValue(value);
    }

    public Flowable<CalendarStartDayOfWeekPreferenceValue> loadCalendarStartDayOfWeekPreferenceValue() {
        return userPreferences.loadCalendarStartDayOfWeekPreferenceValue();
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreferenceValue(CalendarStartDayOfWeekPreferenceValue value) {
        return userPreferences.saveCalendarStartDayOfWeekPreferenceValue(value);
    }

    public Flowable<ReminderNotificationPreferenceValue> loadReminderNotificationPreferenceValue() {
        return userPreferences.loadReminderNotificationPreferenceValue();
    }

    public Single<Preferences> saveReminderNotificationPreferenceValue(ReminderNotificationPreferenceValue value) {
        return userPreferences.saveReminderNotificationPreferenceValue(value);
    }

    public Flowable<PassCodeLockPreferenceValue> loadPasscodeLockPreferenceValue() {
        return userPreferences.loadPasscodeLockPreferenceValue();
    }

    public Single<Preferences> savePasscodeLockPreferenceValue(PassCodeLockPreferenceValue value) {
        return userPreferences.savePasscodeLockPreferenceValue(value);
    }

    public Flowable<WeatherInfoAcquisitionPreferenceValue> loadWeatherInfoAcquisitionPreferenceValue() {
        return userPreferences.loadWeatherInfoAcquisitionPreferenceValue();
    }

    public Single<Preferences> saveWeatherInfoAcquisitionPreferenceValue(WeatherInfoAcquisitionPreferenceValue value) {
        return userPreferences.saveWeatherInfoAcquisitionPreferenceValue(value);
    }
}
