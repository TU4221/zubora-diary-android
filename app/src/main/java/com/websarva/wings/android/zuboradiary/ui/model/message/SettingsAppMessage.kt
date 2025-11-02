package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class SettingsAppMessage : AppMessage {

    data object SettingLoadFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_load_failure
    }

    data object SettingsNotLoadedRetryRestart : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_settings_not_loaded_retry_restart
    }

    data object SettingUpdateFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_failure
    }

    data object SettingUpdateInsufficientStorageFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_insufficient_storage_failure
    }

    data object AllDiaryDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_delete_failure
    }

    data object AllDiaryImagesDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_images_failure
    }

    data object AllSettingsInitializationFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_failure
    }

    data object AllSettingsInitializationInsufficientStorageFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_insufficient_storage_failure
    }

    data object AllDataDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_data_delete_failure
    }
}
