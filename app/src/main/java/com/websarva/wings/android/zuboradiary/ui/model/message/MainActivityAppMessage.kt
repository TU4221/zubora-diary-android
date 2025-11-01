package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class MainActivityAppMessage : AppMessage {

    data object SettingsLoadFailure : MainActivityAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_main_activity_app_message_setting_load_failure
    }

    class Unexpected(val exception: Exception) : MainActivityAppMessage() {
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
