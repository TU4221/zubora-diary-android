package com.websarva.wings.android.zuboradiary.data.settings;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.websarva.wings.android.zuboradiary.DateConverter;

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
    private final Preferences.Key<Boolean> KEY_IS_REMINDER_NOTIFICATION =
                                         PreferencesKeys.booleanKey("is_reminder_notification");
    private final Preferences.Key<String> KEY_REMINDER_NOTIFICATION_TIME =
                                    PreferencesKeys.stringKey("reminder_notification_time");
    private final Preferences.Key<Boolean> KEY_IS_PASSCODE_LOCK =
                                                PreferencesKeys.booleanKey("is_passcode_lock");
    private final Preferences.Key<Integer> KEY_PASSCODE = PreferencesKeys.intKey("passcode");
    private final Preferences.Key<Boolean> KEY_IS_GETTING_WEATHER_INFORMATION =
                                    PreferencesKeys.booleanKey("is_getting_weather_information");
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
        return this.dataStore.data().cache().map(preferences -> {
            Integer savedThemeColorNumber = preferences.get(KEY_THEME_COLOR);
            int savedThemeColorNameResId = -1;
            if (savedThemeColorNumber != null) {
                for (ThemeColors themeColor: ThemeColors.values()) {
                    if (themeColor.getThemeColorNumber() == savedThemeColorNumber) {
                        savedThemeColorNameResId = themeColor.getThemeColorNameResId();
                        return this.context.getString(savedThemeColorNameResId);
                    }
                }
            } else {
                savedThemeColorNameResId = ThemeColors.values()[0].getThemeColorNameResId();
            }
            return this.context.getString(savedThemeColorNameResId);
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
            DayOfWeekNameResIdGetter dayOfWeekNameResIdGetter = new DayOfWeekNameResIdGetter();
            int savedCalendarStartDayOfWeekNameResId = -1;
            if (savedCalendarStartDayOfWeekNumber != null) {
                for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
                    if (dayOfWeek.getValue() == savedCalendarStartDayOfWeekNumber) {
                        savedCalendarStartDayOfWeekNameResId =
                                dayOfWeekNameResIdGetter.getResId(dayOfWeek);
                    }
                }
            } else {
                savedCalendarStartDayOfWeekNameResId =
                        dayOfWeekNameResIdGetter.getResId(DayOfWeek.SUNDAY);
            }
            return this.context.getString(savedCalendarStartDayOfWeekNameResId);
        });
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_CALENDAR_START_DAY_OF_WEEK, value.getValue());
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<Boolean> loadIsReminderNotification() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsReminderNotification = preferences.get(KEY_IS_REMINDER_NOTIFICATION);
            if (savedIsReminderNotification == null) {
                savedIsReminderNotification = Boolean.FALSE;
            }
            return savedIsReminderNotification;
        });
    }

    public Single<Preferences> saveIsReminderNotification(boolean value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_REMINDER_NOTIFICATION, value);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<String> loadReminderNotificationTime() {
        return this.dataStore.data().map(preferences -> {
            String savedReminderNotificationTime = preferences.get(KEY_REMINDER_NOTIFICATION_TIME);
            if (savedReminderNotificationTime == null) {
                savedReminderNotificationTime = "00:00";
            }
            return savedReminderNotificationTime;
        });
    }

    public Single<Preferences> saveReminderNotificationTime(int hourValue, int minuteValue) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            String timeValue = DateConverter.toStringTimeHourMinute(hourValue, minuteValue);
            mutablePreferences.set(KEY_REMINDER_NOTIFICATION_TIME, timeValue);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<Boolean> loadIsPasscodeLock() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsPasscodeLock = preferences.get(KEY_IS_PASSCODE_LOCK);
            if (savedIsPasscodeLock == null) {
                savedIsPasscodeLock = Boolean.FALSE;
            }
            return savedIsPasscodeLock;
        });
    }

    public Single<Preferences> saveIsPasscodeLock(boolean value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_PASSCODE_LOCK, value);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<Integer> loadPasscode() {
        return this.dataStore.data().map(preferences -> {
            Integer savedPasscode = preferences.get(KEY_PASSCODE);
            if (savedPasscode == null) {
                savedPasscode = Integer.valueOf(-1);
            }
            return savedPasscode;
        });
    }

    public Single<Preferences> savePasscode(int value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_PASSCODE, value);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<Boolean> loadIsGettingWeatherInformation() {
        return this.dataStore.data().map(preferences -> {
            Boolean savedIsGettingWeatherInformation = preferences.get(KEY_IS_GETTING_WEATHER_INFORMATION);
            if (savedIsGettingWeatherInformation == null) {
                savedIsGettingWeatherInformation = Boolean.FALSE;
            }
            return savedIsGettingWeatherInformation;
        });
    }

    public Single<Preferences> saveIsGettingWeatherInformation(boolean value) {
        return this.dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_IS_GETTING_WEATHER_INFORMATION, value);
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
