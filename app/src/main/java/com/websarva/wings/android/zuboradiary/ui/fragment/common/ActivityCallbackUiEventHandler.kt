package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent

/**
 * Activityからのコールバックに起因するUIイベントを処理するためのファンクショナルインターフェース。
 *
 * BottomNavigationViewの同じタブが再選択された場合など、Activity層で発生したイベントを
 * Fragmentに通知し、処理させるために使用する。
 */
fun interface ActivityCallbackUiEventHandler {
    /**
     * Activityコールバック起因のUIイベントを受け取った際に呼び出される。
     *
     * @param event 受信したUIイベント。
     */
    fun onActivityCallbackUiEventReceived(event: ActivityCallbackUiEvent)
}
