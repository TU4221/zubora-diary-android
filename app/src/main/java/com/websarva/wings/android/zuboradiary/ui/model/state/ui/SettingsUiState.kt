package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

@Parcelize
internal data class SettingsUiState(
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
    val isProcessing: Boolean = false,
    val isInputDisabled: Boolean = false
) : UiState, Parcelable
