package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 日記項目タイトル編集ダイアログで表示される、
 * 固有のアプリケーションメッセージ。
 */
@Parcelize
sealed interface DiaryItemTitleEditAppMessage : AppMessage {

    /**
     * 予期せぬエラーが発生したことを示すメッセージ。
     * @property exception 発生した例外。デバッグビルドの場合、メッセージに例外名が含まれる。
     */
    class Unexpected(val exception: Exception) : DiaryItemTitleEditAppMessage {

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

    /** 項目タイトル履歴の読み込みに失敗したことを示すメッセージ。 */
    data object ItemTitleHistoryLoadFailure : DiaryItemTitleEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_load_failure
    }

    /** 項目タイトル履歴の削除に失敗したことを示すメッセージ。 */
    data object ItemTitleHistoryDeleteFailure : DiaryItemTitleEditAppMessage {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure
    }
}
