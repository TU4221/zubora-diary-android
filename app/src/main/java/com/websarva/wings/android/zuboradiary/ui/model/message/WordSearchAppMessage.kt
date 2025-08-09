package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class WordSearchAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object SearchResultListLoadFailure :  WordSearchAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_word_search_app_message_search_result_list_load_failure
    ) {
        private fun readResolve(): Any = SearchResultListLoadFailure
    }
}
