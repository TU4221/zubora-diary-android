package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryShowFragment
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * 日記表示画面([DiaryShowFragment])で表示される、固有のアプリケーションメッセージを表すsealed class。
 */
@Parcelize
sealed class DiaryShowAppMessage : AppMessage {

    /** 日記の削除に失敗したことを示すメッセージ。 */
    data object DiaryDeleteFailure : DiaryShowAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_show_app_message_diary_delete_failure
    }

    /** 添付画像の削除に失敗したことを示すメッセージ。 */
    data object DiaryImageDeleteFailure : DiaryShowAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_diary_show_app_message_diary_image_delete_failure
    }
}
