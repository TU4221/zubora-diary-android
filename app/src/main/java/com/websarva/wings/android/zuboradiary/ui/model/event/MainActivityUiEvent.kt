package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage

/**
 * [MainActivity]における、UIイベントを表すsealed class。
 */
sealed class MainActivityUiEvent : UiEvent {

    /** BottomNavigationViewの開始タブの画面に遷移することを示すイベント。 */
    data object NavigateStartTabScreen : MainActivityUiEvent()

    /**
     * MainActivity固有のアプリケーションメッセージダイアログを表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[MainActivityAppMessage]。
     */
    data class ShowMainActivityAppMessageDialog(val message: MainActivityAppMessage) : MainActivityUiEvent()

    /**
     * アプリケーション共通のメッセージダイアログを表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[CommonAppMessage]。
     */
    data class ShowCommonAppMessageDialog(val message: CommonAppMessage) : MainActivityUiEvent()
}
