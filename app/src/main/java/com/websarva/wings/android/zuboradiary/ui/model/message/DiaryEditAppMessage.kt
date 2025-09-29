package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class DiaryEditAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryLoadFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiarySaveFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_save_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiarySaveFailure
    }

    data object DiaryDeleteFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryImageDeleteFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_image_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryImageDeleteFailure
    }

    data object DiaryInfoLoadFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_diary_info_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }

    data object WeatherInfoFetchFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_connection_error,
        R.string.dialog_diary_edit_app_message_weather_info_fetch_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = WeatherInfoFetchFailure
    }

    data object WeatherInfoDateOutOfRange :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_hint,
        R.string.dialog_diary_edit_app_message_weather_info_date_out_of_range
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = WeatherInfoFetchFailure
    }

    data object AccessLocationPermissionRequest :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_permission_request,
        R.string.dialog_diary_edit_app_message_access_location_permission_request
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = AccessLocationPermissionRequest
    }

    data object ImageLoadFailure :  DiaryEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_edit_app_message_image_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = ImageLoadFailure
    }
}
