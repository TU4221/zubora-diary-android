package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * [MainActivity]で表示される、固有のアプリケーションメッセージを表すsealed class。
 */
@Parcelize
sealed class MainActivityAppMessage : AppMessage {

    /** 設定情報の読み込みに失敗したことを示すメッセージ。 */
    data object SettingsLoadFailure : MainActivityAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_main_activity_app_message_setting_load_failure
    }
}
