package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class SettingsAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object SettingLoadFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_setting_load_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = SettingLoadFailure
    }

    data object SettingsNotLoadedRetryRestart :  SettingsAppMessage(
        R.string.dialog_app_message_title_hint,
        R.string.dialog_settings_app_message_settings_not_loaded_retry_restart
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = SettingLoadFailure
    }

    data object SettingUpdateFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_setting_update_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = SettingUpdateFailure
    }

    data object AllDiaryDeleteFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_diary_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = AllDiaryDeleteFailure
    }

    data object AllDiaryImagesDeleteFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_diary_images_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = AllDiaryDeleteFailure
    }

    data object AllSettingsInitializationFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_settings_initialization_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = SettingUpdateFailure
    }

    data object AllDataDeleteFailure :  SettingsAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_settings_app_message_all_data_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = AllDataDeleteFailure
    }
}
