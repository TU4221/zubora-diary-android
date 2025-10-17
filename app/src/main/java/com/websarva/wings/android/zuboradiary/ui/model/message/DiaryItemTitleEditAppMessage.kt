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
    }

    data object ItemTitleHistoryDeleteFailure : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_item_title_edit_app_message_selection_history_item_delete_failure
    }

    class Unexpected(val exception: Exception) : DiaryItemTitleEditAppMessage() {
        @IgnoredOnParcel
        private val delegate = UnexpectedMessageDelegate(exception)

        @IgnoredOnParcel
        override val dialogTitleStringResId: Int
            get() = delegate.dialogTitleStringResId

        @IgnoredOnParcel
        override val dialogMessageStringResId: Int
            get() = delegate.dialogMessageStringResId

        @IgnoredOnParcel
        override val dialogMessageArgs: Array<Any>
            get() = delegate.dialogMessageArgs
    }
}
