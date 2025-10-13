package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
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
                    launchWithUnexpectedErrorHandler {
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

    override fun createNavigatePreviousFragmentEvent(result: FragmentResult<*>): SettingsEvent {
        return SettingsEvent.CommonEvent(
            CommonUiEvent.NavigatePreviousFragment(result)
        )
    }

    override fun createAppMessageEvent(appMessage: SettingsAppMessage): SettingsEvent {
        return SettingsEvent.CommonEvent(
            CommonUiEvent.NavigateAppMessage(appMessage)
        )
    }

    override fun createUnexpectedAppMessage(e: Exception): SettingsAppMessage {
        return SettingsAppMessage.Unexpected(e)
    }

    private fun setUpSettingsValue() {
        updateUiState(SettingsState.LoadingAllSettings)
        setUpThemeColorSettingValue()
        setUpCalendarStartDayOfWeekSettingValue()
        setUpReminderNotificationSettingValue()
        setUpPasscodeLockSettingValue()
        setUpWeatherInfoFetchSettingValue()
    }

    // TODO:下記メソッドは一設定の読込メソッドでしか呼び出されておらず、Data層の詳細を知った呼び出し方の為、修正用。
    //      また下記メソッド名を設定読込後のUiState更新を意味する名称に変更する。
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

    // TODO:下記メソッドは一設定の読込メソッドでしか呼び出されておらず、Data層の詳細を知った呼び出し方の為、修正用。
    //      また下記メソッド名を設定読込後のUiState更新を意味する名称に変更する。
    private suspend fun onUserSettingsFetchFailure() {
        when (uiState.value) {
            SettingsState.LoadingAllSettings,
            SettingsState.LoadAllSettingsSuccess -> {
                updateUiState(SettingsState.LoadAllSettingsFailure)
                // TODO:ラムダ関数引数などでUnexceptedMessageを通知するように修正。
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
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            onUserSettingsFetchSuccess()
                            it.value.themeColor.toUiModel()
                        }
                        is UseCaseResult.Failure -> {
                            onUserSettingsFetchFailure()
                            it.exception.fallbackSetting.themeColor.toUiModel()
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
                .map {
                    when (it) {
                        is UseCaseResult.Success -> it.value.dayOfWeek
                        is UseCaseResult.Failure -> {
                            it.exception.fallbackSetting.dayOfWeek
                        }
                    }
                }.onEach { value: DayOfWeek ->
                    handle[SAVED_CALENDAR_START_DAY_OF_WEEK_STATE_KEY] = value
                }.stateInEagerly(initialValue)
    }

    private fun setUpReminderNotificationSettingValue() {
        isCheckedReminderNotification =
            loadReminderNotificationSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> it.value.isEnabled
                        is UseCaseResult.Failure -> {
                            it.exception.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null )

        reminderNotificationTime =
            loadReminderNotificationSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            when (it.value) {
                                is ReminderNotificationSetting.Enabled -> it.value.notificationTime
                                ReminderNotificationSetting.Disabled -> null
                            }
                        }
                        is UseCaseResult.Failure -> {
                            when (it.exception.fallbackSetting) {
                                is ReminderNotificationSetting.Enabled -> {
                                    it.exception.fallbackSetting.notificationTime
                                }
                                ReminderNotificationSetting.Disabled -> null
                            }
                        }
                    }
                }.stateInEagerly(null)
    }

    private fun setUpPasscodeLockSettingValue() {
        isCheckedPasscodeLock =
            loadPasscodeLockSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            it.value.isEnabled
                        }
                        is UseCaseResult.Failure -> {
                            it.exception.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null )

        passcode =
            loadPasscodeLockSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            when (it.value) {
                                is PasscodeLockSetting.Enabled -> it.value.passcode
                                PasscodeLockSetting.Disabled -> null
                            }
                        }
                        is UseCaseResult.Failure -> {
                            when (it.exception.fallbackSetting) {
                                is PasscodeLockSetting.Enabled -> it.exception.fallbackSetting.passcode
                                PasscodeLockSetting.Disabled -> null
                            }
                        }
                    }
                }.stateInEagerly(null )
    }

    private fun setUpWeatherInfoFetchSettingValue() {
        isCheckedWeatherInfoFetch =
            loadWeatherInfoFetchSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> it.value.isEnabled
                        is UseCaseResult.Failure -> {
                            it.exception.fallbackSetting.isEnabled
                        }
                    }
                }.stateInEagerly(null)
    }

    // BackPressed(戻るボタン)処理
    override fun onBackPressed() {
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    // Viewクリック処理
    fun onThemeColorSettingButtonClick() {
        if (!canExecuteSettingsOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    fun onCalendarStartDayOfWeekSettingButtonClick() {
        if (!canExecuteSettingsOperation) return

        val dayOfWeek = calendarStartDayOfWeek.requireValue()
        launchWithUnexpectedErrorHandler {
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
            launchWithUnexpectedErrorHandler {
                emitUiEvent(SettingsEvent.TurnReminderNotificationSettingSwitch(false))
            }
            return
        }

        launchWithUnexpectedErrorHandler {
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
            launchWithUnexpectedErrorHandler {
                emitUiEvent(SettingsEvent.TurnPasscodeLockSettingSwitch(false))
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
        val settingValue = isCheckedWeatherInfoFetch.requireValue()
        if (isChecked == settingValue) return

        if (!canExecuteSettingsOperation) {
            launchWithUnexpectedErrorHandler {
                emitUiEvent(SettingsEvent.TurnWeatherInfoFetchSettingSwitch(false))
            }
            return
        }

        launchWithUnexpectedErrorHandler {
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

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    fun onAllSettingsInitializationButtonClick() {
        if (!canExecuteSettingsOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    fun onAllDataDeleteButtonClick() {
        if (!canExecuteSettingsOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    fun onOpenSourceLicenseButtonClick() {
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
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
                SettingsEvent.TurnReminderNotificationSettingSwitch(false)
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
                SettingsEvent.ShowApplicationDetailsSettings
            )
        }
    }

    // Permission処理
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onPostNotificationsPermissionChecked(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
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
        launchWithUnexpectedErrorHandler {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsEvent.ShowRequestPostNotificationsPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsEvent.TurnReminderNotificationSettingSwitch(false)
                )
                emitUiEvent(
                    SettingsEvent.NavigateNotificationPermissionDialog
                )
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) {
                emitUiEvent(
                    SettingsEvent.NavigateReminderNotificationTimePickerDialog
                )
            } else {
                emitUiEvent(
                    SettingsEvent.TurnReminderNotificationSettingSwitch(false)
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
                    SettingsEvent.CheckShouldShowRequestAccessLocationPermissionRationale
                )
            }
        }
    }

    fun onShouldShowRequestAccessLocationPermissionRationaleChecked(shouldShowRequest: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsEvent.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                emitUiEvent(
                    SettingsEvent.TurnWeatherInfoFetchSettingSwitch(false)
                )
                emitUiEvent(
                    SettingsEvent.NavigateLocationPermissionDialog
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
                    SettingsEvent.TurnWeatherInfoFetchSettingSwitch(false)
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

    /**
     * 設定操作の実行を試みるヘルパー関数。
     *
     * 現在の [SettingsState] に基づいて操作の可否を判定する。
     * 操作可能であればメイン処理を、不可能であればリセット処理を実行する。
     *
     * @param currentUiState 現在のUI State。操作可否の判定に使用する。
     * @param onExecute メイン処理。設定操作が可能な場合に実行される。
     * @param onCannotExecute リセット処理。設定操作が不可能な場合に実行される。
     *   [SettingsState] が [SettingsState.LoadAllSettingsFailure]の場合は、
     *   この処理の実行前にエラーメッセージを表示する。
     */
    private suspend fun executeSettingsOperation(
        currentUiState: SettingsState,
        onExecute: suspend () -> Unit,
        onCannotExecute: suspend () -> Unit = {}
    ) {
        val canExecuteSettingsOperation =
            when (currentUiState) {
                SettingsState.LoadAllSettingsSuccess -> true

                SettingsState.Idle,
                SettingsState.LoadingAllSettings,
                SettingsState.DeletingAllDiaries,
                SettingsState.DeletingAllData -> false

                SettingsState.LoadAllSettingsFailure -> {
                    emitAppMessageEvent(SettingsAppMessage.SettingsNotLoadedRetryRestart)
                    false
                }
            }
        if (canExecuteSettingsOperation) {
            onExecute()
        } else {
            onCannotExecute()
        }
    }

    private suspend fun saveThemeColor(value: ThemeColorUi) {
        val result =
            updateThemeColorSettingUseCase(
                ThemeColorSetting(value.toDomainModel())
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
                emitUiEvent(SettingsEvent.TurnReminderNotificationSettingSwitch(false))
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
                emitUiEvent(SettingsEvent.TurnReminderNotificationSettingSwitch(true))
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
                emitUiEvent(SettingsEvent.TurnPasscodeLockSettingSwitch(!value))
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
                emitUiEvent(SettingsEvent.TurnWeatherInfoFetchSettingSwitch(!value))
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
}
