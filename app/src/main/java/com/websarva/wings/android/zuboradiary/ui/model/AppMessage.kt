package com.websarva.wings.android.zuboradiary.ui.model

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.io.Serializable

internal sealed class AppMessage(
    private val dialogTitleStringResId: Int,
    private val dialogMessageStringResId: Int
) : Serializable {
    fun getDialogTitle(context: Context): String {
        return context.getString(dialogTitleStringResId)
    }

    fun getDialogMessage(context: Context): String {
        return context.getString(dialogMessageStringResId)
    }
}

internal sealed class DiaryListAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryListLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_list_load_failure
    ) {
        private fun readResolve(): Any = DiaryListLoadFailure
    }

    data object DiaryDeleteFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_delete_failure
    ) {
        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryInfoLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_info_load_failure
    ) {
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }
}

internal sealed class WordSearchAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object SearchResultListLoadFailure :  WordSearchAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_word_search_app_message_search_result_list_load_failure
    ) {
        private fun readResolve(): Any = SearchResultListLoadFailure
    }
}

internal sealed class CalendarAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryLoadFailure :  CalendarAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_calendar_app_message_diary_load_failure
    ) {
        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiaryInfoLoadFailure :  CalendarAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_calendar_app_message_diary_info_load_failure
    ) {
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }

}

internal sealed class SettingsAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object SettingLoadFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_setting_load_failure
    ) {
        private fun readResolve(): Any = SettingLoadFailure
    }

    data object SettingsNotLoadedRetryRestart :  SettingsAppMessage(
        R.string.dialog_app_message_title_hint,
        R.string.dialog_settings_app_message_settings_not_loaded_retry_restart
    ) {
        private fun readResolve(): Any = SettingLoadFailure
    }

    data object SettingUpdateFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_setting_update_failure
    ) {
        private fun readResolve(): Any = SettingUpdateFailure
    }

    data object AllDiaryDeleteFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_diary_delete_failure
    ) {
        private fun readResolve(): Any = AllDiaryDeleteFailure
    }

    data object AllSettingsInitializationFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_settings_initialization_failure
    ) {
        private fun readResolve(): Any = SettingUpdateFailure
    }

    data object AllDataDeleteFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_data_delete_failure
    ) {
        private fun readResolve(): Any = AllDataDeleteFailure
    }
}

internal sealed class DiaryShowAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryLoadFailure :  DiaryShowAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_show_app_message_diary_load_failure
    ) {
        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiaryDeleteFailure :  DiaryShowAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_show_app_message_diary_delete_failure
    ) {
        private fun readResolve(): Any = DiaryDeleteFailure
    }

}

internal sealed class DiaryEditAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryLoadFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_load_failure
    ) {
        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiarySaveFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_save_failure
    ) {
        private fun readResolve(): Any = DiarySaveFailure
    }

    data object DiaryDeleteFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_delete_failure
    ) {
        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryInfoLoadFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_info_load_failure
    ) {
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }

    data object WeatherInfoFetchFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_connection_error,
        R.string.dialog_diary_edit_app_message_weather_info_fetch_failure
    ) {
        private fun readResolve(): Any = WeatherInfoFetchFailure
    }

    data object WeatherInfoDateOutOfRange :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_hint,
        R.string.dialog_diary_edit_app_message_weather_info_date_out_of_range
    ) {
        private fun readResolve(): Any = WeatherInfoFetchFailure
    }

    data object AccessLocationPermissionRequest :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_permission_request,
        R.string.dialog_diary_edit_app_message_access_location_permission_request
    ) {
        private fun readResolve(): Any = AccessLocationPermissionRequest
    }
}

internal sealed class DiaryItemTitleEditAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object ItemTitleHistoryLoadFailure :  DiaryItemTitleEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_item_title_edit_app_message_selection_history_load_failure
    ) {
        private fun readResolve(): Any = ItemTitleHistoryLoadFailure
    }


    data object ItemTitleHistoryDeleteFailure :  DiaryItemTitleEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure
    ) {
        private fun readResolve(): Any = ItemTitleHistoryDeleteFailure
    }
}
