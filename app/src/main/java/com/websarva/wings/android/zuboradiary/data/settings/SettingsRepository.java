package com.websarva.wings.android.zuboradiary.data.settings;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.Preferences;

import java.time.DayOfWeek;
import java.time.LocalTime;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class SettingsRepository {
    private UserPreferences userPreferences;
    public SettingsRepository(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public Flowable<Integer> loadThemeColorNumber() {
        return this.userPreferences.loadThemeColorNumber();
    }

    public Flowable<String> loadThemeColorName() {
        return this.userPreferences.loadThemeColorName();
    }

    public Single<Preferences> saveThemeColor(ThemeColors value) {
        return this.userPreferences.saveThemeColor(value);
    }

    public Flowable<String> loadCalendarStartDayOfWeekName() {
        return this.userPreferences.loadCalendarStartDayOfWeekName();
    }

    public Flowable<Integer> loadCalendarStartDayOfWeekNumber() {
        return this.userPreferences.loadCalendarStartDayOfWeekNumber();
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.userPreferences.saveCalendarStartDayOfWeek(value);
    }

    public Flowable<Boolean> loadIsReminderNotification() {
        return this.userPreferences.loadIsReminderNotification();
    }

    public Single<Preferences> saveIsReminderNotification(boolean value) {
        return this.userPreferences.saveIsReminderNotification(value);
    }

    public Flowable<String> loadReminderNotificationTime() {
        return this.userPreferences.loadReminderNotificationTime();
    }

    public Single<Preferences> saveReminderNotificationTime(@NonNull LocalTime localTime) {
        return this.userPreferences.saveReminderNotificationTime(localTime);
    }

    public Flowable<Boolean> loadIsPasscodeLock() {
        return this.userPreferences.loadIsPasscodeLock();
    }

    public Single<Preferences> saveIsPasscodeLock(boolean value) {
        return this.userPreferences.saveIsPasscodeLock(value);
    }

    public Flowable<Integer> loadPasscode() {
        return this.userPreferences.loadPasscode();
    }

    public Single<Preferences> savePasscode(int value) {
        return this.userPreferences.savePasscode(value);
    }

    public Flowable<Boolean> loadIsGettingWeatherInformation() {
        return this.userPreferences.loadIsGettingWeatherInformation();
    }

    public Single<Preferences> saveIsGettingWeatherInformation(boolean value) {
        return this.userPreferences.saveIsGettingWeatherInformation(value);
    }
}
