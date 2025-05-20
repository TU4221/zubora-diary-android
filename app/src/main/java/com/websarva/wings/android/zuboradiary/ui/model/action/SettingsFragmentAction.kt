package com.websarva.wings.android.zuboradiary.ui.model.action

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

internal sealed class SettingsFragmentAction : FragmentAction() {
    data object NavigateThemeColorPickerDialog : SettingsFragmentAction()
    data class NavigateCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsFragmentAction()
    data object NavigateReminderNotificationTimePickerDialog : SettingsFragmentAction()
    data object NavigateNotificationPermissionDialog : SettingsFragmentAction()
    data object NavigateLocationPermissionDialog : SettingsFragmentAction()
    data object NavigateAllDiariesDeleteDialog : SettingsFragmentAction()
    data object NavigateAllSettingsInitializationDialog : SettingsFragmentAction()
    data object NavigateAllDataDeleteDialog : SettingsFragmentAction()
    data object NavigateOpenSourceLicensesFragment : SettingsFragmentAction()
    data object ReleaseAllPersistablePermission : SettingsFragmentAction()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckPostNotificationsPermission : SettingsFragmentAction()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsFragmentAction()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object ShowRequestPostNotificationsPermissionRationale : SettingsFragmentAction()
    data object CheckAccessLocationPermission : SettingsFragmentAction()
    data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsFragmentAction()
    data object ShowRequestAccessLocationPermissionRationale : SettingsFragmentAction()
    data object TurnOffReminderNotificationSettingSwitch : SettingsFragmentAction()
    data object TurnOffWeatherInfoAcquisitionSettingSwitch : SettingsFragmentAction()
}
