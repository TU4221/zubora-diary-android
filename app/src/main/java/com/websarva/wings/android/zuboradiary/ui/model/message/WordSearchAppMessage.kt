package com.websarva.wings.android.zuboradiary.ui.model.message

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.WordSearchFragment
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * ワード検索画面([WordSearchFragment])で表示される、固有のアプリケーションメッセージを表すsealed class。
 */
@Parcelize
sealed class WordSearchAppMessage : AppMessage {

    /** 検索結果リストの読み込みに失敗したことを示すメッセージ。 */
    data object SearchResultListLoadFailure : WordSearchAppMessage() {
        @IgnoredOnParcel
        override val dialogTitleStringResId: Int = R.string.dialog_app_message_title_access_error
        @IgnoredOnParcel
        override val dialogMessageStringResId: Int = R.string.dialog_word_search_app_message_search_result_list_load_failure
    }
}
