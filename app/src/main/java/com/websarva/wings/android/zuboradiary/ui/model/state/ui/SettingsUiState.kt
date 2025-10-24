package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

@Parcelize
internal data class SettingsUiState(
    val themeColor: ThemeColorUi? = null,
    val calendarStartDayOfWeek: DayOfWeek? = null,
    val isReminderEnabled: Boolean? = null,
    val reminderNotificationTime: LocalTime? = null,
    val isPasscodeLockEnabled: Boolean? = null,
    val passcode: String? = null,
    val isWeatherFetchEnabled: Boolean? = null,
    val hasError: Boolean = false,
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
