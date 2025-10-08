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

        private fun readResolve(): Any = SearchResultListLoadFailure
    }
}
