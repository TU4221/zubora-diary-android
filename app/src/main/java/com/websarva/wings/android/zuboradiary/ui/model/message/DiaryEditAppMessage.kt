package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryEditFragment
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 日記編集画面([DiaryEditFragment])で表示される、固有のアプリケーションメッセージ。
 */
@Parcelize
sealed interface DiaryEditAppMessage : AppMessage {

    /**
     * 予期せぬエラーが発生したことを示すメッセージ。
     * @property exception 発生した例外。デバッグビルドの場合、メッセージに例外名が含まれる。
     */
    class Unexpected(val exception: Exception) : DiaryEditAppMessage {

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

    /** 日記の読み込みに失敗したことを示すメッセージ。 */
    data object DiaryLoadFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_load_failure
    }

    /** 日記の保存に失敗したことを示すメッセージ。 */
    data object DiarySaveFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_failure
    }

    /** ストレージ容量不足により日記の保存に失敗したことを示すメッセージ。 */
    data object DiarySaveInsufficientStorageFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_save_insufficient_storage_failure
    }

    /** 日記の削除に失敗したことを示すメッセージ。 */
    data object DiaryDeleteFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_delete_failure
    }

    /** 添付画像の削除に失敗したことを示すメッセージ。 */
    data object DiaryImageDeleteFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_image_delete_failure
    }

    /** 日記情報の読み込みに失敗したことを示すメッセージ。 */
    data object DiaryInfoLoadFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_diary_info_load_failure
    }

    /** 天気情報の取得に失敗したことを示すメッセージ。 */
    data object WeatherInfoFetchFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_connection_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_fetch_failure
    }

    /** 天気情報を取得可能な期間外であることを示すメッセージ。 */
    data object WeatherInfoDateOutOfRange : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_hint
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_weather_info_date_out_of_range
    }

    /** 位置情報へのアクセス許可が必要であることを示すメッセージ。 */
    data object AccessLocationPermissionRequest : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_permission_request
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_access_location_permission_request
    }

    /** 添付画像の読み込みに失敗したことを示すメッセージ。 */
    data object ImageLoadFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_failure
    }

    /** ストレージ容量不足により添付画像の読み込みに失敗したことを示すメッセージ。 */
    data object ImageLoadInsufficientStorageFailure : DiaryEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_storage_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_edit_app_message_image_load_insufficient_storage_failure
    }
}
