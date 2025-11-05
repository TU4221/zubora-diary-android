package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
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
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllSettingsInitializationException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.PassCodeSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.WeatherInfoFetchSettingUpdateException
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
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
) : BaseFragmentViewModel<SettingsUiEvent, SettingsAppMessage, SettingsUiState>(
    SettingsUiState()
) {

    //region Initialization
    init {
        updateToProcessingState()
        collectUiStates()

        // MEMO:このViewModelの初期化処理では、各設定の読み込み失敗を個別にハンドリングしているため、
        //      予期せぬ例外のみを処理する共通の`launchWithUnexpectedErrorHandler`は使用せず、
        //      素の`launch`で実行する。
        viewModelScope.launch {
            waitForAllSettingsInitializationCompletion()
            updateToIdleState()
        }
    }

    private fun collectUiStates() {
        collectThemeColorSetting()
        collectCalendarStartDayOfWeekSetting()
        collectReminderNotificationSetting()
        collectPasscodeLockSetting()
        collectWeatherInfoFetchSetting()
    }

    private fun collectThemeColorSetting() {
        loadThemeColorSettingUseCase()
            .onEach {
                handleSettingLoadResult(it)
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                ThemeColorSetting.default()
            ).distinctUntilChanged().onEach {
                updateThemeColor(it.themeColor.toUiModel())
            }.launchIn(viewModelScope)
    }

    private fun collectCalendarStartDayOfWeekSetting() {
        loadCalendarStartDayOfWeekSettingUseCase()
            .onEach {
                handleSettingLoadResult(it)
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                CalendarStartDayOfWeekSetting.default()
            ).distinctUntilChanged().onEach {
                updateCalendarStartDayOfWeek(it.dayOfWeek)
            }.launchIn(viewModelScope)
    }

    private fun collectReminderNotificationSetting() {
        loadReminderNotificationSettingUseCase()
            .onEach {
                handleSettingLoadResult(it)
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                ReminderNotificationSetting.default()
            ).distinctUntilChanged().onEach {
                when (it) {
                    is ReminderNotificationSetting.Enabled -> {
                        updateToReminderEnabledState(it.notificationTime)
                    }
                    ReminderNotificationSetting.Disabled -> updateToReminderDisabledState()
                }
            }.launchIn(viewModelScope)
    }

    private fun collectPasscodeLockSetting() {
        loadPasscodeLockSettingUseCase()
            .onEach {
                handleSettingLoadResult(it)
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                PasscodeLockSetting.default()
            ).distinctUntilChanged().onEach {
                when (it) {
                    is PasscodeLockSetting.Enabled -> {
                        updateToPasscodeEnabledState(it.passcode)
                    }
                    PasscodeLockSetting.Disabled -> updateToPasscodeDisabledState()
                }
            }.launchIn(viewModelScope)
    }

    private fun collectWeatherInfoFetchSetting() {
        loadWeatherInfoFetchSettingUseCase()
            .onEach {
                handleSettingLoadResult(it)
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                WeatherInfoFetchSetting.default()
            ).distinctUntilChanged().onEach {
                updateIsWeatherFetchEnabled(it.isEnabled)
            }.launchIn(viewModelScope)
    }

    private suspend fun handleSettingLoadResult(result: UseCaseResult<*, UseCaseException>) {
        when (result) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> handleSettingLoadFailure(result.exception)
        }
    }

    private suspend fun handleSettingLoadFailure(exception: UseCaseException) {
        if (currentUiState.hasSettingsLoadFailure) return

        updateToSettingsLoadFailedState()
        if (exception is UseCaseUnknownException) {
            emitUnexpectedAppMessage(exception)
        } else {
            emitAppMessageEvent(SettingsAppMessage.SettingLoadFailure)
        }
    }

    /**
     * 全ての設定値が初期化されるまで待機するsuspend関数。
     */
    private suspend fun waitForAllSettingsInitializationCompletion() {
        uiState.map {
            it.themeColor != null
                    && it.calendarStartDayOfWeek != null
                    && it.isReminderEnabled != null
                    && it.isPasscodeLockEnabled != null
                    && it.isWeatherFetchEnabled != null
        }.first { allAreReady ->
            allAreReady
        }
    }
    //endregion

    //region UI Event Handlers - Action
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    fun onThemeColorSettingButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClick() {
        if (!canExecuteOperation()) return

        val dayOfWeek = currentUiState.calendarStartDayOfWeek ?: return
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    fun onReminderNotificationSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = currentUiState.isReminderEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                emitUiEvent(
                    SettingsUiEvent.TurnReminderNotificationSettingSwitch(!isChecked)
                )
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    emitUiEvent(
                        SettingsUiEvent.CheckPostNotificationsPermission
                    )
                } else {
                    emitUiEvent(
                        SettingsUiEvent.NavigateReminderNotificationTimePickerDialog
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
        val settingValue = currentUiState.isPasscodeLockEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                emitUiEvent(
                    SettingsUiEvent.TurnPasscodeLockSettingSwitch(!isChecked)
                )
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            savePasscodeLock(isChecked)
        }
    }

    fun onWeatherInfoFetchSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = currentUiState.isWeatherFetchEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                emitUiEvent(
                    SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(!isChecked)
                )
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            if (isChecked) {
                emitUiEvent(
                    SettingsUiEvent.CheckAccessLocationPermission
                )
            } else {
                saveWeatherInfoFetch(false)
            }
        }
    }

    fun onAllDiariesDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateOpenSourceLicensesFragment
            )
        }
    }
    //endregion

    //region UI Event Handlers - Results
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
        launchWithUnexpectedErrorHandler {
            saveThemeColor(themeColor.toDomainModel())
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
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
            saveReminderNotificationValid(time)
        }
    }

    private fun handleReminderNotificationSettingDialogNegativeResult() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.TurnReminderNotificationSettingSwitch(false)
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
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.ShowApplicationDetailsSettings
            )
        }
    }
    //endregion

    //region UI Event Handlers - Permissions
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onPostNotificationsPermissionChecked(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) {
                emitUiEvent(
                    SettingsUiEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitUiEvent(
                    SettingsUiEvent.CheckShouldShowRequestPostNotificationsPermissionRationale
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onShouldShowRequestPostNotificationsPermissionRationaleChecked(shouldShowRequest: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsUiEvent.ShowRequestPostNotificationsPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsUiEvent.TurnReminderNotificationSettingSwitch(false)
                )
                emitUiEvent(
                    SettingsUiEvent.NavigateNotificationPermissionDialog
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) {
                emitUiEvent(
                    SettingsUiEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitUiEvent(
                    SettingsUiEvent.TurnReminderNotificationSettingSwitch(false)
                )
            }
        }
    }

    fun onAccessLocationPermissionChecked(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) {
                saveWeatherInfoFetch(true)
            } else {
                emitUiEvent(
                    SettingsUiEvent.CheckShouldShowRequestAccessLocationPermissionRationale
                )
            }
        }
    }

    fun onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsUiEvent.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(false)
                )
                emitUiEvent(
                    SettingsUiEvent.NavigateLocationPermissionDialog
                )
            }
        }
    }

    fun onRequestAccessLocationPermissionRationaleResultReceived(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) {
                saveWeatherInfoFetch(true)
            } else {
                emitUiEvent(
                    SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(false)
                )
            }
        }
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    fun onEnsureReminderNotificationSettingMatchesPermission(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) return@launchWithUnexpectedErrorHandler

            saveReminderNotificationInvalid()
        }
    }

    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    fun onEnsureWeatherInfoFetchSettingMatchesPermission(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) return@launchWithUnexpectedErrorHandler

            saveWeatherInfoFetch(false)
        }
    }
    //endregion

    //region Business Logic
    /**
     * 現在のUI状態に基づいて、設定操作が実行可能かどうかを同期的に判定する。
     * 操作不可能な場合は、必要に応じてユーザーにメッセージを表示する。
     *
     * @return 操作可能な場合は `true`、そうでない場合は `false`。
     */
    private fun canExecuteOperation(): Boolean {
        if (currentUiState.hasSettingsLoadFailure) {
            launchWithUnexpectedErrorHandler {
                emitAppMessageEvent(SettingsAppMessage.SettingsNotLoadedRetryRestart)
            }
            return false
        }
        return isReadyForOperation
    }

    private suspend fun saveThemeColor(value: ThemeColor) {
        val result =
            updateThemeColorSettingUseCase(
                ThemeColorSetting(value)
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is ThemeColorSettingUpdateException.UpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is ThemeColorSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is ThemeColorSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun saveCalendarStartDayOfWeek(value: DayOfWeek) {
        val result =
            updateCalendarStartDayOfWeekSettingUseCase(
                CalendarStartDayOfWeekSetting(value)
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is CalendarStartDayOfWeekSettingUpdateException.UpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is CalendarStartDayOfWeekSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is CalendarStartDayOfWeekSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun saveReminderNotificationValid(value: LocalTime) {
        val result =
            updateReminderNotificationSettingUseCase(
                ReminderNotificationSetting.Enabled(value)
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                emitUiEvent(SettingsUiEvent.TurnReminderNotificationSettingSwitch(false))
                when (result.exception) {
                    is ReminderNotificationSettingUpdateException.SettingUpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is ReminderNotificationSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is ReminderNotificationSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun saveReminderNotificationInvalid() {
        val result =
            updateReminderNotificationSettingUseCase(
                ReminderNotificationSetting.Disabled
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                emitUiEvent(SettingsUiEvent.TurnReminderNotificationSettingSwitch(true))
                when (result.exception) {
                    is ReminderNotificationSettingUpdateException.SettingUpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is ReminderNotificationSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is ReminderNotificationSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun savePasscodeLock(value: Boolean) {
        val passcode = if (value) {
            "0000" // TODO:仮
        } else {
            ""
        }

        val setting =
            if (value) {
                PasscodeLockSetting.Enabled(passcode)
            } else {
                PasscodeLockSetting.Disabled
            }
        val result = updatePasscodeLockSettingUseCase(setting)
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                emitUiEvent(SettingsUiEvent.TurnPasscodeLockSettingSwitch(!value))
                when (result.exception) {
                    is PassCodeSettingUpdateException.UpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is PassCodeSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is PassCodeSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun saveWeatherInfoFetch(value: Boolean) {
        val result =
            updateWeatherInfoFetchSettingUseCase(
                WeatherInfoFetchSetting(value)
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                emitUiEvent(SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(!value))
                when (result.exception) {
                    is WeatherInfoFetchSettingUpdateException.UpdateFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateFailure)
                    }
                    is WeatherInfoFetchSettingUpdateException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.SettingUpdateInsufficientStorageFailure)
                    }
                    is WeatherInfoFetchSettingUpdateException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun deleteAllDiaries() {
        updateToProcessingState()
        when (val result = deleteAllDiariesUseCase()) {
            is UseCaseResult.Success -> {
                updateToIdleState()
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                Log.e(logTag, "全日記削除_失敗", result.exception)
                when (result.exception) {
                    is AllDiariesDeleteException.DeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryDeleteFailure)
                    }
                    is AllDiariesDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryImagesDeleteFailure)
                    }
                    is AllDiariesDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
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
                when (result.exception) {
                    is AllSettingsInitializationException.InitializationFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationFailure)
                    }
                    is AllSettingsInitializationException.InsufficientStorage -> {
                        emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationInsufficientStorageFailure)
                    }
                    is AllSettingsInitializationException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    private suspend fun deleteAllData() {
        updateToProcessingState()
        when (val result = deleteAllDataUseCase()) {
            is UseCaseResult.Success -> {
                updateToIdleState()
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "アプリ全データ削除_失敗", result.exception)
                updateToIdleState()
                when (result.exception) {
                    is AllDataDeleteException.DiariesDeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDataDeleteFailure)
                    }
                    is AllDataDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllDiaryImagesDeleteFailure)
                    }
                    is AllDataDeleteException.SettingsInitializationFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationFailure)
                    }
                    is AllDataDeleteException.SettingsInitializationInsufficientStorageFailure -> {
                        emitAppMessageEvent(SettingsAppMessage.AllSettingsInitializationInsufficientStorageFailure)
                    }
                    is AllDataDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }
    //endregion

    //region UI State Update
    private fun updateThemeColor(themeColor: ThemeColorUi?) {
        updateUiState { it.copy(themeColor = themeColor) }
    }

    private fun updateCalendarStartDayOfWeek(dayOfWeek: DayOfWeek?) {
        updateUiState { it.copy(calendarStartDayOfWeek = dayOfWeek) }
    }

    private fun updateIsWeatherFetchEnabled(isEnabled: Boolean?) {
        updateUiState { it.copy(isWeatherFetchEnabled = isEnabled) }
    }

    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    private fun updateToSettingsLoadFailedState() {
        updateUiState { it.copy(hasSettingsLoadFailure = true) }
    }

    private fun updateToReminderEnabledState(notificationTime: LocalTime) {
        updateUiState {
            it.copy(
                isReminderEnabled = true,
                reminderNotificationTime = notificationTime
            )
        }
    }

    private fun updateToReminderDisabledState() {
        updateUiState {
            it.copy(
                isReminderEnabled = false,
                reminderNotificationTime = null
            )
        }
    }

    private fun updateToPasscodeEnabledState(passcode: String) {
        updateUiState {
            it.copy(
                isPasscodeLockEnabled = true,
                passcode = passcode
            )
        }
    }

    private fun updateToPasscodeDisabledState() {
        updateUiState {
            it.copy(
                isPasscodeLockEnabled = false,
                passcode = null
            )
        }
    }
    //endregion
}
