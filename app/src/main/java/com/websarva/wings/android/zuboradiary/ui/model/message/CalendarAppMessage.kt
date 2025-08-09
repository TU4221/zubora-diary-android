package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R

internal sealed class CalendarAppMessage(
    dialogTitleStringResId: Int,
    dialogMessageStringResId: Int
) : AppMessage(dialogTitleStringResId, dialogMessageStringResId) {

    data object DiaryLoadFailure :  CalendarAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_calendar_app_message_diary_load_failure
    ) {
        private fun readResolve(): Any = DiaryLoadFailure
    }

    data object DiaryInfoLoadFailure :  CalendarAppMessage(
        R.string.dialog_app_message_title_access_error,
        R.string.dialog_calendar_app_message_diary_info_load_failure
    ) {
        private fun readResolve(): Any = DiaryInfoLoadFailure
    }

}
