package com.websarva.wings.android.zuboradiary.data.common

/**
 * アプリケーションがフォアグラウンドで実行されているかどうかの状態を提供するインターフェースである。
 *
 * この状態情報は、例えばバックグラウンドでのみ実行すべき処理や、
 * フォアグラウンド状態に応じてUIの挙動を変化させたい場合などに利用される。
 */
internal interface AppForegroundStateProvider {

    /**
     * アプリケーションが現在フォアグラウンドで実行されているかどうかを示す。
     *
     * `true` の場合、アプリケーションはフォアグラウンド状態にある。
     * `false` の場合、アプリケーションはバックグラウンド状態にある、または実行されていない。
     *
     * この値は動的に変化する可能性があるため、利用側は必要に応じて最新の状態を取得する必要がある。
     */
    val isAppInForeground: Boolean
}
