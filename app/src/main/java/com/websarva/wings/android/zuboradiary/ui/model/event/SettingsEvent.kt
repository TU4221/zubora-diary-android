package com.websarva.wings.android.zuboradiary.ui.model.event

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

sealed class SettingsEvent : UiEvent {
    internal data object NavigateThemeColorPickerDialog : SettingsEvent()
    internal data class NavigateCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsEvent()
    internal data object NavigateReminderNotificationTimePickerDialog : SettingsEvent()
    internal data object NavigateNotificationPermissionDialog : SettingsEvent()
    internal data object NavigateLocationPermissionDialog : SettingsEvent()
    internal data object NavigateAllDiariesDeleteDialog : SettingsEvent()
    internal data object NavigateAllSettingsInitializationDialog : SettingsEvent()
    internal data object NavigateAllDataDeleteDialog : SettingsEvent()
    internal data object NavigateOpenSourceLicensesFragment : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object CheckPostNotificationsPermission : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal data object ShowRequestPostNotificationsPermissionRationale : SettingsEvent()
    internal data object CheckAccessLocationPermission : SettingsEvent()
    internal data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsEvent()
    internal data object ShowRequestAccessLocationPermissionRationale : SettingsEvent()
    internal data object TurnOffReminderNotificationSettingSwitch : SettingsEvent()
    internal data object TurnOffPasscodeLockSettingSwitch : SettingsEvent()
    internal data object TurnOffWeatherInfoFetchSettingSwitch : SettingsEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : SettingsEvent()
}
