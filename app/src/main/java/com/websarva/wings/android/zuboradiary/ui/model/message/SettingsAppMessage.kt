package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.SettingsFragment
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 設定画面([SettingsFragment])で表示される、固有のアプリケーションメッセージ。
 */
@Parcelize
sealed interface SettingsAppMessage : AppMessage {

    /**
     * 予期せぬエラーが発生したことを示すメッセージ。
     * @property exception 発生した例外。デバッグビルドの場合、メッセージに例外名が含まれる。
     */
    class Unexpected(val exception: Exception) : SettingsAppMessage {

        @IgnoredOnParcel
        override val dialogTitleStringResId = R.string.dialog_app_message_title_unexpected_error

        @IgnoredOnParcel
        override val dialogMessageStringResId =
            if (BuildConfig.DEBUG) {
                R.string.dialog_app_message_developer_unexpected_error
            } else {
                R.string.dialog_app_message_unexpected_error
            }

        @IgnoredOnParcel
        override val dialogMessageArgs: Array<Any> =
            if (BuildConfig.DEBUG) {
                arrayOf(exception.javaClass.simpleName)
            } else {
                emptyArray()
            }
    }

    /** 設定情報の読み込みに失敗したことを示すメッセージ。 */
    data object SettingLoadFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_load_failure
    }

    /** 設定が読み込まれていないため、再起動を促すメッセージ。 */
    data object SettingsNotLoadedRetryRestart : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_settings_not_loaded_retry_restart
    }

    /** 設定の更新に失敗したことを示すメッセージ。 */
    data object SettingUpdateFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_failure
    }

    /** ストレージ容量不足により設定の更新に失敗したことを示すメッセージ。 */
    data object SettingUpdateInsufficientStorageFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_setting_update_insufficient_storage_failure
    }

    /** 全日記の削除に失敗したことを示すメッセージ。 */
    data object AllDiaryDeleteFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_delete_failure
    }

    /** 全ての添付画像の削除に失敗したことを示すメッセージ。 */
    data object AllDiaryImagesDeleteFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_diary_images_failure
    }

    /** 全設定の初期化に失敗したことを示すメッセージ。 */
    data object AllSettingsInitializationFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_failure
    }

    /** ストレージ容量不足により全設定の初期化に失敗したことを示すメッセージ。 */
    data object AllSettingsInitializationInsufficientStorageFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_settings_initialization_insufficient_storage_failure
    }

    /** 全データの削除に失敗したことを示すメッセージ。 */
    data object AllDataDeleteFailure : SettingsAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_settings_app_message_all_data_delete_failure
    }
}
