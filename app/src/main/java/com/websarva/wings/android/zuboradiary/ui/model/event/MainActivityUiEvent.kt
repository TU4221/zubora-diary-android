package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.model.message.CommonAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.message.MainActivityAppMessage

/**
 * [MainActivity]における、UIイベントを表すsealed class。
 */
sealed class MainActivityUiEvent : UiEvent {

    /**
     * MainActivity固有のアプリケーションメッセージを表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[MainActivityAppMessage]。
     */
    data class NavigateMainActivityAppMessage(val message: MainActivityAppMessage) : MainActivityUiEvent()

    /**
     * アプリケーション共通のメッセージを表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[CommonAppMessage]。
     */
    data class NavigateCommonAppMessage(val message: CommonAppMessage) : MainActivityUiEvent()

    /** BottomNavigationViewの開始タブのFragmentに遷移することを示すイベント。 */
    data object NavigateStartTabFragment : MainActivityUiEvent()
}
