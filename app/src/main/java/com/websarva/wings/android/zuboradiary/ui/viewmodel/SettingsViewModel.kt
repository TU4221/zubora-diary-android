package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDataUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDiariesUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchAllSettingsValueUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveCalendarStartDayOfWeekUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SavePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.state.SettingsState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val handle: SavedStateHandle, // MEMO:システムの初期化によるプロセスの終了からの復元用
    private val initializeAllSettingsUseCase: InitializeAllSettingsUseCase,
    private val fetchAllSettingsValueUseCase: FetchAllSettingsValueUseCase,
    private val saveThemeColorSettingUseCase: SaveThemeColorSettingUseCase,
    private val saveCalendarStartDayOfWeekUseCase: SaveCalendarStartDayOfWeekUseCase,
    private val saveReminderNotificationSettingUseCase: SaveReminderNotificationSettingUseCase,
    private val savePasscodeLockSettingUseCase: SavePasscodeLockSettingUseCase,
    private val saveWeatherInfoFetchSettingUseCase: SaveWeatherInfoFetchSettingUseCase,
    private val deleteAllDiariesUseCase: DeleteAllDiariesUseCase,
    private val deleteAllDataUseCase: DeleteAllDataUseCase,
) : BaseViewModel<SettingsEvent, SettingsAppMessage, SettingsState>(
    SettingsState.Idle
) {

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

    override val isProcessingState =
        uiState
            .map { state ->
                // TODO:保留
                when (state) {
                    SettingsState.Idle -> false
                }
            }.stateInDefault(
                viewModelScope,
                false
            )

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

    lateinit var isCheckedWeatherInfoFetch: StateFlow<Boolean?>
        private set
    private lateinit var isWeatherInfoFetchNotNull: StateFlow<Boolean>

    lateinit var isAllSettingsNotNull: StateFlow<Boolean>

    init {
        setUpPreferencesValueLoading()
    }

    override fun initialize() {
        super.initialize()
        setUpPreferencesValueLoading()
    }

    private fun setUpPreferencesValueLoading() {
        val allPreferences = fetchAllSettingsValueUseCase().value
        setUpThemeColorPreferenceValueLoading(allPreferences)
        setUpCalendarStartDayOfWeekPreferenceValueLoading(allPreferences)
        setUpReminderNotificationPreferenceValueLoading(allPreferences)
        setUpPasscodeLockPreferenceValueLoading(allPreferences)
        setUpWeatherInfoFetchPreferenceValueLoading(allPreferences)
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

    private fun setUpWeatherInfoFetchPreferenceValueLoading(preferences: Flow<AllPreferences>) {
        isCheckedWeatherInfoFetch =
            preferences.map { value ->
                value.weatherInfoFetchPreference.isChecked
            }.stateIn(null)

        isWeatherInfoFetchNotNull =
            isCheckedWeatherInfoFetch.map { value ->
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
                isWeatherInfoFetchNotNull
            ) {
                    isThemeColorNotNull,
                    isCalendarStartDayOfWeekNotNull,
                    isReminderNotificationNotNull,
                    isPasscodeLockNotNull,
                    isWeatherInfoFetchNotNull ->
                return@combine isThemeColorNotNull
                        && isCalendarStartDayOfWeekNotNull
                        && isReminderNotificationNotNull
                        && isPasscodeLockNotNull
                        && isWeatherInfoFetchNotNull
            }.stateIn(false)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // ViewClicked処理
    fun onThemeColorSettingButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClicked() {
        val dayOfWeek = calendarStartDayOfWeek.requireValue()

        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    fun onReminderNotificationSettingCheckedChanged(isChecked: Boolean) {
        // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedReminderNotification.requireValue()
        if (isChecked == settingValue) return

        viewModelScope.launch {
            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    emitViewModelEvent(
                        SettingsEvent.CheckPostNotificationsPermission
                    )
                } else {
                    emitViewModelEvent(
                        SettingsEvent.NavigateReminderNotificationTimePickerDialog
                    )
                }
            } else {
                saveReminderNotificationInvalid()
            }
        }

    }

    fun onPasscodeLockSettingCheckedChanged(isChecked: Boolean) {
        // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedPasscodeLock.requireValue()
        if (isChecked == settingValue) return

        viewModelScope.launch {
            savePasscodeLock(isChecked)
        }
    }

    fun onWeatherInfoFetchSettingCheckedChanged(isChecked: Boolean) {
        // DateStorePreferences初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedWeatherInfoFetch.requireValue()
        if (isChecked == settingValue) return

        viewModelScope.launch {
            if (isChecked) {
                emitViewModelEvent(
                    SettingsEvent.CheckAccessLocationPermission
                )
            } else {
                saveWeatherInfoFetch(false)
            }
        }
    }

    fun onAllDiariesDeleteButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClicked() {
        viewModelScope.launch {
            emitViewModelEvent(
                SettingsEvent.NavigateOpenSourceLicensesFragment
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
            emitViewModelEvent(
                SettingsEvent.TurnOffReminderNotificationSettingSwitch
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
            deleteAllDiaries()
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
            deleteAllData()
        }
    }

    // Permission処理
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onPostNotificationsPermissionChecked(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                emitViewModelEvent(
                    SettingsEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitViewModelEvent(
                    SettingsEvent.CheckShouldShowRequestPostNotificationsPermissionRationale
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onShouldShowRequestPostNotificationsPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                emitViewModelEvent(
                    SettingsEvent.ShowRequestPostNotificationsPermissionRationale
                )
            } else {
                emitViewModelEvent(
                    SettingsEvent.TurnOffReminderNotificationSettingSwitch
                )
                emitViewModelEvent(
                    SettingsEvent.NavigateNotificationPermissionDialog
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                emitViewModelEvent(
                    SettingsEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitViewModelEvent(
                    SettingsEvent.TurnOffReminderNotificationSettingSwitch
                )
            }
        }
    }

    fun onAccessLocationPermissionChecked(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                saveWeatherInfoFetch(true)
            } else {
                emitViewModelEvent(
                    SettingsEvent.CheckShouldShowRequestAccessLocationPermissionRationale
                )
            }
        }
    }

    fun onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                emitViewModelEvent(
                    SettingsEvent.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                emitViewModelEvent(
                    SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch
                )
                emitViewModelEvent(
                    SettingsEvent.NavigateLocationPermissionDialog
                )
            }
        }
    }

    fun onRequestAccessLocationPermissionRationaleResultReceived(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                saveWeatherInfoFetch(true)
            } else {
                emitViewModelEvent(
                    SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch
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
    fun onInitializeWeatherInfoFetchSettingFromPermission(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) return@launch

            saveWeatherInfoFetch(false)
        }
    }

    private suspend fun saveThemeColor(value: ThemeColor) {
        executeSettingUpdate {
            saveThemeColorSettingUseCase(value)
        }
    }

    private suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        executeSettingUpdate {
            saveCalendarStartDayOfWeekUseCase(value)
        }
    }

    private suspend fun saveReminderNotificationValid(value: LocalTime) {
        executeSettingUpdate {
            saveReminderNotificationSettingUseCase(true, value)
        }
    }

    private suspend fun saveReminderNotificationInvalid() {
        executeSettingUpdate {
            saveReminderNotificationSettingUseCase(false)
        }
    }

    private suspend fun savePasscodeLock(value: Boolean) {
        val passcode = if (value) {
            "0000" // TODO:仮
        } else {
            ""
        }

        executeSettingUpdate {
            savePasscodeLockSettingUseCase(value, passcode)
        }
    }

    private suspend fun saveWeatherInfoFetch(value: Boolean) {
        executeSettingUpdate {
            saveWeatherInfoFetchSettingUseCase(value)
        }
    }

    private suspend fun executeSettingUpdate(
        process: suspend () -> DefaultUseCaseResult<Unit>
    ) {
        when (val result = process()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "アプリ設定値更新_失敗", result.exception)
                emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
            }
        }
    }

    private suspend fun deleteAllDiaries() {
        when (val result = deleteAllDiariesUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "全日記削除_失敗", result.exception)
                when (result.exception) {
                    is DeleteAllDiariesUseCaseException.DeleteAllDiariesFailed -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryDeleteFailure)
                    }
                    is DeleteAllDiariesUseCaseException.RevokeAllPersistentAccessUriFailed -> {
                        // 処理なし
                    }
                }
            }
        }
    }

    private suspend fun initializeAllSettings() {
        executeSettingUpdate {
            initializeAllSettingsUseCase()
        }
    }

    private suspend fun deleteAllData() {
        when (val result = deleteAllDataUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "アプリ全データ削除_失敗", result.exception)
                when (result.exception) {
                    is DeleteAllDataUseCaseException.DeleteAllDataFailed -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDataDeleteFailure)
                    }
                    is DeleteAllDataUseCaseException.RevokeAllPersistentAccessUriFailed -> {
                        // 処理なし
                    }
                }
            }
        }
    }
}
