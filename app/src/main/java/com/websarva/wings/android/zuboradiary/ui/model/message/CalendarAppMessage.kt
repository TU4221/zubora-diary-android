package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class CalendarAppMessage : AppMessage {

    data object DiaryLoadFailure : CalendarAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_calendar_app_message_diary_load_failure
    }

    data object DiaryInfoLoadFailure : CalendarAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_calendar_app_message_diary_info_load_failure
    }

}
