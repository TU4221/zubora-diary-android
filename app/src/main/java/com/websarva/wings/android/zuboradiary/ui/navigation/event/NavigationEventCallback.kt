package com.websarva.wings.android.zuboradiary.ui.navigation.event

/**
 * 画面遷移イベントの実行結果通知を受け取るためのインターフェース。
 *
 * 遷移処理の成功または失敗（中断）時のコールバックを提供する。
 */
interface NavigationEventCallback<NE : NavigationEvent<*, *>> {

    /**
     * 遷移イベントの処理が成功した際に呼び出される事を想定。
     *
     * @param event 処理が完了した遷移イベント。
     */
    fun onNavigationEventSuccess(event: NE)

    /**
     * 遷移イベントの処理が失敗、または中断された際に呼び出される事を想定。
     *
     * ライフサイクル不適合や遷移条件を満たさなかった場合などに実行される。
     *
     * @param event 処理に失敗した遷移イベント。
     */
    fun onNavigationEventFailure(event: NE)
}
