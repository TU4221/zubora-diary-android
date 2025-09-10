package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllDataDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.AllDiariesDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadPasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdatePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.message.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.SettingsState
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
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
    private val loadThemeColorSettingUseCase: LoadThemeColorSettingUseCase,
    private val loadCalendarStartDayOfWeekSettingUseCase: LoadCalendarStartDayOfWeekSettingUseCase,
    private val loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase,
    private val loadPasscodeLockSettingUseCase: LoadPasscodeLockSettingUseCase,
    private val loadWeatherInfoFetchSettingUseCase: LoadWeatherInfoFetchSettingUseCase,
    private val updateThemeColorSettingUseCase: UpdateThemeColorSettingUseCase,
    private val updateCalendarStartDayOfWeekSettingUseCase: UpdateCalendarStartDayOfWeekSettingUseCase,
    private val updateReminderNotificationSettingUseCase: UpdateReminderNotificationSettingUseCase,
    private val updatePasscodeLockSettingUseCase: UpdatePasscodeLockSettingUseCase,
    private val updateWeatherInfoFetchSettingUseCase: UpdateWeatherInfoFetchSettingUseCase,
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

    override val isProgressIndicatorVisible =
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


    private val canExecuteSettingsOperation: Boolean
        get() {
            return when (uiState.value) {
                SettingsState.LoadAllSettingsSuccess -> true

                SettingsState.Idle,
                SettingsState.LoadingAllSettings,
                SettingsState.DeletingAllDiaries,
                SettingsState.DeletingAllData -> false

                SettingsState.LoadAllSettingsFailure -> {
                    viewModelScope.launch {
                        emitAppMessageEvent(SettingsAppMessage.SettingsNotLoadedRetryRestart)
                    }
                    false
                }
            }
        }

    // MEMO:StateFlow型設定値変数の値はデータソースからの値のみを代入したいので、
    //      代入されるまでの間(初回設定値読込中)はnullとする。
    lateinit var themeColor: StateFlow<ThemeColorUi?>
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

    override suspend fun emitNavigatePreviousFragmentEvent(result: FragmentResult<*>) {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.CommonEvent(
                    CommonUiEvent.NavigatePreviousFragment(result)
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
                emitAppMessageEvent(SettingsAppMessage.SettingLoadFailure)
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
        val initialValue = handle.get<ThemeColorUi>(SAVED_THEME_COLOR_STATE_KEY)
        themeColor =
            loadThemeColorSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            onUserSettingsFetchSuccess()
                            it.setting.themeColor.toUiModel()
                        }
                        is UserSettingResult.Failure -> {
                            onUserSettingsFetchFailure()
                            it.fallbackSetting.themeColor.toUiModel()
                        }
                    }
                }.onEach { value: ThemeColorUi ->
                    handle[SAVED_THEME_COLOR_STATE_KEY] = value
                }.stateInEagerly(initialValue)
    }

    private fun setUpCalendarStartDayOfWeekSettingValue() {
        val initialValue = handle.get<DayOfWeek>(SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY)
        calendarStartDayOfWeek =
            loadCalendarStartDayOfWeekSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.dayOfWeek
                        is UserSettingResult.Failure -> {
                            it.fallbackSetting.dayOfWeek
                        }
                    }
                }.onEach { value: DayOfWeek ->
                    handle[SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY] = value
                }.stateInEagerly(initialValue)
    }

    private fun setUpReminderNotificationSettingValue() {
        isCheckedReminderNotification =
            loadReminderNotificationSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.isEnabled
                        is UserSettingResult.Failure -> {
                            it.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null )

        reminderNotificationTime =
            loadReminderNotificationSettingUseCase()
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
            loadPasscodeLockSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> {
                            it.setting.isEnabled
                        }
                        is UserSettingResult.Failure -> {
                            it.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null )

        passcode =
            loadPasscodeLockSettingUseCase()
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
            loadWeatherInfoFetchSettingUseCase()
                .value
                .map {
                    when (it) {
                        is UserSettingResult.Success -> it.setting.isEnabled
                        is UserSettingResult.Failure -> {
                            it.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        viewModelScope.launch {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onThemeColorSettingButtonClick() {
        if (!canExecuteSettingsOperation) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClick() {
        if (!canExecuteSettingsOperation) return

        val dayOfWeek = calendarStartDayOfWeek.requireValue()
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    fun onReminderNotificationSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedReminderNotification.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation) {
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

    fun onPasscodeLockSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedPasscodeLock.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation) {
            viewModelScope.launch {
                emitUiEvent(SettingsEvent.TurnOffPasscodeLockSettingSwitch)
            }
            return
        }

        viewModelScope.launch {
            savePasscodeLock(isChecked)
        }
    }

    fun onWeatherInfoFetchSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = isCheckedWeatherInfoFetch.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation) {
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

    fun onAllDiariesDeleteButtonClick() {
        if (!canExecuteSettingsOperation) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClick() {
        if (!canExecuteSettingsOperation) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClick() {
        if (!canExecuteSettingsOperation) return

        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClick() {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.NavigateOpenSourceLicensesFragment
            )
        }
    }

    // Fragmentからの結果受取処理
    fun onThemeColorSettingDialogResultReceived(result: DialogResult<ThemeColorUi>) {
        when (result) {
            is DialogResult.Positive<ThemeColorUi> -> {
                handleThemeColorSettingDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleThemeColorSettingDialogPositiveResult(themeColor: ThemeColorUi) {
        viewModelScope.launch {
            saveThemeColor(themeColor)
        }
    }

    fun onCalendarStartDayOfWeekSettingDialogResultReceived(result: DialogResult<DayOfWeek>) {
        when (result) {
            is DialogResult.Positive<DayOfWeek> -> {
                handleCalendarStartDayOfWeekSettingDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    private fun handleCalendarStartDayOfWeekSettingDialogPositiveResult(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            saveCalendarStartDayOfWeek(dayOfWeek)
        }
    }

    fun onReminderNotificationSettingDialogResultReceived(result: DialogResult<LocalTime>) {
        when (result) {
            is DialogResult.Positive<LocalTime> -> {
                handleReminderNotificationSettingDialogPositiveResult(result.data)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                handleReminderNotificationSettingDialogNegativeResult()
                return
            }
        }
    }

    private fun handleReminderNotificationSettingDialogPositiveResult(time: LocalTime) {
        viewModelScope.launch {
            saveReminderNotificationValid(time)
        }
    }

    private fun handleReminderNotificationSettingDialogNegativeResult() {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.TurnOffReminderNotificationSettingSwitch
            )
        }
    }

    fun onAllDiariesDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleAllDiariesDeleteDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun handleAllDiariesDeleteDialogPositiveResult() {
        viewModelScope.launch {
            deleteAllDiaries()
        }
    }

    fun onAllSettingsInitializationDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleAllSettingsInitializationDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun handleAllSettingsInitializationDialogPositiveResult() {
        viewModelScope.launch {
            initializeAllSettings()
        }
    }

    fun onAllDataDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleAllDataDeleteDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun handleAllDataDeleteDialogPositiveResult() {
        viewModelScope.launch {
            deleteAllData()
        }
    }

    fun onPermissionDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handlePermissionDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                return
            }
        }
    }

    private fun handlePermissionDialogPositiveResult() {
        viewModelScope.launch {
            emitUiEvent(
                SettingsEvent.ShowApplicationDetailsSettings
            )
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
    fun onEnsureReminderNotificationSettingMatchesPermission(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) return@launch

            saveReminderNotificationInvalid()
        }
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    fun onEnsureWeatherInfoFetchSettingMatchesPermission(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) return@launch

            saveWeatherInfoFetch(false)
        }
    }

    private suspend fun saveThemeColor(value: ThemeColorUi) {
        executeSettingUpdate(
            {
                updateThemeColorSettingUseCase(
                    ThemeColorSetting(value.toDomainModel())
                )
            }
        )
    }

    private suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        executeSettingUpdate(
            {
                updateCalendarStartDayOfWeekSettingUseCase(
                    CalendarStartDayOfWeekSetting(value)
                )
            }
        )
    }

    private suspend fun saveReminderNotificationValid(value: LocalTime) {
        executeSettingUpdate (
            {
                updateReminderNotificationSettingUseCase(
                    ReminderNotificationSetting.Enabled(value)
                )
            }
        )
    }

    private suspend fun saveReminderNotificationInvalid() {
        executeSettingUpdate(
            {
                updateReminderNotificationSettingUseCase(
                ReminderNotificationSetting.Disabled
                )
            },
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
            {
                val setting =
                    if (value) {
                        PasscodeLockSetting.Enabled(passcode)
                    } else {
                        PasscodeLockSetting.Disabled
                    }
                updatePasscodeLockSettingUseCase(setting)
            },
            { emitUiEvent(SettingsEvent.TurnOffPasscodeLockSettingSwitch) }
        )
    }

    private suspend fun saveWeatherInfoFetch(value: Boolean) {
        executeSettingUpdate(
            {
                updateWeatherInfoFetchSettingUseCase(
                    WeatherInfoFetchSetting(value)
                )
            },
            { emitUiEvent(SettingsEvent.TurnOffWeatherInfoFetchSettingSwitch) }
        )
    }

    private suspend fun executeSettingUpdate(
        process: suspend () -> UseCaseResult<Unit, UseCaseException>,
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
                    is AllDiariesDeleteException.DeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryDeleteFailure)
                    }
                    is AllDiariesDeleteException.PersistableUriPermissionReleaseFailure -> {
                        // 処理なし
                    }
                }
            }
        }
    }

    private suspend fun initializeAllSettings() {
        when (val result = initializeAllSettingsUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "全設定初期化_失敗", result.exception)
                emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationFailure)
            }
        }
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
                    is AllDataDeleteException.DiariesDeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDataDeleteFailure)
                    }
                    is AllDataDeleteException.UriPermissionReleaseFailure -> {
                        // 処理なし
                    }
                    is AllDataDeleteException.SettingsInitializationFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationFailure)
                    }
                }
            }
        }
    }
}
