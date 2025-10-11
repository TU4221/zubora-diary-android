package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class WordSearchAppMessage : AppMessage {

    data object SearchResultListLoadFailure : WordSearchAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_word_search_app_message_search_result_list_load_failure
    }

    class Unexpected(val exception: Exception) : WordSearchAppMessage() {
        @IgnoredOnParcel
        private val delegate = UnexpectedMessageDelegate(exception)

        @IgnoredOnParcel
        override val dialogTitleStringResId: Int
            get() = delegate.dialogTitleStringResId

        @IgnoredOnParcel
        override val dialogMessageStringResId: Int
            get() = delegate.dialogMessageStringResId

        @IgnoredOnParcel
        override val dialogMessageArgs: List<Any>
            get() = delegate.dialogMessageArgs
    }
}
