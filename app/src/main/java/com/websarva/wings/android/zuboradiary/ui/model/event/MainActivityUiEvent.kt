package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.activity.MainActivity
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage

/**
 * [MainActivity]における、UIイベントを表すsealed class。
 */
sealed class MainActivityUiEvent : UiEvent {

    /**
     * アプリケーションメッセージを表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[AppMessage]。
     */
    data class NavigateAppMessage(val message: AppMessage) : MainActivityUiEvent()

    /** BottomNavigationViewの開始タブのFragmentに遷移することを示すイベント。 */
    data object NavigateStartTabFragment : MainActivityUiEvent()
}
