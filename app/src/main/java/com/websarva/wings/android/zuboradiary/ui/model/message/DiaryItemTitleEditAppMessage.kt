package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryItemTitleEditAppMessage : AppMessage {

    data object ItemTitleHistoryLoadFailure : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_load_failure

        private fun readResolve(): Any = ItemTitleHistoryLoadFailure
    }


    data object ItemTitleHistoryDeleteFailure : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure

        private fun readResolve(): Any = ItemTitleHistoryDeleteFailure
    }
}
