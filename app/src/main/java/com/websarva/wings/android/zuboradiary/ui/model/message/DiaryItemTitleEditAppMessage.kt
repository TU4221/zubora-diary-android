package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class DiaryItemTitleEditAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object ItemTitleHistoryLoadFailure :  DiaryItemTitleEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_item_title_edit_app_message_selection_history_load_failure
    ) {
        private fun readResolve(): Any = ItemTitleHistoryLoadFailure
    }


    data object ItemTitleHistoryDeleteFailure :  DiaryItemTitleEditAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure
    ) {
        private fun readResolve(): Any = ItemTitleHistoryDeleteFailure
    }
}
