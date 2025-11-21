package com.websarva.wings.android.zuboradiary.ui.model.event

import java.util.concurrent.atomic.AtomicBoolean

/**
 * 一度だけ消費されることを意図したイベントをラップするクラス。
 *
 * 画面回転などで同じイベントが複数回処理されることを防ぐ。
 *
 * @param T イベントの内容を表す型。
 * @property content イベントの実際のコンテンツ。
 */
class ConsumableEvent<T>(private val content: T) {

    /** このイベントが既に処理されたかどうかを示すフラグ。 */
    private val hasBeenHandled = AtomicBoolean(false)

    /**
     * イベントが未処理の場合にのみコンテンツを返す。
     * @return 未処理の場合はコンテンツ、処理済みの場合は`null`。
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled.getAndSet(true)) {
            null
        } else {
            content
        }
    }
}
