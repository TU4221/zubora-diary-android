package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.DiaryItemTitleEditDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 日記項目タイトル編集ダイアログ([DiaryItemTitleEditDialog])で表示される、
 * 固有のアプリケーションメッセージを表すsealed class。
 */
@Parcelize
sealed class DiaryItemTitleEditAppMessage : AppMessage {

    /** 項目タイトル履歴の読み込みに失敗したことを示すメッセージ。 */
    data object ItemTitleHistoryLoadFailure : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_load_failure
    }

    /** 項目タイトル履歴の削除に失敗したことを示すメッセージ。 */
    data object ItemTitleHistoryDeleteFailure : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure
    }
}
