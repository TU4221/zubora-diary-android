package com.websarva.wings.android.zuboradiary.ui.settings

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workerRepository: WorkerRepository,
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {
    private val disposables = CompositeDisposable()

    // MEMO:MutableLiveDataに値セットするまでFlowableによるラグが発生するためnull許容型とする。
    //      これにより、Observerの引数がnull許容型となりnull時の処理ができる。
    private val _themeColor = MutableLiveData<ThemeColor?>()
    val themeColor: LiveData<ThemeColor?>
        get() = _themeColor

    private val _calendarStartDayOfWeek = MutableLiveData<DayOfWeek?>()
    val calendarStartDayOfWeek: LiveData<DayOfWeek?>
        get() = _calendarStartDayOfWeek

    private val _isCheckedReminderNotification = MutableLiveData<Boolean?>()
    val isCheckedReminderNotification: LiveData<Boolean?>
        get() = _isCheckedReminderNotification

    private val _reminderNotificationTime = MutableLiveData<LocalTime?>()
    val reminderNotificationTime: LiveData<LocalTime?>
        get() = _reminderNotificationTime

    private val _isCheckedPasscodeLock = MutableLiveData<Boolean?>()
    val isCheckedPasscodeLock: LiveData<Boolean?>
        get() = _isCheckedPasscodeLock

    private val _isCheckedWeatherInfoAcquisition = MutableLiveData<Boolean?>()
    val isCheckedWeatherInfoAcquisition: LiveData<Boolean?>
        get() = _isCheckedWeatherInfoAcquisition

    private val _geoCoordinates = MutableLiveData<GeoCoordinates?>()
    val geoCoordinates: LiveData<GeoCoordinates?>
        get() = _geoCoordinates

    val hasUpdatedGeoCoordinates
        get() = _geoCoordinates.value != null

    private lateinit var themeColorPreferenceFlowable: Flowable<ThemeColorPreference>
    private lateinit var calendarStartDayPreferenceFlowable: Flowable<CalendarStartDayOfWeekPreference>
    private lateinit var reminderNotificationPreferenceFlowable: Flowable<ReminderNotificationPreference>
    private lateinit var passCodeLockPreferenceFlowable: Flowable<PassCodeLockPreference>
    private lateinit var weatherInfoAcquisitionPreferenceFlowable: Flowable<WeatherInfoAcquisitionPreference>

    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        setUpThemeColorPreferenceValueLoading()
        setUpCalendarStartDayOfWeekPreferenceValueLoading()
        setUpReminderNotificationPreferenceValueLoading()
        setUpPasscodeLockPreferenceValueLoading()
        setUpWeatherInfoAcquisitionPreferenceValueLoading()
    }

    private fun setUpThemeColorPreferenceValueLoading() {
        themeColorPreferenceFlowable = userPreferencesRepository.loadThemeColorPreference()
        disposables.add(
            themeColorPreferenceFlowable.subscribe(
                { value: ThemeColorPreference ->
                    // HACK:一つのDataStore(UserPreferencesクラス)からFlowableを生成している為、
                    //      一つのPreferenceを更新すると他のPreferenceのFlowableにも通知される。
                    //      結果的にObserverにも通知が行き、不必要な処理が発生してしまう。
                    //      対策として下記コードを記述。(他PreferenceFlowableも同様)
                    val themeColor = _themeColor.value
                    if (themeColor != null && themeColor == value.toThemeColor()) return@subscribe
                    this._themeColor.postValue(value.toThemeColor())
                },
                { throwable: Throwable ->
                    Log.d("Exception", "テーマカラー設定値読込失敗", throwable)
                    addSettingLoadingErrorMessage()
                }
            )
        )
    }

    fun loadThemeColorSettingValue(): ThemeColor {
        val themeColorValue = _themeColor.value
        if (themeColorValue != null) return themeColorValue
        val defaultValue = ThemeColorPreference()
        return themeColorPreferenceFlowable.blockingFirst(defaultValue).toThemeColor()
    }

    private fun setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        calendarStartDayPreferenceFlowable =
            userPreferencesRepository.loadCalendarStartDayOfWeekPreference()
        disposables.add(
            calendarStartDayPreferenceFlowable.subscribe(
                { value: CalendarStartDayOfWeekPreference ->
                    val dayOfWeek = _calendarStartDayOfWeek.value
                    if (dayOfWeek != null && dayOfWeek == value.toDayOfWeek()) return@subscribe

                    val calendarStartDayOfWeek = value.toDayOfWeek()
                    this._calendarStartDayOfWeek.postValue(calendarStartDayOfWeek)
                },
                { throwable: Throwable ->
                    Log.d("Exception", "カレンダー開始曜日設定値読込失敗", throwable)
                    addSettingLoadingErrorMessage()
                }
            )
        )
    }

    fun loadCalendarStartDaySettingValue(): DayOfWeek {
        val dayOfWeekValue = _calendarStartDayOfWeek.value
        if (dayOfWeekValue != null) return dayOfWeekValue
        val defaultValue = CalendarStartDayOfWeekPreference()
        return calendarStartDayPreferenceFlowable.blockingFirst(defaultValue).toDayOfWeek()
    }

    private fun setUpReminderNotificationPreferenceValueLoading() {
        reminderNotificationPreferenceFlowable =
            userPreferencesRepository.loadReminderNotificationPreference()
        disposables.add(
            reminderNotificationPreferenceFlowable.subscribe(
                { value: ReminderNotificationPreference ->
                    val isChecked = _isCheckedReminderNotification.value
                    if (isChecked != null && isChecked == value.isChecked) return@subscribe

                    _isCheckedReminderNotification.postValue(value.isChecked)
                    _reminderNotificationTime.postValue(value.notificationLocalTime)
                },
                { throwable: Throwable ->
                    Log.d("Exception", "リマインダー通知設定値読込失敗", throwable)
                    addSettingLoadingErrorMessage()
                }
            )
        )
    }

    fun loadIsCheckedReminderNotificationSetting(): Boolean {
        val value = _isCheckedReminderNotification.value
        if (value != null) return value
        val defaultValue =
            ReminderNotificationPreference()
        return reminderNotificationPreferenceFlowable.blockingFirst(defaultValue).isChecked
    }

    private fun setUpPasscodeLockPreferenceValueLoading() {
        passCodeLockPreferenceFlowable = userPreferencesRepository.loadPasscodeLockPreference()
        disposables.add(
            passCodeLockPreferenceFlowable.subscribe(
                { value: PassCodeLockPreference ->
                    val isChecked = _isCheckedPasscodeLock.value
                    if (isChecked != null && isChecked == value.isChecked) return@subscribe
                    _isCheckedPasscodeLock.postValue(value.isChecked)
                },
                { throwable: Throwable ->
                    Log.d("Exception", "パスコード設定値読込失敗", throwable)
                    addSettingLoadingErrorMessage()
                }
            )
        )
    }

    private fun setUpWeatherInfoAcquisitionPreferenceValueLoading() {
        weatherInfoAcquisitionPreferenceFlowable =
            userPreferencesRepository.loadWeatherInfoAcquisitionPreference()
        disposables.add(
            weatherInfoAcquisitionPreferenceFlowable.subscribe(
                { value: WeatherInfoAcquisitionPreference ->
                    val isChecked = _isCheckedWeatherInfoAcquisition.value
                    if (isChecked != null && isChecked == value.isChecked) return@subscribe
                    _isCheckedWeatherInfoAcquisition.postValue(value.isChecked)
                },
                { throwable: Throwable ->
                    Log.d("Exception", "天気情報取得設定値読込失敗", throwable)
                    addSettingLoadingErrorMessage()
                }
            )
        )
    }

    fun loadIsCheckedWeatherInfoAcquisitionSetting(): Boolean {
        val value = _isCheckedWeatherInfoAcquisition.value
        if (value != null) return value
        val defaultValue =
            WeatherInfoAcquisitionPreference()
        return weatherInfoAcquisitionPreferenceFlowable.blockingFirst(defaultValue).isChecked
    }

    private fun addSettingLoadingErrorMessage() {
        if (equalLastAppMessage(AppMessage.SETTING_LOADING_ERROR)) return  // 設定更新エラー通知の重複防止

        addAppMessage(AppMessage.SETTING_LOADING_ERROR)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun saveThemeColor(value: ThemeColor) {
        val preferenceValue = ThemeColorPreference(value)
        val result = userPreferencesRepository.saveThemeColorPreference(preferenceValue)
        setUpProcessOnUpdate(result, null)
    }

    fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        val preferenceValue =
            CalendarStartDayOfWeekPreference(value)
        val result =
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        setUpProcessOnUpdate(result, null)
    }

    fun saveReminderNotificationValid(value: LocalTime) {
        val preferenceValue =
            ReminderNotificationPreference(true, value)
        val result = userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
        setUpProcessOnUpdate(result) {
            workerRepository.registerReminderNotificationWorker(value)
        }
    }

    fun saveReminderNotificationInvalid() {
        val preferenceValue =
            ReminderNotificationPreference(false, null as LocalTime?)
        val result = userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
        setUpProcessOnUpdate(result) {
            workerRepository.cancelReminderNotificationWorker()
        }
    }

    fun savePasscodeLock(value: Boolean) {
        val passcode = if (value) {
            "0000" // TODO:仮
        } else {
            ""
        }

        val preferenceValue = PassCodeLockPreference(value, passcode)
        val result = userPreferencesRepository.savePasscodeLockPreference(preferenceValue)
        setUpProcessOnUpdate(result, null)
    }

    fun saveWeatherInfoAcquisition(value: Boolean) {
        val preferenceValue =
            WeatherInfoAcquisitionPreference(value)
        val result =
            userPreferencesRepository.saveWeatherInfoAcquisitionPreference(preferenceValue)
        setUpProcessOnUpdate(result, null)
    }

    private fun interface OnSettingsUpdateCallback {
        fun onUpdateSettings()
    }

    private fun setUpProcessOnUpdate(
        result: Single<Preferences>,
        callback: OnSettingsUpdateCallback?
    ) {
        disposables.add(
            result.subscribe(
                { if (callback == null) return@subscribe
                    callback.onUpdateSettings()
                },
                { val appMessage = AppMessage.SETTING_UPDATE_ERROR
                    if (equalLastAppMessage(appMessage)) return@subscribe  // 設定更新エラー通知の重複防止
                    addAppMessage(appMessage)
                }
            )
        )
    }

    fun updateGeoCoordinates(geoCoordinates: GeoCoordinates) {
        _geoCoordinates.value = geoCoordinates
    }

    fun clearGeoCoordinates() {
        _geoCoordinates.value = null
    }

    fun deleteAllDiaries() {
        try {
            diaryRepository.deleteAllDiaries().get()
        } catch (e: CancellationException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        }
    }

    fun deleteAllSettings() {
        val result = userPreferencesRepository.initializeAllPreferences()
        setUpProcessOnUpdate(result, null)
    }

    fun deleteAllData() {
        try {
            diaryRepository.deleteAllData().get()
        } catch (e: CancellationException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        } catch (e: ExecutionException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        } catch (e: InterruptedException) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
        }
        deleteAllSettings()
    }
}
