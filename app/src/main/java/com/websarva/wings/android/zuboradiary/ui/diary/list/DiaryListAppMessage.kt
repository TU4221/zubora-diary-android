package com.websarva.wings.android.zuboradiary.ui.diary.list

import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.message.AppMessage
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 日記一覧画面で表示される、固有のアプリケーションメッセージ。
 */
@Parcelize
sealed class DiaryListAppMessage : AppMessage() {

    /**
     * 予期せぬエラーが発生したことを示すメッセージ。
     * @property exception 発生した例外。デバッグビルドの場合、メッセージに例外名が含まれる。
     */
    class Unexpected(val exception: Exception) : DiaryListAppMessage() {

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

    /** 日記リストの読み込みに失敗したことを示すメッセージ。 */
    data object DiaryListLoadFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_list_load_failure
    }

    /** 日記の削除に失敗したことを示すメッセージ。 */
    data object DiaryDeleteFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_delete_failure
    }

    /** 添付画像の削除に失敗したことを示すメッセージ。 */
    data object DiaryImageDeleteFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_image_delete_failure
    }

    /** 日記情報の読み込みに失敗したことを示すメッセージ。 */
    data object DiaryInfoLoadFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_info_load_failure
    }
}
