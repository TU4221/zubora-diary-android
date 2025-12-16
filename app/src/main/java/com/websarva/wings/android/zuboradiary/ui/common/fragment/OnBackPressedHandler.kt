package com.websarva.wings.android.zuboradiary.ui.common.fragment

/**
 * バックプレスイベントをハンドリングする責務を定義するインターフェース。
 *
 *  FragmentからViewModelへ、戻る操作の処理を委譲する。
 */
fun interface OnBackPressedHandler {

    /**
     * バックプレスイベントが発生した際に呼び出される事を想定。
     */
    fun onBackPressed()
}
