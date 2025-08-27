package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class DiaryShowAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryDeleteFailure :  DiaryShowAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_diary_show_app_message_diary_delete_failure
    ) {
        // デシリアライズ時のシングルトン性を維持
        private fun readResolve(): Any = DiaryDeleteFailure
    }
}
