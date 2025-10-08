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

        private fun readResolve(): Any = SettingLoadFailure
    }

    data object SettingsNotLoadedRetryRestart : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_settings_not_loaded_retry_restart

        private fun readResolve(): Any = SettingsNotLoadedRetryRestart
    }

    data object SettingUpdateFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_failure

        private fun readResolve(): Any = SettingUpdateFailure
    }

    data object SettingUpdateInsufficientStorageFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_insufficient_storage_failure

        private fun readResolve(): Any = SettingUpdateInsufficientStorageFailure
    }

    data object AllDiaryDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_delete_failure

        private fun readResolve(): Any = AllDiaryDeleteFailure
    }

    data object AllDiaryImagesDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_images_failure

        private fun readResolve(): Any = AllDiaryImagesDeleteFailure
    }

    data object AllSettingsInitializationFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_failure

        private fun readResolve(): Any = AllSettingsInitializationFailure
    }

    data object AllSettingsInitializationInsufficientStorageFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_insufficient_storage_failure

        private fun readResolve(): Any = AllSettingsInitializationInsufficientStorageFailure
    }

    data object AllDataDeleteFailure : SettingsAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_data_delete_failure

        private fun readResolve(): Any = AllDataDeleteFailure
    }
}
