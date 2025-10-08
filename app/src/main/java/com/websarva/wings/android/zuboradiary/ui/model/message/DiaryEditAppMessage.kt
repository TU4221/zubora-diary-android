package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryEditAppMessage : AppMessage {

    data object DiaryLoadFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_load_failure

        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiarySaveFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_failure

        private fun readResolve(): Any = DiarySaveFailure
    }

    data object DiarySaveInsufficientStorageFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_insufficient_storage_failure

        private fun readResolve(): Any = DiarySaveInsufficientStorageFailure
    }

    data object DiaryDeleteFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_delete_failure

        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryImageDeleteFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_image_delete_failure

        private fun readResolve(): Any = DiaryImageDeleteFailure
    }

    data object DiaryInfoLoadFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_info_load_failure

        private fun readResolve(): Any = DiaryInfoLoadFailure
    }

    data object WeatherInfoFetchFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_connection_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_fetch_failure

        private fun readResolve(): Any = WeatherInfoFetchFailure
    }

    data object WeatherInfoDateOutOfRange : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_date_out_of_range

        private fun readResolve(): Any = WeatherInfoDateOutOfRange
    }

    data object AccessLocationPermissionRequest : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_permission_request
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_access_location_permission_request

        private fun readResolve(): Any = AccessLocationPermissionRequest
    }

    data object ImageLoadFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_failure

        private fun readResolve(): Any = ImageLoadFailure
    }

    data object ImageLoadInsufficientStorageFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_insufficient_storage_failure

        private fun readResolve(): Any = ImageLoadInsufficientStorageFailure
    }
}
