package com.websarva.wings.android.zuboradiary.ui.model.event

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

sealed class SettingsUiEvent : UiEvent {
    internal data object NavigateThemeColorPickerDialog : SettingsUiEvent()
    internal data class NavigateCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsUiEvent()
    internal data object NavigateReminderNotificationTimePickerDialog : SettingsUiEvent()
    internal data object NavigateNotificationPermissionDialog : SettingsUiEvent()
    internal data object NavigateLocationPermissionDialog : SettingsUiEvent()
    internal data object NavigateAllDiariesDeleteDialog : SettingsUiEvent()
    internal data object NavigateAllSettingsInitializationDialog : SettingsUiEvent()
    internal data object NavigateAllDataDeleteDialog : SettingsUiEvent()
    internal data object NavigateOpenSourceLicensesFragment : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object CheckPostNotificationsPermission : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object ShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()
    internal data object CheckAccessLocationPermission : SettingsUiEvent()
    internal data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsUiEvent()
    internal data object ShowRequestAccessLocationPermissionRationale : SettingsUiEvent()
    internal data class TurnReminderNotificationSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    internal data class TurnPasscodeLockSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    internal data class TurnWeatherInfoFetchSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
    internal data object ShowApplicationDetailsSettings : SettingsUiEvent()
}
