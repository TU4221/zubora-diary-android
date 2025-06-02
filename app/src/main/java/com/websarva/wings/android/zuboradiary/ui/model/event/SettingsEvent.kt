package com.websarva.wings.android.zuboradiary.ui.model.event

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

internal sealed class SettingsEvent : ViewModelEvent() {
    data object NavigateThemeColorPickerDialog : SettingsEvent()
    data class NavigateCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsEvent()
    data object NavigateReminderNotificationTimePickerDialog : SettingsEvent()
    data object NavigateNotificationPermissionDialog : SettingsEvent()
    data object NavigateLocationPermissionDialog : SettingsEvent()
    data object NavigateAllDiariesDeleteDialog : SettingsEvent()
    data object NavigateAllSettingsInitializationDialog : SettingsEvent()
    data object NavigateAllDataDeleteDialog : SettingsEvent()
    data object NavigateOpenSourceLicensesFragment : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckPostNotificationsPermission : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsEvent()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object ShowRequestPostNotificationsPermissionRationale : SettingsEvent()
    data object CheckAccessLocationPermission : SettingsEvent()
    data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsEvent()
    data object ShowRequestAccessLocationPermissionRationale : SettingsEvent()
    data object TurnOffReminderNotificationSettingSwitch : SettingsEvent()
    data object TurnOffWeatherInfoAcquisitionSettingSwitch : SettingsEvent()
}
