package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDataUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDiariesUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchPasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveCalendarStartDayOfWeekUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SavePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.state.SettingsState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val fetchThemeColorSettingUseCase: FetchThemeColorSettingUseCase,
    private val fetchCalendarStartDayOfWeekSettingUseCase: FetchCalendarStartDayOfWeekSettingUseCase,
    private val fetchReminderNotificationSettingUseCase: FetchReminderNotificationSettingUseCase,
    private val fetchPasscodeLockSettingUseCase: FetchPasscodeLockSettingUseCase,
    private val fetchWeatherInfoFetchSettingUseCase: FetchWeatherInfoFetchSettingUseCase,
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
    //      DateSourceからの読込が完了する前(onCreateView()の時点)にViewの設定で設定値を参照してしまい、
    //      例外が発生してしまう問題が発生。
    //      通常のアプリ起動だとDateSourceからの読込が完了してからActivityのBinding処理を行うようにしている為、
    //      このような問題は発生しない。各フラグメントにDateSourceからの読込完了条件をいれるとコルーチンを使用する等の
    //      複雑な処理になるため、SavedStateHandleで対応する。
    companion object {
        private const val SAVED_THEME_COLOR_STATE_KEY = "savedThemeColorState"
        private const val SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY = "savedCalendarStartDayOfWeekState"
    }

    private fun <T> Flow<T>.stateInEagerly(initialValue: T): StateFlow<T> {
        return this.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)
    }

    private val logTag = createLogTag()

    override val isProcessingState =
        uiState
            .map { state ->
                when (state) {
                    SettingsState.LoadingAllSettings,
                    SettingsState.DeletingAllData,
                    SettingsState.DeletingAllDiaries -> true

                    SettingsState.Idle,
                    SettingsState.LoadAllSettingsSuccess,
                    SettingsState.LoadAllSettingsFailure -> false
                }
            }.stateInWhileSubscribed(
                false
            )

    // MEMO:StateFlow型設定値変数の値はデータソースからの値のみを代入したいので、
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

    lateinit var isCheckedWeatherInfoFetch: StateFlow<Boolean?>
        private set

    init {
        setUpSettingsValue()
    }

    override fun initialize() {
        super.initialize()
        setUpSettingsValue()
    }

    override suspend fun emitNavigatePreviousFragmentEvent() {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment
                )
            )
        }
    }

    override suspend fun emitAppMessageEvent(appMessage: SettingsAppMessage) {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.CommonEvent(
                    CommonUiEvent.NavigateAppMessage(appMessage)
                )
            )
        }
    }

    private fun setUpSettingsValue() {
        updateUiState(SettingsState.LoadingAllSettings)
        setUpThemeColorSettingValue()
        setUpCalendarStartDayOfWeekSettingValue()
        setUpReminderNotificationSettingValue()
        setUpPasscodeLockSettingValue()
        setUpWeatherInfoFetchSettingValue()
    }

    private fun onUserSettingsFetchSuccess() {
        when (uiState.value) {
            SettingsState.LoadingAllSettings -> {
                updateUiState(SettingsState.LoadAllSettingsSuccess)
            }

            SettingsState.Idle,
            SettingsState.DeletingAllData,
            SettingsState.DeletingAllDiaries,
            SettingsState.LoadAllSettingsFailure,
            SettingsState.LoadAllSettingsSuccess -> {
                // 処理なし
            }
        }
    }

    private suspend fun onUserSettingsFetchFailure() {
        when (uiState.value) {
            SettingsState.LoadingAllSettings,
            SettingsState.LoadAllSettingsSuccess -> {
                updateUiState(SettingsState.LoadAllSettingsFailure)
                emitAppMessageEvent(SettingsAppMessage.SettingLoadingFailure)
            }

            SettingsState.Idle,
            SettingsState.DeletingAllData,
            SettingsState.DeletingAllDiaries,
            SettingsState.LoadAllSettingsFailure -> {
                // 処理なし
            }
        }
    }

    private fun setUpThemeColorSettingValue() {
        val initialValue = handle.get<ThemeColor>(SAVED_THEME_COLOR_STATE_KEY)
        themeColor =
            fetchThemeColorSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            onUserSettingsFetchSuccess()
                            it.setting.themeColor
                        }
                        is UserSettingResult.Failure -> {
                            onUserSettingsFetchFailure()
                            it.fallbackSetting.themeColor
                        }
                    }
                }.onEach { value: ThemeColor ->
                    handle[SAVED_THEME_COLOR_STATE_KEY] = value
                }.stateInEagerly(initialValue)
    }

    private fun setUpCalendarStartDayOfWeekSettingValue() {
        val initialValue = handle.get<DayOfWeek>(SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY)
        calendarStartDayOfWeek =
            fetchCalendarStartDayOfWeekSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.dayOfWeek
                        is UserSettingResult.Failure -> it.fallbackSetting.dayOfWeek
                    }
                }.onEach { value: DayOfWeek ->
                    handle[SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY] = value
                }.stateInEagerly(initialValue)
    }

    private fun setUpReminderNotificationSettingValue() {
        isCheckedReminderNotification =
            fetchReminderNotificationSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.isEnabled
                        is UserSettingResult.Failure -> it.fallbackSetting.isEnabled
                    }
                }.stateInEagerly(null )

        reminderNotificationTime =
            fetchReminderNotificationSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            when (it.setting) {
                                is ReminderNotificationSetting.Enabled -> it.setting.notificationTime
                                ReminderNotificationSetting.Disabled -> null
                            }
                        }
                        is UserSettingResult.Failure -> {
                            when (it.fallbackSetting) {
                                is ReminderNotificationSetting.Enabled -> it.fallbackSetting.notificationTime
                                ReminderNotificationSetting.Disabled -> null
                            }
                        }
                    }
                }.stateInEagerly(null)
    }

    private fun setUpPasscodeLockSettingValue() {
        isCheckedPasscodeLock =
            fetchPasscodeLockSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            it.setting.isEnabled
                        }
                        is UserSettingResult.Failure -> it.fallbackSetting.isEnabled
                    }
                }.stateInEagerly(null )

        passcode =
            fetchPasscodeLockSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            when (it.setting) {
                                is PasscodeLockSetting.Enabled -> it.setting.passcode
                                PasscodeLockSetting.Disabled -> null
                            }
                        }
                        is UserSettingResult.Failure -> {
                            when (it.fallbackSetting) {
                                is PasscodeLockSetting.Enabled -> it.fallbackSetting.passcode
                                PasscodeLockSetting.Disabled -> null
                            }
                        }
                    }
                }.stateInEagerly(null )
    }

    private fun setUpWeatherInfoFetchSettingValue() {
        isCheckedWeatherInfoFetch =
            fetchWeatherInfoFetchSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.isEnabled
                        is UserSettingResult.Failure -> it.fallbackSetting.isEnabled
                    }
                }.stateInEagerly(null)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // ViewClicked処理
    private fun canExecuteSettingsOperation(): Boolean {
        when (uiState.value) {
            SettingsState.LoadAllSettingsFailure -> {
                viewModelScope.launch {
                    emitAppMessageEvent(SettingsAppMessage.SettingsNotLoadedRetryRestart)
                }
                return false
            }
            SettingsState.LoadAllSettingsSuccess -> {
                return true
            }

            SettingsState.Idle,
            SettingsState.LoadingAllSettings,
            SettingsState.DeletingAllData,
            SettingsState.DeletingAllDiaries -> return false
        }
    }

    fun onThemeColorSettingButtonClicked() {
        if (!canExecuteSettingsOperation()) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClicked() {
        if (!canExecuteSettingsOperation()) return

        val dayOfWeek = calendarStartDayOfWeek.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    fun onReminderNotificationSettingCheckedChanged(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedReminderNotification.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation()) {
            viewModelScope.launch {
                emitUiEvent(SettingsEvent.TurnOffReminderNotificationSettingSwitch)
            }
            return
        }

        viewModelScope.launch {
            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    emitUiEvent(
                        SettingsEvent.CheckPostNotificationsPermission
                    )
                } else {
                    emitUiEvent(
                        SettingsEvent.NavigateReminderNotificationTimePickerDialog
                    )
                }
            } else {
                saveReminderNotificationInvalid()
            }
        }

    }

    fun onPasscodeLockSettingCheckedChanged(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedPasscodeLock.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation()) {
            viewModelScope.launch {
                emitUiEvent(SettingsEvent.TurnOffPasscodeLockSettingSwitch)
            }
            return
        }

        viewModelScope.launch {
            savePasscodeLock(isChecked)
        }
    }

    fun onWeatherInfoFetchSettingCheckedChanged(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedWeatherInfoFetch.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation()) {
            viewModelScope.launch {
                emitUiEvent(SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch)
            }
            return
        }

        viewModelScope.launch {
            if (isChecked) {
                emitUiEvent(
                    SettingsEvent.CheckAccessLocationPermission
                )
            } else {
                saveWeatherInfoFetch(false)
            }
        }
    }

    fun onAllDiariesDeleteButtonClicked() {
        if (!canExecuteSettingsOperation()) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClicked() {
        if (!canExecuteSettingsOperation()) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClicked() {
        if (!canExecuteSettingsOperation()) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClicked() {
        viewModelScope.launch {
            emitUiEvent(
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
            emitUiEvent(
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
                emitUiEvent(
                    SettingsEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitUiEvent(
                    SettingsEvent.CheckShouldShowRequestPostNotificationsPermissionRationale
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onShouldShowRequestPostNotificationsPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsEvent.ShowRequestPostNotificationsPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsEvent.TurnOffReminderNotificationSettingSwitch
                )
                emitUiEvent(
                    SettingsEvent.NavigateNotificationPermissionDialog
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                emitUiEvent(
                    SettingsEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitUiEvent(
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
                emitUiEvent(
                    SettingsEvent.CheckShouldShowRequestAccessLocationPermissionRationale
                )
            }
        }
    }

    fun onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest: Boolean) {
        viewModelScope.launch {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsEvent.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch
                )
                emitUiEvent(
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
                emitUiEvent(
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
        executeSettingUpdate(
            { saveThemeColorSettingUseCase(value) }
        )
    }

    private suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        executeSettingUpdate(
            { saveCalendarStartDayOfWeekUseCase(value) }
        )
    }

    private suspend fun saveReminderNotificationValid(value: LocalTime) {
        executeSettingUpdate (
            { saveReminderNotificationSettingUseCase(true, value) }
        )
    }

    private suspend fun saveReminderNotificationInvalid() {
        executeSettingUpdate(
            { saveReminderNotificationSettingUseCase(false) },
            { emitUiEvent(SettingsEvent.TurnOffReminderNotificationSettingSwitch) }
        )
    }

    private suspend fun savePasscodeLock(value: Boolean) {
        val passcode = if (value) {
            "0000" // TODO:仮
        } else {
            ""
        }

        executeSettingUpdate (
            { savePasscodeLockSettingUseCase(value, passcode) },
            { emitUiEvent(SettingsEvent.TurnOffPasscodeLockSettingSwitch) }
        )
    }

    private suspend fun saveWeatherInfoFetch(value: Boolean) {
        executeSettingUpdate(
            { saveWeatherInfoFetchSettingUseCase(value) },
            { emitUiEvent(SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch) }
        )
    }

    private suspend fun executeSettingUpdate(
        process: suspend () -> DefaultUseCaseResult<Unit>,
        onFailure: (suspend () -> Unit)? = null
    ) {
        when (val result = process()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "アプリ設定値更新_失敗", result.exception)
                if (onFailure != null) onFailure()
                emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
            }
        }
    }

    private suspend fun deleteAllDiaries() {
        updateUiState(SettingsState.DeletingAllDiaries)
        when (val result = deleteAllDiariesUseCase()) {
            is UseCaseResult.Success -> {
                updateUiState(SettingsState.LoadAllSettingsSuccess)
            }
            is UseCaseResult.Failure -> {
                updateUiState(SettingsState.LoadAllSettingsSuccess)
                Log.e(logTag, "全日記削除_失敗", result.exception)
                when (result.exception) {
                    is DeleteAllDiariesUseCaseException.DeleteAllDiariesFailed -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryDeleteFailure)
                    }
                    is DeleteAllDiariesUseCaseException.ReleaseAllPersistableUriPermissionFailed -> {
                        // 処理なし
                    }
                }
            }
        }
    }

    private suspend fun initializeAllSettings() {
        executeSettingUpdate(
            { initializeAllSettingsUseCase() }
        )
    }

    private suspend fun deleteAllData() {
        updateUiState(SettingsState.DeletingAllData)
        when (val result = deleteAllDataUseCase()) {
            is UseCaseResult.Success -> {
                updateUiState(SettingsState.LoadAllSettingsSuccess)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "アプリ全データ削除_失敗", result.exception)
                updateUiState(SettingsState.LoadAllSettingsSuccess)
                when (result.exception) {
                    is DeleteAllDataUseCaseException.DeleteAllDataFailed -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDataDeleteFailure)
                    }
                    is DeleteAllDataUseCaseException.ReleaseAllPersistableUriPermissionFailed -> {
                        // 処理なし
                    }
                }
            }
        }
    }
}
