package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.SettingsFragment
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 設定画面([SettingsFragment])のUI状態を表すデータクラス。
 *
 * @property themeColor 現在のテーマカラー。
 * @property calendarStartDayOfWeek カレンダーの週の開始曜日。
 * @property isReminderEnabled リマインダー通知が有効かを示す。
 * @property reminderNotificationTime リマインダーの通知時刻。
 * @property isPasscodeLockEnabled パスコードロックが有効かを示す。
 * @property passcode 設定されているパスコード。
 * @property isWeatherFetchEnabled 天気情報取得が有効かを示す。
 *
 * @property hasSettingsLoadFailure 設定の読み込みに失敗したかを示す。
 *
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
@Parcelize
data class SettingsUiState(
    // UiData
    val themeColor: ThemeColorUi? = null,
    val calendarStartDayOfWeek: DayOfWeek? = null,
    val isReminderEnabled: Boolean? = null,
    val reminderNotificationTime: LocalTime? = null,
    val isPasscodeLockEnabled: Boolean? = null,
    val passcode: String? = null,
    val isWeatherFetchEnabled: Boolean? = null,

    // UiState
    val hasSettingsLoadFailure: Boolean = false,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, Parcelable
