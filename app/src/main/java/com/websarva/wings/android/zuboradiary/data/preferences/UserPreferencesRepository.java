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

    public Flowable<ThemeColorPreference> loadThemeColorPreference() {
        return userPreferences.loadThemeColorPreference();
    }

    public Single<Preferences> saveThemeColorPreference(ThemeColorPreference preference) {
        return userPreferences.saveThemeColorPreference(preference);
    }

    public Flowable<CalendarStartDayOfWeekPreference> loadCalendarStartDayOfWeekPreference() {
        return userPreferences.loadCalendarStartDayOfWeekPreference();
    }

    public Single<Preferences> saveCalendarStartDayOfWeekPreference(CalendarStartDayOfWeekPreference preference) {
        return userPreferences.saveCalendarStartDayOfWeekPreference(preference);
    }

    public Flowable<ReminderNotificationPreference> loadReminderNotificationPreference() {
        return userPreferences.loadReminderNotificationPreference();
    }

    public Single<Preferences> saveReminderNotificationPreference(ReminderNotificationPreference preference) {
        return userPreferences.saveReminderNotificationPreference(preference);
    }

    public Flowable<PassCodeLockPreference> loadPasscodeLockPreference() {
        return userPreferences.loadPasscodeLockPreference();
    }

    public Single<Preferences> savePasscodeLockPreference(PassCodeLockPreference preference) {
        return userPreferences.savePasscodeLockPreference(preference);
    }

    public Flowable<WeatherInfoAcquisitionPreference> loadWeatherInfoAcquisitionPreference() {
        return userPreferences.loadWeatherInfoAcquisitionPreference();
    }

    public Single<Preferences> saveWeatherInfoAcquisitionPreference(WeatherInfoAcquisitionPreference preference) {
        return userPreferences.saveWeatherInfoAcquisitionPreference(preference);
    }

    public Single<Preferences> initializeAllPreferences() {
        return userPreferences.initializeAllPreferences();
    }
}
