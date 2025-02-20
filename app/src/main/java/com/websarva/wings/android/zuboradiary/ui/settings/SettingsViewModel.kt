package com.websarva.wings.android.zuboradiary.ui.settings

import android.util.Log
import androidx.datastore.core.IOException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workerRepository: WorkerRepository,
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    // MEMO:MutableLiveDataに値セットするまでFlowによるラグが発生する可能性があるためnull許容型とする。
    //      これにより、Observerの引数がnull許容型となりnull時の処理ができる。
    lateinit var themeColor: LiveData<ThemeColor>
    lateinit var calendarStartDayOfWeek: LiveData<DayOfWeek>
    // TODO:preferencesの更新をsuspend関数にしたことによりMaterialSwitchがカクつくようになった。
    //      DataBindingをやめてObserverで切替たら直る？
    lateinit var isCheckedReminderNotification: LiveData<Boolean>
    lateinit var reminderNotificationTime: LiveData<LocalTime?>
    lateinit var isCheckedPasscodeLock: LiveData<Boolean>
    private lateinit var passcode: LiveData<String?>
    lateinit var isCheckedWeatherInfoAcquisition: LiveData<Boolean>

    private val _isAllSettingsNotNull = MediatorLiveData(false)
    val isAllSettingsNotNull: LiveData<Boolean>
        get() = _isAllSettingsNotNull

    private val _geoCoordinates = MutableLiveData<GeoCoordinates?>()
    val geoCoordinates: LiveData<GeoCoordinates?>
        get() = _geoCoordinates

    val hasUpdatedGeoCoordinates
        get() = _geoCoordinates.value != null

    init {
        initialize()
        setUpIsAllSettingsNotNull()
    }

    override fun initialize() {
        initializeAppMessageList()
        setUpThemeColorPreferenceValueLoading()
        setUpCalendarStartDayOfWeekPreferenceValueLoading()
        setUpReminderNotificationPreferenceValueLoading()
        setUpPasscodeLockPreferenceValueLoading()
        setUpWeatherInfoAcquisitionPreferenceValueLoading()
    }

    private fun setUpIsAllSettingsNotNull() {
        _isAllSettingsNotNull.addSource(themeColor) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(calendarStartDayOfWeek) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(isCheckedReminderNotification) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(reminderNotificationTime) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(isCheckedPasscodeLock) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(passcode) { checkAllSettingsNotNull() }
        _isAllSettingsNotNull.addSource(isCheckedWeatherInfoAcquisition) { checkAllSettingsNotNull() }
    }

    private fun checkAllSettingsNotNull() {
        _isAllSettingsNotNull.value =
            themeColor.value != null
                    && calendarStartDayOfWeek.value != null
                    && isCheckedReminderNotification.value != null
                    && checkIsCheckedReminderNotificationNotNull()
                    && passcode.value != null
                    && isCheckedWeatherInfoAcquisition.value != null
    }

    private fun checkIsCheckedReminderNotificationNotNull(): Boolean {
        val isCheckedReminderNotification = isCheckedReminderNotification.value ?: return false
        if (isCheckedReminderNotification) {
            return reminderNotificationTime.value != null
        }

        return true
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

    suspend fun saveThemeColor(value: ThemeColor): Boolean {
        val preferenceValue = ThemeColorPreference(value)
        return updateSettingValue{
            userPreferencesRepository.saveThemeColorPreference(preferenceValue)
        }
    }

    suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek): Boolean {
        val preferenceValue =
            CalendarStartDayOfWeekPreference(value)
        return updateSettingValue{
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        }
    }

    suspend fun saveReminderNotificationValid(value: LocalTime): Boolean {
        val preferenceValue =
            ReminderNotificationPreference(true, value)
        return updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.registerReminderNotificationWorker(value)
        }
    }

    suspend fun saveReminderNotificationInvalid(): Boolean {
        val preferenceValue =
            ReminderNotificationPreference(false, null as LocalTime?)
        return updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.cancelReminderNotificationWorker()
        }
    }

    suspend fun savePasscodeLock(value: Boolean): Boolean {
        val passcode = if (value) {
            "0000" // TODO:仮
        } else {
            ""
        }

        val preferenceValue = PassCodeLockPreference(value, passcode)
        return updateSettingValue{
            userPreferencesRepository.savePasscodeLockPreference(preferenceValue)
        }
    }

    suspend fun saveWeatherInfoAcquisition(value: Boolean): Boolean {
        val preferenceValue =
            WeatherInfoAcquisitionPreference(value)
        return updateSettingValue{
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

    private suspend fun updateSettingValue(
        updateProcess: SettingUpdateProcess
    ): Boolean {
        try {
            updateProcess.update()
        } catch (e: IOException) {
            Log.d("Exception", "設定値更新失敗", e)
            addSettingUpdateErrorMessage()
            return false
        } catch (e: Exception) {
            Log.d("Exception", "設定値更新失敗", e)
            addSettingUpdateErrorMessage()
            return false
        }
        return true
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
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }
        return true
    }

    suspend fun initializeAllSettings(): Boolean {
        return updateSettingValue{
            userPreferencesRepository.initializeAllPreferences()
        }
    }

    suspend fun deleteAllData(): Boolean {
        try {
            diaryRepository.deleteAllData()
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }
        return initializeAllSettings()
    }
}
