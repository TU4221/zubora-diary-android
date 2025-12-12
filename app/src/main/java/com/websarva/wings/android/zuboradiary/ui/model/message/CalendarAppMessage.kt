package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.CalendarFragment
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * カレンダー画面([CalendarFragment])で表示される、固有のアプリケーションメッセージを表すsealed class。
 */
@Parcelize
sealed class CalendarAppMessage : AppMessage {

    /**
     * 予期せぬエラーが発生したことを示すメッセージ。
     * @property exception 発生した例外。デバッグビルドの場合、メッセージに例外名が含まれる。
     */
    class Unexpected(val exception: Exception) : CalendarAppMessage() {

        @IgnoredOnParcel
        override val dialogTitleStringResId = R.string.dialog_app_message_title_unexpected_error

        @IgnoredOnParcel
        override val dialogMessageStringResId =
            if (BuildConfig.DEBUG) {
                R.string.dialog_app_message_developer_unexpected_error
            } else {
                R.string.dialog_app_message_unexpected_error
            }

        @IgnoredOnParcel
        override val dialogMessageArgs: Array<Any> =
            if (BuildConfig.DEBUG) {
                arrayOf(exception.javaClass.simpleName)
            } else {
                emptyArray()
            }
    }

    /** 日記の読み込みに失敗したことを示すメッセージ。 */
    data object DiaryLoadFailure : CalendarAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_calendar_app_message_diary_load_failure
    }

    /** 日記情報の読み込みに失敗したことを示すメッセージ。 */
    data object DiaryInfoLoadFailure : CalendarAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_calendar_app_message_diary_info_load_failure
    }

    /** 設定情報の読み込みに失敗したことを示すメッセージ。 */
    data object SettingsLoadFailure : CalendarAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_calendar_app_message_calendar_settings_load_failure
    }
}
