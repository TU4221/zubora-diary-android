package com.websarva.wings.android.zuboradiary.ui.model.event

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

sealed class SettingsUiEvent : UiEvent {
    data object NavigateThemeColorPickerDialog : SettingsUiEvent()
    data class NavigateCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsUiEvent()
    data object NavigateReminderNotificationTimePickerDialog : SettingsUiEvent()
    data object NavigateNotificationPermissionDialog : SettingsUiEvent()
    data object NavigateLocationPermissionDialog : SettingsUiEvent()
    data object NavigateAllDiariesDeleteDialog : SettingsUiEvent()
    data object NavigateAllSettingsInitializationDialog : SettingsUiEvent()
    data object NavigateAllDataDeleteDialog : SettingsUiEvent()
    data object NavigateOpenSourceLicensesFragment : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckPostNotificationsPermission : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object ShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()
    data object CheckAccessLocationPermission : SettingsUiEvent()
    data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsUiEvent()
    data object ShowRequestAccessLocationPermissionRationale : SettingsUiEvent()
    data class TurnReminderNotificationSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    data class TurnPasscodeLockSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    data class TurnWeatherInfoFetchSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    data object ShowApplicationDetailsSettings : SettingsUiEvent()
}
