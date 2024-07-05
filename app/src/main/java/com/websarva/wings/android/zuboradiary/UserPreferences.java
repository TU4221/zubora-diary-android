package com.websarva.wings.android.zuboradiary;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.websarva.wings.android.zuboradiary.ui.settings.DayOfWeekNameResIdGetter;
import com.websarva.wings.android.zuboradiary.ui.settings.ThemeColors;

import java.time.DayOfWeek;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferences {
    private static UserPreferences instance;
    private Context context;
    private RxDataStore<Preferences> dataStore;
    private final Preferences.Key<Integer> KEY_THEME_COLOR = PreferencesKeys.intKey("theme_color");
    private final Preferences.Key<Integer> KEY_CALENDAR_START_DAY_OF_WEEK =
                                        PreferencesKeys.intKey("calendar_start_day_of_week");
    private final String KEY_REMINDER_NOTIFICATION = "reminder_notification";
    private final String KEY_REMINDER_NOTIFICATION_TIME = "reminder_notification_time";
    private final String KEY_PASSCODE_LOCK = "passcode_lock";
    private final String KEY_PASSCODE = "passcode";
    private final String KEY_GET_WEATHER_INFORMATION = "getting_weather_information";
    private UserPreferences(Context context) {
        this.context = context;
        this.dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
    }

    public static UserPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new UserPreferences(context);
        }
        return  instance;
    }

    public Flowable<Integer> loadThemeColorNumber() {
        return this.dataStore.data().map(preferences -> preferences.get(KEY_THEME_COLOR));
    }

    public Flowable<String> loadThemeColorName() {
        return this.dataStore.data().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(KEY_THEME_COLOR);
            if (savedThemeColorNumber != null) {
                for (ThemeColors themeColor: ThemeColors.values()) {
                    if (themeColor.getThemeColorNumber() == savedThemeColorNumber) {
                        int savedThemeColorNameResId = themeColor.getThemeColorNameResId();
                        return this.context.getString(savedThemeColorNameResId);
                    }
                }
            }
            return this.context.getString(R.string.fragment_settings_item_value_not_set); // TODO:未設定の場合は初期値を設定しておいて方がいいかも・・・
        });
    }

    public Flowable<ThemeColors> loadThemeColor() {
        return this.dataStore.data().map(preferences -> {
            int savedThemeColorNumber = preferences.get(KEY_THEME_COLOR);
            for (ThemeColors themeColor: ThemeColors.values()) {
                if (themeColor.getThemeColorNumber() == savedThemeColorNumber) {
                    return themeColor;
                }
            }
            return null;
        });
    }

    public Single<Preferences> saveThemeColor(ThemeColors value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_THEME_COLOR, value.getThemeColorNumber());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<String> loadCalendarStartDayOfWeekName() {
        return this.dataStore.data().map(preferences -> {
            Integer savedCalendarStartDayOfWeekNumber = preferences.get(KEY_CALENDAR_START_DAY_OF_WEEK);
            if (savedCalendarStartDayOfWeekNumber != null) {
                for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
                    if (dayOfWeek.getValue() == savedCalendarStartDayOfWeekNumber) {
                        DayOfWeekNameResIdGetter dayOfWeekNameResIdGetter = new DayOfWeekNameResIdGetter();
                        int savedCalendarStartDayOfWeekNameResId =
                                dayOfWeekNameResIdGetter.getResId(dayOfWeek);
                        return this.context.getString(savedCalendarStartDayOfWeekNameResId);
                    }
                }
            }
            return this.context.getString(R.string.fragment_settings_item_value_not_set);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_CALENDAR_START_DAY_OF_WEEK, value.getValue());
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
