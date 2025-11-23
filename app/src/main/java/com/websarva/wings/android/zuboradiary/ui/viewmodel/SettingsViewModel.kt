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
            emitNavigatePreviousFragmentEvent(null)
        }
    }

    // TODO:Button -> Itemに名称変更
    /**
     * テーマカラー設定項目がクリックされた時に呼び出される事を想定。
     * テーマカラー選択ダイアログへ遷移するイベントを発行する。
     */
    fun onThemeColorSettingButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateThemeColorPickerDialog
            )
        }
    }

    /**
     * カレンダー開始曜日設定項目がクリックされた時に呼び出される事を想定。
     * カレンダー開始曜日選択ダイアログへ遷移するイベントを発行する。
     */
    fun onCalendarStartDayOfWeekSettingButtonClick() {
        if (!canExecuteOperation()) return

        val dayOfWeek = currentUiState.calendarStartDayOfWeek ?: return
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateCalendarStartDayPickerDialog(dayOfWeek)
            )
        }
    }

    /**
     * リマインダー通知設定のスイッチが切り替えられた時に呼び出される事を想定。
     * 権限確認または設定保存の処理を開始する。
     * @param isChecked スイッチがONになった場合はtrue
     */
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

    /**
     * 天気情報取得設定のスイッチが切り替えられた時に呼び出される事を想定。
     * 権限確認または設定保存の処理を開始する。
     * @param isChecked スイッチがONになった場合はtrue
     */
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

    /**
     * 全日記削除項目がクリックされた時に呼び出される事を想定。
     * 全日記削除確認ダイアログへ遷移するイベントを発行する。
     */
    fun onAllDiariesDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllDiariesDeleteDialog
            )
        }
    }

    /**
     * 全設定初期化項目がクリックされた時に呼び出される事を想定。
     * 全設定初期化確認ダイアログへ遷移するイベントを発行する。
     */
    fun onAllSettingsInitializationButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllSettingsInitializationDialog
            )
        }
    }

    /**
     * 全データ削除項目がクリックされた時に呼び出される事を想定。
     * 全データ削除確認ダイアログへ遷移するイベントを発行する。
     */
    fun onAllDataDeleteButtonClick() {
        if (!canExecuteOperation()) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateAllDataDeleteDialog
            )
        }
    }

    /**
     * オープンソースソフトウェアライセンス項目がクリックされた時に呼び出される事を想定。
     * OSSライセンス表示画面へ遷移するイベントを発行する。
     */
    fun onOpenSourceLicenseButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.NavigateOSSLicensesDialog
            )
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
            saveThemeColor(themeColor.toDomainModel())
        }
    }

    /**
     * カレンダー開始曜日選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 週の開始曜日設定を保存する。
     * @param dayOfWeek 選択された週の開始曜日
     */
    internal fun onCalendarStartDayOfWeekSettingDialogPositiveResultReceived(dayOfWeek: DayOfWeek) {
        launchWithUnexpectedErrorHandler {
            saveCalendarStartDayOfWeek(dayOfWeek)
        }
    }

    /**
     * リマインダー通知時間選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * リマインダー通知を有効にする設定を保存する。
     * @param time 保存する通知時刻
     * */
    internal fun onReminderNotificationSettingDialogPositiveResultReceived(time: LocalTime) {
        launchWithUnexpectedErrorHandler {
            saveReminderNotificationValid(time)
        }
    }

    /**
     * リマインダー通知時間選択ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * リマインダー通知設定スイッチをOffにする。
     * */
    internal fun onReminderNotificationSettingDialogNegativeResultReceived() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.TurnReminderNotificationSettingSwitch(false)
            )
        }
    }

    /**
     * 全日記削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全日記の削除を実行する。
     */
    internal fun onAllDiariesDeleteDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            deleteAllDiaries()
        }
    }

    /**
     * 全設定初期化確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全設定の初期化を実行する。
     */
    internal fun onAllSettingsInitializationDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            initializeAllSettings()
        }
    }

    /**
     * 全データ削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 全データの削除を実行する。
     */
    internal fun onAllDataDeleteDialogResultPositiveReceived() {
        launchWithUnexpectedErrorHandler {
            deleteAllData()
        }
    }

    /**
     * 権限確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * アプリケーションの詳細設定画面を表示する。
     */
    internal fun onPermissionDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                SettingsUiEvent.ShowApplicationDetailsSettings
            )
        }
    }
    //endregion

    //region UI Event Handlers - Permissions
    /**
     * 通知権限が許可されているかどうかの確認結果を受け取った時に呼び出される事を想定。
     * 権限の状態に応じて、ダイアログ表示や時刻選択ダイアログへの遷移を行う。
     * @param isGranted 通知権限が許可されている場合はtrue
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    internal fun onPostNotificationsPermissionChecked(isGranted: Boolean) {
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

    /**
     * 通知権限要求を表示する必要があるかどうかの確認結果を受け取った時に呼び出される事を想定。
     * 結果に応じて、権限要求の表示またはアプリ設定画面への遷移を促すダイアログを表示する。
     * @param shouldShowRequest 理由を表示する必要がある場合はtrue
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    internal fun onShouldShowRequestPostNotificationsPermissionRationaleChecked(
        shouldShowRequest: Boolean
    ) {
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

    /**
     * 通知権限要求ダイアログの表示結果を受け取った時に呼び出される事を想定。
     * 権限が許可された場合は時刻選択ダイアログへ遷移し、そうでなければスイッチをOFFに戻す。
     * @param isGranted 権限が許可された場合はtrue
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    internal fun onRequestPostNotificationsPermissionRationaleResultReceived(isGranted: Boolean) {
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

    /**
     * 位置情報権限が許可されているかどうかの確認結果を受け取った時に呼び出される事を想定。
     * 権限の状態に応じて、設定の保存または権限要求を行う。
     * @param isGranted 位置情報権限が許可されている場合はtrue
     */
    internal fun onAccessLocationPermissionChecked(isGranted: Boolean) {
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

    /**
     * 位置情報権限要求を表示する必要があるかどうかの確認結果を受け取った時に呼び出される事を想定。
     * 結果に応じて、権限要求の表示またはアプリ設定画面への遷移を促すダイアログを表示する。
     * @param shouldShowRequest 理由を表示する必要がある場合はtrue
     */
    internal fun onShouldShowRequestAccessLocationPermissionRationaleChecked(
        shouldShowRequest: Boolean
    ) {
        launchWithUnexpectedErrorHandler {
            if (shouldShowRequest) {
                emitUiEvent(
                    SettingsUiEvent.ShowRequestAccessLocationPermissionRationale
                )
            } else {
                emitUiEvent(SettingsUiEvent.NavigateLocationPermissionDialog)
            }
        }
    }

    /**
     * 位置情報権限要求ダイアログの表示結果を受け取った時に呼び出される事を想定。
     * 権限が許可されたかどうかを保存し、UIに反映させる。
     * @param isGranted 権限が許可された場合はtrue
     */
    internal fun onRequestAccessLocationPermissionRationaleResultReceived(isGranted: Boolean) {
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

    /**
     * OSの通知権限の状態をリマインダー設定に同期させるために呼び出される事を想定。
     * 権限がないのに設定がONになっている場合は、設定をOFFに更新する。
     * @param isGranted OSの通知権限が許可されている場合はtrue
     */
    // MEMO:端末設定画面で"許可 -> 無許可"に変更したときの対応コード
    internal fun onEnsureReminderNotificationSettingMatchesPermission(isGranted: Boolean) {
        launchWithUnexpectedErrorHandler {
            if (isGranted) return@launchWithUnexpectedErrorHandler

            saveReminderNotificationInvalid()
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

            saveWeatherInfoFetch(false)
        }
    }
    //endregion

    //region Business Logic
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

    /**
     * テーマカラー設定を保存する。
     * @param themeColor 保存するテーマカラー
     */
    private suspend fun saveThemeColor(themeColor: ThemeColor) {
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

    /**
     * 週の開始曜日設定を保存する。
     * @param dayOfWeek 保存する週の開始曜日
     */
    private suspend fun saveCalendarStartDayOfWeek(dayOfWeek: DayOfWeek) {
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

    /**
     * リマインダー通知を有効にする設定を保存する。
     * @param time 保存する通知時刻
     */
    private suspend fun saveReminderNotificationValid(time: LocalTime) {
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

    /**
     * パスコードロック設定を保存する。
     * @param value パスコードロックを有効にする場合はtrue
     */
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

    /**
     * 天気情報取得設定を保存する。
     * @param isEnabled 天気情報取得を有効にする場合はtrue
     */
    private suspend fun saveWeatherInfoFetch(isEnabled: Boolean) {
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
