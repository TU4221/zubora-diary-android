package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.IOException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.SettingsFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workerRepository: WorkerRepository,
    private val diaryRepository: DiaryRepository,
    private val uriRepository: UriRepository
) : BaseViewModel() {

    // HACK:SavedStateHandleを使用する理由
    //      プロセスキルでアプリを再起動した時、ActivityのBinding処理とは関係なしにFragmentの処理が始まり、
    //      Preferencesの格納値の読込が完了する前(onCreateView()の時点)にViewの設定で設定値を参照してしまい、
    //      例外が発生してしまう問題が発生。
    //      通常のアプリ起動だとPreferencesの格納値の読込が完了してからActivityのBinding処理を行うようにしている為、
    //      このような問題は発生しない。各フラグメントにPreferencesの読込完了条件をいれるとコルーチンを使用する等の
    //      複雑な処理になるため、SavedStateHandleで対応する。
    companion object {
        private const val SAVED_THEME_COLOR_STATE_KEY = "savedThemeColorState"
        private const val SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY = "savedCalendarStartDayOfWeekState"
    }

    private fun <T> Flow<T>.stateIn(initialValue: T): StateFlow<T> {
        return this.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)
    }

    private val logTag = createLogTag()

    // MEMO:StateFlow型設定値変数の値ははPreferencesDatastoreの値のみを代入したいので、
    //      代入されるまでの間(初回設定値読込中)はnullとする。
    lateinit var themeColor: StateFlow<ThemeColor?>
        private set
    private lateinit var isThemeColorNotNull: StateFlow<Boolean>

    lateinit var calendarStartDayOfWeek: StateFlow<DayOfWeek?>
        private set
    private lateinit var isCalendarStartDayOfWeekNotNull: StateFlow<Boolean>

    lateinit var isCheckedReminderNotification: StateFlow<Boolean?>
        private set
    lateinit var reminderNotificationTime: StateFlow<LocalTime?>
        private set
    private lateinit var isReminderNotificationNotNull: StateFlow<Boolean>

    lateinit var isCheckedPasscodeLock: StateFlow<Boolean?>
        private set
    private lateinit var passcode: StateFlow<String?>
    private lateinit var isPasscodeLockNotNull: StateFlow<Boolean>

    lateinit var isCheckedWeatherInfoAcquisition: StateFlow<Boolean?>
        private set
    private lateinit var isWeatherInfoAcquisitionNotNull: StateFlow<Boolean>

    lateinit var isAllSettingsNotNull: StateFlow<Boolean>

    private val initialScrollPositionY = 0
    var scrollPositionY = initialScrollPositionY

    // Fragment処理
    private val _fragmentAction = MutableSharedFlow<FragmentAction>()
    val fragmentAction
        get() = _fragmentAction.asSharedFlow()

    init {
        setUpPreferencesValueLoading()
    }

    override fun initialize() {
        super.initialize()
        setUpPreferencesValueLoading()
        scrollPositionY = initialScrollPositionY
    }

    private fun setUpPreferencesValueLoading() {
        val allPreferences: Flow<AllPreferences>
        try {
            allPreferences = userPreferencesRepository.loadAllPreferences()
        } catch (e: Throwable) {
            Log.e(logTag, "アプリ設定値読込_失敗", e)
            addSettingLoadingErrorMessage()
            return
        }
        setUpThemeColorPreferenceValueLoading(allPreferences)
        setUpCalendarStartDayOfWeekPreferenceValueLoading(allPreferences)
        setUpReminderNotificationPreferenceValueLoading(allPreferences)
        setUpPasscodeLockPreferenceValueLoading(allPreferences)
        setUpWeatherInfoAcquisitionPreferenceValueLoading(allPreferences)
        setUpIsAllSettingsNotNull()
    }

    private fun setUpThemeColorPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        val initialValue = handle.get<ThemeColor>(SAVED_THEME_COLOR_STATE_KEY)
        themeColor =
            preferences.map { value ->
                value.themeColorPreference.themeColor
            }.onEach { value: ThemeColor ->
                handle[SAVED_THEME_COLOR_STATE_KEY] = value
            }.stateIn(initialValue)

        isThemeColorNotNull =
            themeColor.map { value ->
                value != null
            }.stateIn(false)
    }

    private fun setUpCalendarStartDayOfWeekPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        val initialValue = handle.get<DayOfWeek>(SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY)
        calendarStartDayOfWeek =
            preferences.map { value ->
                value.calendarStartDayOfWeekPreference.dayOfWeek
            }.onEach { value: DayOfWeek ->
                handle[SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY] = value
            }.stateIn(initialValue)

        isCalendarStartDayOfWeekNotNull =
            calendarStartDayOfWeek.map { value ->
                value != null
            }.stateIn(false)
    }

    private fun setUpReminderNotificationPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        isCheckedReminderNotification =
            preferences.map { value ->
                value.reminderNotificationPreference.isChecked
            }.stateIn(null )

        reminderNotificationTime =
            preferences.map { value ->
                value.reminderNotificationPreference.notificationLocalTime
            }.stateIn(null)

        isReminderNotificationNotNull =
            combine(isCheckedReminderNotification, reminderNotificationTime) {
                    isCheckedReminderNotification, reminderNotificationTime ->
                if (isCheckedReminderNotification == null) return@combine false
                return@combine if (isCheckedReminderNotification) {
                    reminderNotificationTime != null
                } else {
                    true
                }
            }.stateIn(false)
    }

    private fun setUpPasscodeLockPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        isCheckedPasscodeLock =
            preferences.map { value ->
                value.passcodeLockPreference.isChecked
            }.stateIn(null)

        passcode =
            preferences.map { value ->
                value.passcodeLockPreference.passCode
            }.stateIn(null)

        isPasscodeLockNotNull =
            combine(isCheckedPasscodeLock, passcode) {
                    isCheckedPasscodeLock, passcode ->
                if (isCheckedPasscodeLock == null) return@combine false
                return@combine if (isCheckedPasscodeLock) {
                    passcode != null
                } else {
                    true
                }
            }.stateIn(false)
    }

    private fun setUpWeatherInfoAcquisitionPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        isCheckedWeatherInfoAcquisition =
            preferences.map { value ->
                value.weatherInfoAcquisitionPreference.isChecked
            }.stateIn(null)

        isWeatherInfoAcquisitionNotNull =
            isCheckedWeatherInfoAcquisition.map { value ->
                value != null
            }.stateIn(false)
    }

    private fun setUpIsAllSettingsNotNull() {
        isAllSettingsNotNull =
            combine(
                isThemeColorNotNull,
                isCalendarStartDayOfWeekNotNull,
                isReminderNotificationNotNull,
                isPasscodeLockNotNull,
                isWeatherInfoAcquisitionNotNull
            ) {
                    isThemeColorNotNull,
                    isCalendarStartDayOfWeekNotNull,
                    isReminderNotificationNotNull,
                    isPasscodeLockNotNull,
                    isWeatherInfoAcquisitionNotNull ->
                return@combine isThemeColorNotNull
                        && isCalendarStartDayOfWeekNotNull
                        && isReminderNotificationNotNull
                        && isPasscodeLockNotNull
                        && isWeatherInfoAcquisitionNotNull
            }.stateIn(false)
    }

    private fun addSettingLoadingErrorMessage() {
        if (equalLastAppMessage(SettingsAppMessage.SettingLoadingFailure)) return  // 設定更新エラー通知の重複防止

        addAppMessage(SettingsAppMessage.SettingLoadingFailure)
    }

    // ViewClicked処理
    fun onThemeColorSettingButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClicked() {
        viewModelScope.launch {
            val dayOfWeek = calendarStartDayOfWeek.requireValue()
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    fun onReminderNotificationSettingCheckedChanged(isChecked: Boolean) {
        viewModelScope.launch {
            // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
            // 初回読込時は処理不要のため下記条件追加。
            val settingValue = isCheckedReminderNotification.requireValue()
            if (isChecked == settingValue) return@launch

            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _fragmentAction.emit(
                        SettingsFragmentAction.CheckPostNotificationsPermission
                    )
                } else {
                    _fragmentAction.emit(
                        SettingsFragmentAction.NavigateReminderNotificationTimePickerDialog
                    )
                }
            } else {
                saveReminderNotificationInvalid()
            }
        }

    }

    fun onPasscodeLockSettingCheckedChanged(isChecked: Boolean) {
        viewModelScope.launch {
            // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
            // 初回読込時は処理不要のため下記条件追加。
            val settingValue = isCheckedPasscodeLock.requireValue()
            if (isChecked == settingValue) return@launch

            savePasscodeLock(isChecked)
        }
    }

    fun onWeatherInfoAcquisitionSettingCheckedChanged(isChecked: Boolean) {
        viewModelScope.launch {
            // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
            // 初回読込時は処理不要のため下記条件追加。
            val settingValue = isCheckedWeatherInfoAcquisition.requireValue()
            if (isChecked == settingValue) return@launch

            if (isChecked) {
                _fragmentAction.emit(
                    SettingsFragmentAction.CheckAccessLocationPermission
                )
            } else {
                saveWeatherInfoAcquisition(false)
            }
        }
    }

    fun onAllDiariesDeleteButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClicked() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.NavigateOpenSourceLicensesFragment
            )
        }
    }

    // Fragmentからの結果受取処理
    fun onThemeColorSettingDialogResultReceived(result: DialogResult<ThemeColor>) {
        when (result) {
            is DialogResult.Positive<ThemeColor> -> {
                onThemeColorSettingDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onThemeColorSettingDialogPositiveResultReceived(themeColor: ThemeColor) {
        viewModelScope.launch {
            saveThemeColor(themeColor)
        }
    }

    fun onCalendarStartDayOfWeekSettingDialogResultReceived(result: DialogResult<DayOfWeek>) {
        when (result) {
            is DialogResult.Positive<DayOfWeek> -> {
                onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            saveCalendarStartDayOfWeek(dayOfWeek)
        }
    }

    fun onReminderNotificationSettingDialogResultReceived(result: DialogResult<LocalTime>) {
        when (result) {
            is DialogResult.Positive<LocalTime> -> {
                onReminderNotificationSettingDialogPositiveResultReceived(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                onReminderNotificationSettingDialogNegativeResultReceived()
                return
            }
        }
    }

    private fun onReminderNotificationSettingDialogPositiveResultReceived(time: LocalTime) {
        viewModelScope.launch {
            saveReminderNotificationValid(time)
        }
    }

    private fun onReminderNotificationSettingDialogNegativeResultReceived() {
        viewModelScope.launch {
            _fragmentAction.emit(
                SettingsFragmentAction.TurnOffReminderNotificationSettingSwitch
            )
        }
    }

    fun onAllDiariesDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onAllDiariesDeleteDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun onAllDiariesDeleteDialogPositiveResultReceived() {
        viewModelScope.launch {
            val isSuccessful = deleteAllDiaries()
            if (!isSuccessful) return@launch

            uriRepository.releaseAllPersistablePermission()
        }
    }

    fun onAllSettingsInitializationDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onAllSettingsInitializationDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun onAllSettingsInitializationDialogPositiveResultReceived() {
        viewModelScope.launch {
            initializeAllSettings()
        }
    }

    fun onAllDataDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                onAllDataDeleteDialogPositiveResultReceived()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun onAllDataDeleteDialogPositiveResultReceived() {
        viewModelScope.launch {
            val isSuccessful = deleteAllData()
            if (!isSuccessful) return@launch

            uriRepository.releaseAllPersistablePermission()
        }
    }

    // Permission処理
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onPostNotificationsPermissionChecked(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                _fragmentAction.emit(
                    SettingsFragmentAction.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.CheckShouldShowRequestPostNotificationsPermissionRationale
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onShouldShowRequestPostNotificationsPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                _fragmentAction.emit(
                    SettingsFragmentAction.ShowRequestPostNotificationsPermissionRationale
                )
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.TurnOffReminderNotificationSettingSwitch
                )
                _fragmentAction.emit(
                    SettingsFragmentAction.NavigateNotificationPermissionDialog
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                _fragmentAction.emit(
                    SettingsFragmentAction.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.TurnOffReminderNotificationSettingSwitch
                )
            }
        }
    }

    fun onAccessLocationPermissionChecked(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                saveWeatherInfoAcquisition(true)
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.CheckShouldShowRequestAccessLocationPermissionRationale
                )
            }
        }
    }

    fun onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                _fragmentAction.emit(
                    SettingsFragmentAction.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.TurnOffWeatherInfoAcquisitionSettingSwitch
                )
                _fragmentAction.emit(
                    SettingsFragmentAction.NavigateLocationPermissionDialog
                )
            }
        }
    }

    fun onRequestAccessLocationPermissionRationaleResultReceived(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                saveWeatherInfoAcquisition(true)
            } else {
                _fragmentAction.emit(
                    SettingsFragmentAction.TurnOffWeatherInfoAcquisitionSettingSwitch
                )
            }
        }
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    fun onInitializeReminderNotificationSettingFromPermission(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) return@launch

            saveReminderNotificationInvalid()
        }
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    fun onInitializeWeatherInfoAcquisitionSettingFromPermission(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) return@launch

            saveWeatherInfoAcquisition(false)
        }
    }

    private suspend fun saveThemeColor(value: ThemeColor): Boolean {
        val preferenceValue = ThemeColorPreference(value)
        return updateSettingValue{
            userPreferencesRepository.saveThemeColorPreference(preferenceValue)
        }
    }

    private suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek): Boolean {
        val preferenceValue =
            CalendarStartDayOfWeekPreference(value)
        return updateSettingValue{
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        }
    }

    private suspend fun saveReminderNotificationValid(value: LocalTime): Boolean {
        val preferenceValue =
            ReminderNotificationPreference(true, value)
        return updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.registerReminderNotificationWorker(value)
        }
    }

    private suspend fun saveReminderNotificationInvalid(): Boolean {
        val preferenceValue =
            ReminderNotificationPreference(false, null as LocalTime?)
        return updateSettingValue{
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.cancelReminderNotificationWorker()
        }
    }

    private suspend fun savePasscodeLock(value: Boolean): Boolean {
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

    private suspend fun saveWeatherInfoAcquisition(value: Boolean): Boolean {
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

    private suspend fun deleteAllDiaries(): Boolean {
        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: Exception) {
            Log.e(logTag, "全日記削除_失敗", e)
            addAppMessage(SettingsAppMessage.AllDiaryDeleteFailure)
            return false
        }
        return true
    }

    private suspend fun initializeAllSettings(): Boolean {
        return updateSettingValue{
            userPreferencesRepository.initializeAllPreferences()
        }
    }

    private suspend fun deleteAllData(): Boolean {
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
