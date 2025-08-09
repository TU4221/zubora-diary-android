package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class DiaryListAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryListLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_list_load_failure
    ) {
        private fun readResolve(): Any = DiaryListLoadFailure
    }

    data object DiaryDeleteFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_delete_failure
    ) {
        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryInfoLoadFailure :  DiaryListAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_list_app_message_diary_info_load_failure
    ) {
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }
}
