package com.websarva.wings.android.zuboradiary.ui.common.fragment

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent

/**
 * 各画面固有のUIイベントを処理するためのファンクショナルインターフェース。
 *
 * ViewModelから通知されたUIイベントを、対応するFragmentで処理する際のコールバックとして使用する。
 *
 * @param E 処理するUiEventの型。
 */
fun interface MainUiEventHandler<in E : UiEvent> {
    /**
     * 画面固有のUIイベントを受け取った際に呼び出される。
     *
     * @param event 受信したUIイベント。
     */
    fun onMainUiEventReceived(event: E)
}
