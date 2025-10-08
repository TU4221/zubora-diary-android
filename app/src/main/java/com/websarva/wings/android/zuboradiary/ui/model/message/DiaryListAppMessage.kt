package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryListAppMessage : AppMessage {

    data object DiaryListLoadFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_list_load_failure

        private fun readResolve(): Any = DiaryListLoadFailure
    }

    data object DiaryDeleteFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_delete_failure

        private fun readResolve(): Any = DiaryDeleteFailure
    }

    data object DiaryImageDeleteFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_image_delete_failure

        private fun readResolve(): Any = DiaryImageDeleteFailure
    }

    data object DiaryInfoLoadFailure : DiaryListAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_list_app_message_diary_info_load_failure

        private fun readResolve(): Any = DiaryInfoLoadFailure
    }
}
