package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.os.Build
import android.util.Log
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

/**
 * 設定画面のUIロジックと状態([SettingsUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - 各種設定（テーマカラー、週の開始曜日、リマインダーなど）の読み込み
 * - ユーザー操作に応じた設定値の更新
 * - リマインダー通知や天気情報取得に必要なOS権限の要求フローの管理
 * - データ管理操作（全日記削除、設定初期化、全データ削除）の実行
 * - 各種設定ダイアログやOSSライセンス画面への遷移イベントの発行
 */
@HiltViewModel
class SettingsViewModel @Inject internal constructor(
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
) : BaseFragmentViewModel<SettingsUiState, SettingsUiEvent, SettingsAppMessage>(
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

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectThemeColorSetting()
        collectCalendarStartDayOfWeekSetting()
        collectReminderNotificationSetting()
        collectPasscodeLockSetting()
        collectWeatherInfoFetchSetting()
    }

    /** テーマカラー設定の変更を監視し、UIに反映させる。 */
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

    /** 週の開始曜日設定の変更を監視し、UIに反映させる。 */
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

    /** リマインダー通知設定の変更を監視し、UIに反映させる。 */
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

    /** パスコードロック設定の変更を監視し、UIに反映させる。 */
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

    /** 天気情報取得設定の変更を監視し、UIに反映させる。 */
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

    /**
     * 設定読み込みのUseCaseResultを処理する共通ハンドラ。
     * @param result 処理対象の[UseCaseResult]
     */
    private suspend fun handleSettingLoadResult(result: UseCaseResult<*, UseCaseException>) {
        when (result) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> handleSettingLoadFailure(result.exception)
        }
    }

    /**
     * [handleSettingLoadResult]から呼び出され、設定読み込み失敗時の処理を行う。
     * @param exception 発生した例外
     */
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
     * 全ての設定値が非nullになるまで待機し、初期化の完了を保証する。
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
            navigatePreviousScreen()
        }
    }

    /**
     * テーマカラー設定項目がクリックされた時に呼び出される事を想定。
     * テーマカラーの選択ダイアログを表示する。
     */
    fun onThemeColorSettingItemClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            showThemeColorPickerDialog()
        }
    }

    /**
     * カレンダー開始曜日設定項目がクリックされた時に呼び出される事を想定。
     * カレンダー開始曜日の選択ダイアログを表示する。
     */
    fun onCalendarStartDayOfWeekSettingItemClick() {
        if (!canExecuteOperation()) return

        val dayOfWeek = currentUiState.calendarStartDayOfWeek ?: return
        launchWithUnexpectedErrorHandler {
            showCalendarStartDayOfWeekPickerDialog(dayOfWeek)
        }
    }

    /**
     * リマインダー通知設定のスイッチが切り替えられた時に呼び出される事を想定。
     * 設定値の保存プロセスを開始する。
     * @param isChecked スイッチがONになった場合はtrue
     */
    fun onReminderNotificationSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = currentUiState.isReminderEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                changeReminderSettingSwitch(!isChecked)
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            startReminderNotificationSettingSaveProcess(isChecked)
        }
    }

    /**
     * パスコードロック設定のスイッチが切り替えられた時に呼び出される事を想定。
     * 設定値を保存する。
     * @param isChecked スイッチがONになった場合はtrue
     */
    fun onPasscodeLockSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = currentUiState.isPasscodeLockEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                changePasscodeLockSettingSwitch(!isChecked)
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            savePasscodeLockSetting(isChecked)
        }
    }

    /**
     * 天気情報取得設定のスイッチが切り替えられた時に呼び出される事を想定。
     * 設定値の保存プロセスを開始する。
     * @param isChecked スイッチがONになった場合はtrue
     */
    fun onWeatherInfoFetchSettingCheckedChange(isChecked: Boolean) {
        // DateSourceからの初回読込時の値がtrueの場合、本メソッドが呼び出される。
        // 初回読込時は処理不要のため下記条件追加。
        val settingValue = currentUiState.isWeatherFetchEnabled
        if (isChecked == settingValue) return

        if (settingValue == null || !canExecuteOperation()) {
            launchWithUnexpectedErrorHandler {
                changeWeatherInfoFetchSettingSwitch(!isChecked)
            }
            return
        }

        launchWithUnexpectedErrorHandler {
            startWeatherInfoFetchSettingSaveProcess(isChecked)
        }
    }

    /**
     * 全日記削除項目がクリックされた時に呼び出される事を想定。
     * 全日記の削除確認ダイアログを表示する
     */
    fun onAllDiariesDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            showAllDiariesDeleteDialog()
        }
    }

    /**
     * 全設定初期化項目がクリックされた時に呼び出される事を想定。
     * 全設定の初期化確認ダイアログを表示する
     */
    fun onAllSettingsInitializationButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            showAllSettingsInitializationDialog()
        }
    }

    /**
     * 全データ削除項目がクリックされた時に呼び出される事を想定。
     * 全データの削除確認ダイアログを表示する
     */
    fun onAllDataDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            showAllDataDeleteDialog()
        }
    }

    /**
     * オープンソースソフトウェアライセンス項目がクリックされた時に呼び出される事を想定。
     * OSSライセンスダイアログを表示する。
     */
    fun onOpenSourceLicenseButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            showOssLicensesScreen()
        }
    }
    //endregion

    //region UI Event Handlers - Results
    /**
     * テーマカラー選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * テーマカラー設定を保存する。
     * @param themeColor 選択されたテーマカラー
     */
    internal fun onThemeColorSettingDialogPositiveResultReceived(themeColor: ThemeColorUi) {
        launchWithUnexpectedErrorHandler {
            saveThemeColorSetting(themeColor.toDomainModel())
        }
    }

    /**
     * カレンダー開始曜日選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 週の開始曜日設定を保存する。
     * @param dayOfWeek 選択された週の開始曜日
     */
    internal fun onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(dayOfWeek: DayOfWeek) {
        launchWithUnexpectedErrorHandler {
            saveCalendarStartDayOfWeekSetting(dayOfWeek)
        }
    }

    /**
     * リマインダー通知時間選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * リマインダー通知を有効にする設定を保存する。
     * @param time 保存する通知時刻
     * */
    internal fun onReminderNotificationSettingDialogPositiveResultReceived(time: LocalTime) {
        launchWithUnexpectedErrorHandler {
            saveReminderNotificationValidSetting(time)
        }
    }

    /**
     * リマインダー通知時間選択ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * リマインダー通知設定スイッチをOffにする。
     * */
    internal fun onReminderNotificationSettingDialogNegativeResultReceived() {
        launchWithUnexpectedErrorHandler {
            changeReminderSettingSwitch(false)
        }
    }

    /**
     * 全日記削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全日記を削除する。
     */
    internal fun onAllDiariesDeleteDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            deleteAllDiaries()
        }
    }

    /**
     * 全設定初期化確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全設定を初期化する。
     */
    internal fun onAllSettingsInitializationDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            initializeAllSettings()
        }
    }

    /**
     * 全データ削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全データを削除する。
     */
    internal fun onAllDataDeleteDialogResultPositiveReceived() {
        launchWithUnexpectedErrorHandler {
            deleteAllData()
        }
    }
    //endregion

    //region UI Event Handlers - Permissions

    //region PostNotificationsPermission
    /**
     * 通知権限の確認で許可された時に呼び出される事を想定。
     * 時刻選択ダイアログを表示する。
     */
    internal fun onPostNotificationsPermissionGranted() {
        launchWithUnexpectedErrorHandler {
            showReminderNotificationTimePickerDialog()
        }
    }

    /**
     * 通知権限の確認で許可されなかった時に呼び出される事を想定。
     * リマインダー通知設定スイッチをOff状態に切り換える。
     */
    internal fun onPostNotificationsPermissionDenied() {
        launchWithUnexpectedErrorHandler {
            changeReminderSettingSwitch(false)
        }
    }

    /**
     * OSの通知権限の状態をリマインダー設定に同期させるために呼び出される事を想定。
     * 権限がないのに設定がONになっている場合は、設定をOFFに更新する。
     * @param isGranted OSの通知権限が許可されている場合はtrue
     */
    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    internal fun onEnsureReminderNotificationSettingMatchesPermission(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) return@launchWithUnexpectedErrorHandler

            saveReminderNotificationInvalidSetting()
        }
    }
    //endregion

    //region AccessLocationPermission
    /**
     * 位置情報権限の確認で許可された時に呼び出される事を想定。
     * 天気情報取得設定(有効)を保存する。
     */
    internal fun onAccessLocationPermissionGranted() {
        launchWithUnexpectedErrorHandler {
            saveWeatherInfoFetchSetting(true)
        }
    }

    /**
     * 位置情報権限の確認で許可されなかった時に呼び出される事を想定。
     * 天気情報取得設定スイッチをOff状態に切り換える。
     */
    internal fun onAccessLocationPermissionDenied() {
        launchWithUnexpectedErrorHandler {
            changeWeatherInfoFetchSettingSwitch(false)
        }
    }

    /**
     * OSの位置情報権限の状態を天気情報取得設定に同期させるために呼び出される事を想定。
     * 権限がないのに設定がONになっている場合は、設定をOFFに更新する。
     * @param isGranted OSの位置情報権限が許可されている場合はtrue
     */
    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    internal fun onEnsureWeatherInfoFetchSettingMatchesPermission(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) return@launchWithUnexpectedErrorHandler

            saveWeatherInfoFetchSetting(false)
        }
    }
    //endregion

    //endregion

    //region Business Logic

    //region Common Operation
    /**
     * 現在のUI状態に基づいて、設定操作が実行可能かどうかを同期的に判定する。
     * 操作不可能な場合は、必要に応じてユーザーにメッセージを表示する。
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
    //endregion

    //region Theme Color Setting Operation
    /**
     * テーマカラーの選択ダイログを表示する（イベント発行）。
     */
    private suspend fun showThemeColorPickerDialog() {
        emitUiEvent(
            SettingsUiEvent.ShowThemeColorPickerDialog
        )
    }

    /**
     * テーマカラー設定を保存する。
     * @param themeColor 保存するテーマカラー
     */
    private suspend fun saveThemeColorSetting(themeColor: ThemeColor) {
        val result =
            updateThemeColorSettingUseCase(
                ThemeColorSetting(themeColor)
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
    //endregion

    //region Calendar Start Day Of Week Setting Operation
    /**
     * カレンダー開始曜日の選択ダイアログを表示する（イベント発行）。
     */
    private suspend fun showCalendarStartDayOfWeekPickerDialog(dayOfWeek: DayOfWeek) {
        emitUiEvent(
            SettingsUiEvent.ShowCalendarStartDayPickerDialog(dayOfWeek)
        )
    }

    /**
     * 週の開始曜日設定を保存する。
     * @param dayOfWeek 保存する週の開始曜日
     */
    private suspend fun saveCalendarStartDayOfWeekSetting(dayOfWeek: DayOfWeek) {
        val result =
            updateCalendarStartDayOfWeekSettingUseCase(
                CalendarStartDayOfWeekSetting(dayOfWeek)
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
    //endregion

    //region Reminder Notification Setting Operation
    /**
     * リマインダー通知設定スイッチの状態を変更する（イベント発行）。
     * @param isChecked 有効状態にする場合はtrue
     */
    private suspend fun changeReminderSettingSwitch(isChecked: Boolean) {
        emitUiEvent(
            SettingsUiEvent.TurnReminderNotificationSettingSwitch(isChecked)
        )
    }

    /**
     * リマインダー通知設定の保存プロセスを開始する。
     * 有効化の場合、Android 13 (APIレベル 33) 以降では通知権限の確認を行い、
     * それ未満のバージョンでは権限確認をパスして時刻選択ダイアログを表示する。
     * 無効化の場合、そのまま保存する。
     *
     * @param enable 有効にする場合はtrue
     */
    private suspend fun startReminderNotificationSettingSaveProcess(enable: Boolean) {
        if (enable) {
            // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                emitUiEvent(
                    SettingsUiEvent.CheckPostNotificationsPermission
                )
            } else {
                showReminderNotificationTimePickerDialog()
            }
        } else {
            saveReminderNotificationInvalidSetting()
        }
    }

    /**
     * リマインダ通知時間選択ダイアログを表示する（イベント発行）。
     */
    private suspend fun showReminderNotificationTimePickerDialog() {
        emitUiEvent(
            SettingsUiEvent.ShowReminderNotificationTimePickerDialog
        )
    }

    /**
     * リマインダー通知を有効にする設定を保存する。
     * @param time 保存する通知時刻
     */
    private suspend fun saveReminderNotificationValidSetting(time: LocalTime) {
        val result =
            updateReminderNotificationSettingUseCase(
                ReminderNotificationSetting.Enabled(time)
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

    /** リマインダー通知を無効にする設定を保存する。 */
    private suspend fun saveReminderNotificationInvalidSetting() {
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
    //endregion

    //region Passcode Lock Setting Operation
    /**
     * パスコードロック設定スイッチの状態を変更する（イベント発行）。
     * @param isChecked 有効状態にする場合はtrue
     */
    private suspend fun changePasscodeLockSettingSwitch(isChecked: Boolean) {
        emitUiEvent(
            SettingsUiEvent.TurnPasscodeLockSettingSwitch(isChecked)
        )
    }

    /**
     * パスコードロック設定を保存する。
     * @param value パスコードロックを有効にする場合はtrue
     */
    private suspend fun savePasscodeLockSetting(value: Boolean) {
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
        when (val result = updatePasscodeLockSettingUseCase(setting)) {
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
    //endregion

    //region Weather Info Fetch Setting Operation
    /**
     * 天気情報取得設定スイッチの状態を変更する（イベント発行）。
     * @param isChecked 有効状態にする場合はtrue
     */
    private suspend fun changeWeatherInfoFetchSettingSwitch(isChecked: Boolean) {
        emitUiEvent(
            SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(isChecked)
        )
    }

    /**
     * 天気情報取得設定の保存プロセスを開始する。
     * 有効化の場合、位置情報権限有無の確認をする（イベント発行）。
     * 無効化の場合、そのまま保存する。
     *
     * @param enable 有効にする場合はtrue
     */
    private suspend fun startWeatherInfoFetchSettingSaveProcess(enable: Boolean) {
        if (enable) {
            emitUiEvent(
                SettingsUiEvent.CheckAccessLocationPermission
            )
        } else {
            saveWeatherInfoFetchSetting(false)
        }
    }

    /**
     * 天気情報取得設定を保存する。
     * @param isEnabled 天気情報取得を有効にする場合はtrue
     */
    private suspend fun saveWeatherInfoFetchSetting(isEnabled: Boolean) {
        val result =
            updateWeatherInfoFetchSettingUseCase(
                WeatherInfoFetchSetting(isEnabled)
            )
        when (result) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                emitUiEvent(SettingsUiEvent.TurnWeatherInfoFetchSettingSwitch(!isEnabled))
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
    //endregion

    //region App Data Operation
    /**
     * 全ての日記の削除確認ダイアログを表示する（イベント発行）。
     */
    private suspend fun showAllDiariesDeleteDialog() {
        emitUiEvent(
            SettingsUiEvent.ShowAllDiariesDeleteDialog
        )
    }

    /** 全ての日記を削除する。 */
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

    /**
     * 全ての設定の初期化確認ダイアログを表示する（イベント発行）。
     */
    private suspend fun showAllSettingsInitializationDialog() {
        emitUiEvent(
            SettingsUiEvent.ShowAllSettingsInitializationDialog
        )
    }

    /** 全ての設定を初期化する。 */
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

    /**
     * 全てのアプリケーションデータの削除確認ダイアログを表示する（イベント発行）。
     */
    private suspend fun showAllDataDeleteDialog() {
        emitUiEvent(
            SettingsUiEvent.ShowAllDataDeleteDialog
        )
    }

    /** 全てのアプリケーションデータを削除する。 */
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

    //region Standalone Navigation
    /**
     * 前の画面へ遷移する（イベント発行）。
     */
    private suspend fun navigatePreviousScreen() {
        emitNavigatePreviousFragmentEvent()
    }

    /**
     * OSSライセンス表示ダイアログを表示する（イベント発行）。
     */
    private suspend fun showOssLicensesScreen() {
        emitUiEvent(
            SettingsUiEvent.ShowOSSLicensesDialog
        )
    }

    // TODO:不要だが残しておく(最終的に削除)
    /**
     * アプリケーションの詳細設定画面を表示する（イベント発行）。
     */
    private suspend fun showApplicationDetailsSettings() {
        emitUiEvent(
            SettingsUiEvent.ShowApplicationDetailsSettingsScreen
        )
    }
    //endregion

    //endregion

    //region UI State Update
    /**
     * テーマカラーを更新する。
     * @param themeColor 新しいテーマカラー
     */
    private fun updateThemeColor(themeColor: ThemeColorUi?) {
        updateUiState { it.copy(themeColor = themeColor) }
    }

    /**
     * 週の開始曜日を更新する。
     * @param dayOfWeek 新しい週の開始曜日
     */
    private fun updateCalendarStartDayOfWeek(dayOfWeek: DayOfWeek?) {
        updateUiState { it.copy(calendarStartDayOfWeek = dayOfWeek) }
    }

    /**
     * 天気情報取得設定を更新する。
     * @param isEnabled 天気情報取得を有効にする場合はtrue
     */
    private fun updateIsWeatherFetchEnabled(isEnabled: Boolean?) {
        updateUiState { it.copy(isWeatherFetchEnabled = isEnabled) }
    }

    /** UIをアイドル状態（操作可能）に更新する。 */
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** UIを処理中の状態（操作不可）に更新する。 */
    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /** UIを読込失敗の状態に更新する。 */
    private fun updateToSettingsLoadFailedState() {
        updateUiState { it.copy(hasSettingsLoadFailure = true) }
    }

    /**
     * リマインダー通知を有効な状態に更新する。
     * @param notificationTime 更新する通知時刻
     * */
    private fun updateToReminderEnabledState(notificationTime: LocalTime) {
        updateUiState {
            it.copy(
                isReminderEnabled = true,
                reminderNotificationTime = notificationTime
            )
        }
    }

    /** リマインダー通知を無効な状態に更新する。 */
    private fun updateToReminderDisabledState() {
        updateUiState {
            it.copy(
                isReminderEnabled = false,
                reminderNotificationTime = null
            )
        }
    }

    /**
     * パスコードロックを有効な状態に更新する。
     * @param passcode パスコード文字列
     */
    private fun updateToPasscodeEnabledState(passcode: String) {
        updateUiState {
            it.copy(
                isPasscodeLockEnabled = true,
                passcode = passcode
            )
        }
    }

    /** パスコードロックを無効な状態に更新する。 */
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
