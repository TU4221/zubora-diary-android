package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.Preferences;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class SettingsRepository {
    private UserPreferences userPreferences;
    public SettingsRepository(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public Flowable<ThemeColorPreferenceValue> loadThemeColorSettingValue() {
        return this.userPreferences.loadThemeColorPreferenceValue();
    }

    public Single<Preferences> saveThemeColorPreferenceValue(ThemeColorPreferenceValue value) {
        return this.userPreferences.saveThemeColorPreferenceValue(value);
    }

    public Flowable<CalendarStartDayOfWeekPreferenceValue> loadCalendarStartDayOfWeekPreferenceValue() {
        return this.userPreferences.loadCalendarStartDayOfWeekPreferenceValue();
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreferenceValue(CalendarStartDayOfWeekPreferenceValue value) {
        return this.userPreferences.saveCalendarStartDayOfWeekPreferenceValue(value);
    }

    public Flowable<ReminderNotificationPreferenceValue> loadReminderNotificationPreferenceValue() {
        return this.userPreferences.loadReminderNotificationPreferenceValue();
    }

    public Single<Preferences> saveReminderNotificationPreferenceValue(ReminderNotificationPreferenceValue value) {
        return this.userPreferences.saveReminderNotificationPreferenceValue(value);
    }

    public Flowable<PassCodeLockPreferenceValue> loadPasscodeLockPreferenceValue() {
        return this.userPreferences.loadPasscodeLockPreferenceValue();
    }

    public Single<Preferences> savePasscodeLockPreferenceValue(PassCodeLockPreferenceValue value) {
        return this.userPreferences.savePasscodeLockPreferenceValue(value);
    }

    public Flowable<GettingWeatherInformationPreferenceValue> loadGettingWeatherInformationPreferenceValue() {
        return this.userPreferences.loadGettingWeatherInformationPreferenceValue();
    }

    public Single<Preferences> saveGettingWeatherInformationPreferenceValue(GettingWeatherInformationPreferenceValue value) {
        return this.userPreferences.saveGettingWeatherInformationPreferenceValue(value);
    }
}
