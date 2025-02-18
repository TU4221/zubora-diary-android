package com.websarva.wings.android.zuboradiary.ui.settings

import android.util.Log
import androidx.datastore.core.IOException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    // MEMO:MutableLiveDataに値セットするまでFlowによるラグが発生する可能性があるためnull許容型とする。
    //      これにより、Observerの引数がnull許容型となりnull時の処理ができる。
    lateinit var themeColor: LiveData<ThemeColor?>
    lateinit var calendarStartDayOfWeek: LiveData<DayOfWeek?>
    lateinit var isCheckedReminderNotification: LiveData<Boolean?>
    lateinit var reminderNotificationTime: LiveData<LocalTime?>
    lateinit var isCheckedPasscodeLock: LiveData<Boolean?>
    private lateinit var passcode: LiveData<String?>
    lateinit var isCheckedWeatherInfoAcquisition: LiveData<Boolean?>

    private val _geoCoordinates = MutableLiveData<GeoCoordinates?>()
    val geoCoordinates: LiveData<GeoCoordinates?>
        get() = _geoCoordinates

    val hasUpdatedGeoCoordinates
        get() = _geoCoordinates.value != null

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
        loadSettingValue {
            themeColor =
                userPreferencesRepository.loadThemeColorPreference()
                    .map { preference ->
                        preference.themeColor
                    }
                    .asLiveData()
        }
    }

    private fun setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        loadSettingValue {
            calendarStartDayOfWeek =
                userPreferencesRepository.loadCalendarStartDayOfWeekPreference()
                    .map { preference ->
                        preference.dayOfWeek
                    }
                    .asLiveData()
        }
    }

    private fun setUpReminderNotificationPreferenceValueLoading() {
        loadSettingValue {
            isCheckedReminderNotification =
                userPreferencesRepository.loadReminderNotificationPreference()
                    .map { preference ->
                        preference.isChecked
                    }
                    .asLiveData()
            reminderNotificationTime =
                userPreferencesRepository.loadReminderNotificationPreference()
                    .map { preference ->
                        preference.notificationLocalTime
                    }
                    .asLiveData()
        }
    }

    private fun setUpPasscodeLockPreferenceValueLoading() {
        loadSettingValue {
            isCheckedPasscodeLock =
                userPreferencesRepository.loadPasscodeLockPreference()
                    .map { preference ->
                        preference.isChecked
                    }
                    .asLiveData()
            passcode =
                userPreferencesRepository.loadPasscodeLockPreference()
                    .map { preference ->
                        preference.passCode
                    }
                    .asLiveData()
        }
    }

    private fun setUpWeatherInfoAcquisitionPreferenceValueLoading() {
        loadSettingValue {
            isCheckedWeatherInfoAcquisition =
                userPreferencesRepository.loadWeatherInfoAcquisitionPreference()
                    .map { preference ->
                        preference.isChecked
                    }
                    .asLiveData()
        }
    }

    private fun interface SettingLoadingProcess {
        @Throws(Throwable::class)
        fun load()
    }

    private fun loadSettingValue(
        loadingProcess: SettingLoadingProcess
    ) {
        try {
            loadingProcess.load()
        } catch (e: Throwable) {
            Log.d("Exception", "設定値読込失敗", e)
            addSettingLoadingErrorMessage()
        }
    }

    private fun addSettingLoadingErrorMessage() {
        if (equalLastAppMessage(AppMessage.SETTING_LOADING_ERROR)) return  // 設定更新エラー通知の重複防止

        addAppMessage(AppMessage.SETTING_LOADING_ERROR)
    }

    /**
     * 同期的に設定値を取得したいときに使用。
     * 早期参照により、PreferencesDataStore から LiveData へ設定値が未格納(null)の可能性があるため。
     * */
    fun loadThemeColorSettingValue(): ThemeColor {
        val themeColorValue = themeColor.value
        if (themeColorValue != null) return themeColorValue
        return runBlocking {
            userPreferencesRepository.loadThemeColorPreference().first().themeColor
        }
    }

    /**
     * 同期的に設定値を取得したいときに使用。
     * 早期参照により、PreferencesDataStore から LiveData へ設定値が未格納(null)の可能性があるため。
     * */
    fun loadCalendarStartDaySettingValue(): DayOfWeek {
        val dayOfWeekValue = calendarStartDayOfWeek.value
        if (dayOfWeekValue != null) return dayOfWeekValue
        return runBlocking {
            userPreferencesRepository.loadCalendarStartDayOfWeekPreference().first().dayOfWeek
        }
    }

    /**
     * 同期的に設定値を取得したいときに使用。
     * 早期参照により、PreferencesDataStore から LiveData へ設定値が未格納(null)の可能性があるため。
     * */
    fun loadIsCheckedReminderNotificationSetting(): Boolean {
        val value = isCheckedReminderNotification.value
        if (value != null) return value
        return runBlocking {
            userPreferencesRepository.loadReminderNotificationPreference().first().isChecked
        }
    }

    /**
     * 同期的に設定値を取得したいときに使用。
     * 早期参照により、PreferencesDataStore から LiveData へ設定値が未格納(null)の可能性があるため。
     * */
    fun loadIsCheckedWeatherInfoAcquisitionSetting(): Boolean {
        val value = isCheckedWeatherInfoAcquisition.value
        if (value != null) return value
        return runBlocking {
            userPreferencesRepository.loadWeatherInfoAcquisitionPreference().first().isChecked
        }
    }

    fun saveThemeColor(value: ThemeColor) {
        val preferenceValue = ThemeColorPreference(value)
        updateSettingValue{
            userPreferencesRepository.saveThemeColorPreference(preferenceValue)
        }
    }

    fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        val preferenceValue =
            CalendarStartDayOfWeekPreference(value)
        updateSettingValue{
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        }
    }

    fun saveReminderNotificationValid(value: LocalTime) {
        val preferenceValue =
            ReminderNotificationPreference(true, value)
        updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.registerReminderNotificationWorker(value)
        }
    }

    fun saveReminderNotificationInvalid() {
        val preferenceValue =
            ReminderNotificationPreference(false, null as LocalTime?)
        updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
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
        updateSettingValue{
            userPreferencesRepository.savePasscodeLockPreference(preferenceValue)
        }
    }

    fun saveWeatherInfoAcquisition(value: Boolean) {
        val preferenceValue =
            WeatherInfoAcquisitionPreference(value)
        updateSettingValue{
            userPreferencesRepository.saveWeatherInfoAcquisitionPreference(preferenceValue)
        }
    }

    private fun interface SettingUpdateProcess {
        @Throws(
            IOException::class,
            Exception::class
        )
        suspend fun update()
    }

    private fun updateSettingValue(
        updateProcess: SettingUpdateProcess
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateProcess.update()
            } catch (e: IOException) {
                Log.d("Exception", "設定値更新失敗", e)
                addSettingUpdateErrorMessage()
            } catch (e: Exception) {
                Log.d("Exception", "設定値更新失敗", e)
                addSettingUpdateErrorMessage()
            }
        }
    }

    private fun addSettingUpdateErrorMessage() {
        if (equalLastAppMessage(AppMessage.SETTING_UPDATE_ERROR)) return  // 設定更新エラー通知の重複防止

        addAppMessage(AppMessage.SETTING_UPDATE_ERROR)
    }

    fun updateGeoCoordinates(geoCoordinates: GeoCoordinates) {
        _geoCoordinates.value = geoCoordinates
    }

    fun clearGeoCoordinates() {
        _geoCoordinates.value = null
    }

    suspend fun deleteAllDiaries(): Boolean {
        try {
            diaryRepository.deleteAllDiaries()
            return true
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }
    }

    fun initializeAllSettings() {
        updateSettingValue{
            userPreferencesRepository.initializeAllPreferences()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                diaryRepository.deleteAllData()
            } catch (e: CancellationException) {
                addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            } catch (e: ExecutionException) {
                addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            } catch (e: InterruptedException) {
                addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            }
            initializeAllSettings()
        }
    }
}
