package com.websarva.wings.android.zuboradiary.data.settings;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;

import java.time.DayOfWeek;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class SettingsRepository {
    private UserPreferences preferences;
    public SettingsRepository(Context context) {
        this.preferences = UserPreferences.getInstance(context);
    }

    public Flowable<Integer> loadThemeColorNumber() {
        return this.preferences.loadThemeColorNumber();
    }

    public Flowable<String> loadThemeColorName() {
        return this.preferences.loadThemeColorName();
    }

    public Single<Preferences> saveThemeColor(ThemeColors value) {
        return this.preferences.saveThemeColor(value);
    }

    public Flowable<String> loadCalendarStartDayOfWeekName() {
        return this.preferences.loadCalendarStartDayOfWeekName();
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.preferences.saveCalendarStartDayOfWeek(value);
    }

    public Flowable<Boolean> loadIsReminderNotification() {
        return this.preferences.loadIsReminderNotification();
    }

    public Single<Preferences> saveIsReminderNotification(boolean value) {
        return this.preferences.saveIsReminderNotification(value);
    }

    public Flowable<String> loadReminderNotificationTime() {
        return this.preferences.loadReminderNotificationTime();
    }

    public Single<Preferences> saveReminderNotificationTime(int hourValue, int minuteValue) {
        return this.preferences.saveReminderNotificationTime(hourValue, minuteValue);
    }

    public Flowable<Boolean> loadIsPasscodeLock() {
        return this.preferences.loadIsPasscodeLock();
    }

    public Single<Preferences> saveIsPasscodeLock(boolean value) {
        return this.preferences.saveIsPasscodeLock(value);
    }

    public Flowable<Integer> loadPasscode() {
        return this.preferences.loadPasscode();
    }

    public Single<Preferences> savePasscode(int value) {
        return this.preferences.savePasscode(value);
    }

    public Flowable<Boolean> loadIsGettingWeatherInformation() {
        return this.preferences.loadIsGettingWeatherInformation();
    }

    public Single<Preferences> saveIsGettingWeatherInformation(boolean value) {
        return this.preferences.saveIsGettingWeatherInformation(value);
    }
}
