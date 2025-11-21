package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent

/**
 * アプリケーション全体で共通のUIイベントを処理するためのインターフェース。
 *
 * 画面遷移や共通のメッセージ表示など、複数の画面で共有されるUIイベントの処理を定義する。
 */
interface CommonUiEventHandler {
    /**
     * 共通のUIイベントを受け取った際に呼び出される。
     *
     * @param event 受信したUIイベント。
     */
    fun onCommonUiEventReceived(event: CommonUiEvent)
}
