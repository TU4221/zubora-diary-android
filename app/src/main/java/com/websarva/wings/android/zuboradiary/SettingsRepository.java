package com.websarva.wings.android.zuboradiary;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;

import com.websarva.wings.android.zuboradiary.ui.settings.ThemeColors;

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
}
