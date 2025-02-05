package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserPreferences @Inject constructor(private val dataStore: RxDataStore<Preferences>) {

    // MEMO:初回読込は"null"が返ってくるので、その場合は初期値を返す。(他のPreferenceValueも同様)
    fun loadThemeColorPreference(): Flowable<ThemeColorPreference> {
        return dataStore.data().cache().map { preferences: Preferences ->
            val savedThemeColorNumber =
                preferences.get<Int>(ThemeColorPreference.PREFERENCES_KEY_COLOR)
                    ?: return@map ThemeColorPreference()
            ThemeColorPreference(savedThemeColorNumber)
        }
    }

    fun saveThemeColorPreference(value: ThemeColorPreference): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            value.setUpPreferences(mutablePreferences)
            Single.just(
                mutablePreferences
            )
        }
    }

    fun loadCalendarStartDayOfWeekPreference(): Flowable<CalendarStartDayOfWeekPreference> {
        return dataStore.data().map { preferences: Preferences ->
            val savedCalendarStartDayOfWeekNumber =
                preferences.get<Int>(CalendarStartDayOfWeekPreference.PREFERENCES_KEY_DAY_OF_WEEK)
                    ?: return@map CalendarStartDayOfWeekPreference()
            CalendarStartDayOfWeekPreference(savedCalendarStartDayOfWeekNumber)
        }
    }

    fun saveCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            value.setUpPreferences(mutablePreferences)
            Single.just(
                mutablePreferences
            )
        }
    }

    fun loadReminderNotificationPreference(): Flowable<ReminderNotificationPreference> {
        return dataStore.data().map { preferences: Preferences ->
            val savedIsReminderNotification =
                preferences[ReminderNotificationPreference.PREFERENCES_KEY_IS_CHECKED]
            val savedReminderNotificationTime =
                preferences[ReminderNotificationPreference.PREFERENCES_KEY_TIME]
            if (savedIsReminderNotification == null || savedReminderNotificationTime == null) {
                return@map ReminderNotificationPreference()
            }
            ReminderNotificationPreference(
                savedIsReminderNotification,
                savedReminderNotificationTime
            )
        }
    }

    fun saveReminderNotificationPreference(value: ReminderNotificationPreference): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            value.setUpPreferences(mutablePreferences)
            Single.just(
                mutablePreferences
            )
        }
    }

    fun loadPasscodeLockPreference(): Flowable<PassCodeLockPreference> {
        return dataStore.data().map { preferences: Preferences ->
            val savedIsPasscodeLock =
                preferences[PassCodeLockPreference.PREFERENCES_KEY_IS_CHECKED]
            val savedPasscode =
                preferences[PassCodeLockPreference.PREFERENCES_KEY_PASSCODE]
            if (savedIsPasscodeLock == null || savedPasscode == null) {
                return@map PassCodeLockPreference()
            }
            PassCodeLockPreference(savedIsPasscodeLock, savedPasscode)
        }
    }

    fun savePasscodeLockPreference(value: PassCodeLockPreference): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            value.setUpPreferences(mutablePreferences)
            Single.just(
                mutablePreferences
            )
        }
    }

    fun loadWeatherInfoAcquisitionPreference(): Flowable<WeatherInfoAcquisitionPreference> {
        return dataStore.data()
            .map { preferences: Preferences ->
                val savedIsGettingWeatherInformation =
                    preferences.get<Boolean>(WeatherInfoAcquisitionPreference.PREFERENCES_KEY_IS_CHECKED)
                        ?: return@map WeatherInfoAcquisitionPreference()
                WeatherInfoAcquisitionPreference(savedIsGettingWeatherInformation)
            }
    }

    fun saveWeatherInfoAcquisitionPreference(value: WeatherInfoAcquisitionPreference): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            Single.just(
                mutablePreferences.apply {
                    value.setUpPreferences(this)
                }
            )
        }
    }

    fun initializeAllPreferences(): Single<Preferences> {
        return dataStore.updateDataAsync { preferences: Preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            Single.just(
                mutablePreferences.apply {
                    ThemeColorPreference().setUpPreferences(this)
                    CalendarStartDayOfWeekPreference().setUpPreferences(this)
                    ReminderNotificationPreference().setUpPreferences(this)
                    PassCodeLockPreference().setUpPreferences(this)
                    WeatherInfoAcquisitionPreference().setUpPreferences(this)
                }
            )
        }
    }
}
