package com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration

import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerViewのアイテム間に間隔を設けるかどうかを判断するための情報を提供するインターフェース。
 *
 * このインターフェースを実装した[RecyclerView.Adapter]は、
 * 特定のアイテムが間隔描画の対象であるかを[SpacingItemDecoration]に伝える責務を持つ。
 */
interface SpacingItemProvider {

    /**
     * 指定されたポジションのアイテムが、間隔を設けるべき対象であるかを判定する。
     * @param itemPosition 判定するアイテムのリスト内での位置。
     * @return 間隔を設けるべきアイテムの場合は`true`。
     */
    fun isSpacingItem(itemPosition: Int): Boolean
}
