package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class DiaryShowAppMessage : AppMessage {

    data object DiaryDeleteFailure : DiaryShowAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_show_app_message_diary_delete_failure
    }

    data object DiaryImageDeleteFailure : DiaryShowAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_show_app_message_diary_image_delete_failure
    }

    class Unexpected(val exception: Exception) : DiaryShowAppMessage() {
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
