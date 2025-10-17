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
    }

    data object DiarySaveFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_failure
    }

    data object DiarySaveInsufficientStorageFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_insufficient_storage_failure
    }

    data object DiaryDeleteFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_delete_failure
    }

    data object DiaryImageDeleteFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_image_delete_failure
    }

    data object DiaryInfoLoadFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_info_load_failure
    }

    data object WeatherInfoFetchFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_connection_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_fetch_failure
    }

    data object WeatherInfoDateOutOfRange : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_date_out_of_range
    }

    data object AccessLocationPermissionRequest : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_permission_request
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_access_location_permission_request
    }

    data object ImageLoadFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_failure
    }

    data object ImageLoadInsufficientStorageFailure : DiaryEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_insufficient_storage_failure
    }

    class Unexpected(val exception: Exception) : DiaryEditAppMessage() {
        @IgnoredOnParcel
        private val delegate = UnexpectedMessageDelegate(exception)

        @IgnoredOnParcel
        override val dialogTitleStringResId: Int
            get() = delegate.dialogTitleStringResId

        @IgnoredOnParcel
        override val dialogMessageStringResId: Int
            get() = delegate.dialogMessageStringResId

        @IgnoredOnParcel
        override val dialogMessageArgs: Array<Any>
            get() = delegate.dialogMessageArgs
    }
}
