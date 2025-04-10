package com.websarva.wings.android.zuboradiary.ui.settings

import android.util.Log
import androidx.datastore.core.IOException
import androidx.lifecycle.viewModelScope
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
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.appmessage.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workerRepository: WorkerRepository,
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    // MEMO:StateFlow型設定値変数の値ははPreferencesDatastoreの値のみを代入したいので、
    //      代入されるまでの間(初回設定値読込中)はnullとする。
    lateinit var themeColor: StateFlow<ThemeColor?>
        private set
    lateinit var calendarStartDayOfWeek: StateFlow<DayOfWeek?>
        private set
    lateinit var isCheckedReminderNotification: StateFlow<Boolean?>
        private set
    lateinit var reminderNotificationTime: StateFlow<LocalTime?>
        private set
    lateinit var isCheckedPasscodeLock: StateFlow<Boolean?>
        private set
    private lateinit var passcode: StateFlow<String?>
    lateinit var isCheckedWeatherInfoAcquisition: StateFlow<Boolean?>
        private set

    private val _isAllSettingsNotNull = MutableStateFlow(false)
    val isAllSettingsNotNull
        get() = _isAllSettingsNotNull.asStateFlow()

    private val initialGeoCoordinates = null
    private val _geoCoordinates = MutableStateFlow<GeoCoordinates?>(initialGeoCoordinates)
    val geoCoordinates
        get() = _geoCoordinates.asStateFlow()

    val hasUpdatedGeoCoordinates
        get() = _geoCoordinates.value != null

    private val initialScrollPositionY = 0
    var scrollPositionY = initialScrollPositionY

    init {
        setUpThemeColorPreferenceValueLoading()
        setUpCalendarStartDayOfWeekPreferenceValueLoading()
        setUpReminderNotificationPreferenceValueLoading()
        setUpPasscodeLockPreferenceValueLoading()
        setUpWeatherInfoAcquisitionPreferenceValueLoading()
    }

    override fun initialize() {
        super.initialize()
        setUpThemeColorPreferenceValueLoading()
        setUpCalendarStartDayOfWeekPreferenceValueLoading()
        setUpReminderNotificationPreferenceValueLoading()
        setUpPasscodeLockPreferenceValueLoading()
        setUpWeatherInfoAcquisitionPreferenceValueLoading()
        clearGeoCoordinates()
        scrollPositionY = initialScrollPositionY
    }

    private fun setUpThemeColorPreferenceValueLoading() {
        loadSettingValue {
            themeColor =
                userPreferencesRepository.loadThemeColorPreference()
                    .map { preference ->
                        preference.themeColor
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(themeColor)
    }

    private fun setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        loadSettingValue {
            calendarStartDayOfWeek =
                userPreferencesRepository.loadCalendarStartDayOfWeekPreference()
                    .map { preference ->
                        preference.dayOfWeek
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(calendarStartDayOfWeek)
    }

    private fun setUpReminderNotificationPreferenceValueLoading() {
        loadSettingValue {
            isCheckedReminderNotification =
                userPreferencesRepository.loadReminderNotificationPreference()
                    .map { preference ->
                        preference.isChecked
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(isCheckedReminderNotification)

        loadSettingValue {
            reminderNotificationTime =
                userPreferencesRepository.loadReminderNotificationPreference()
                    .map { preference ->
                        preference.notificationLocalTime
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(reminderNotificationTime)
    }

    private fun setUpPasscodeLockPreferenceValueLoading() {
        loadSettingValue {
            isCheckedPasscodeLock =
                userPreferencesRepository.loadPasscodeLockPreference()
                    .map { preference ->
                        preference.isChecked
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(isCheckedPasscodeLock)

        loadSettingValue {
            passcode =
                userPreferencesRepository.loadPasscodeLockPreference()
                    .map { preference ->
                        preference.passCode
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(passcode)
    }

    private fun setUpWeatherInfoAcquisitionPreferenceValueLoading() {
        loadSettingValue {
            isCheckedWeatherInfoAcquisition =
                userPreferencesRepository.loadWeatherInfoAcquisitionPreference()
                    .map { preference ->
                        preference.isChecked
                    }.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        null
                    )
        }
        checkSettingNotNull(isCheckedWeatherInfoAcquisition)
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
            Log.e(logTag, "アプリ設定値読込_失敗", e)
            addSettingLoadingErrorMessage()
        }
    }

    private fun <T> checkSettingNotNull(setting: StateFlow<T?>) {
        viewModelScope.launch(Dispatchers.IO) {
            setting.collect { value: T? ->
                if (_isAllSettingsNotNull.value) return@collect
                if (value == null) return@collect

                checkAllSettingsNotNull()
            }
        }
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

    private fun addSettingLoadingErrorMessage() {
        if (equalLastAppMessage(SettingsAppMessage.SettingLoadingFailure)) return  // 設定更新エラー通知の重複防止

        addAppMessage(SettingsAppMessage.SettingLoadingFailure)
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
            Log.e(logTag, "アプリ設定値更新_失敗", e)
            addSettingUpdateErrorMessage()
            return false
        } catch (e: Exception) {
            Log.e(logTag, "アプリ設定値更新_失敗", e)
            addSettingUpdateErrorMessage()
            return false
        }
        return true
    }

    private fun addSettingUpdateErrorMessage() {
        if (equalLastAppMessage(SettingsAppMessage.SettingUpdateFailure)) return  // 設定更新エラー通知の重複防止

        addAppMessage(SettingsAppMessage.SettingUpdateFailure)
    }

    fun updateGeoCoordinates(geoCoordinates: GeoCoordinates) {
        _geoCoordinates.value = geoCoordinates
    }

    fun clearGeoCoordinates() {
        _geoCoordinates.value = initialGeoCoordinates
    }

    suspend fun deleteAllDiaries(): Boolean {
        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: Exception) {
            Log.e(logTag, "全日記削除_失敗", e)
            addAppMessage(SettingsAppMessage.AllDiaryDeleteFailure)
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
            Log.e(logTag, "アプリ全データ削除_失敗", e)
            addAppMessage(SettingsAppMessage.AllDataDeleteFailure)
            return false
        }
        return initializeAllSettings()
    }
}
