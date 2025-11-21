package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper

/**
 * RecyclerViewのViewHolderがスワイプ可能であることを示すインターフェース。
 *
 * このインターフェースを実装することで、[ItemTouchHelper.Callback]の実装クラスは、
 * どのビューをスワイプ対象とするかを判断できる。
 */
interface SwipeableViewHolder {

    /** スワイプやアニメーションの対象となる、アイテムの前面に表示されるビュー。 */
    val foregroundView: View

    /**
     * スワイプ操作がキャンセルまたは完了し、フォアグラウンドビューが
     * 元の位置に戻るアニメーション（ロールバック）中であるかを示すフラグ。
     */
    var isRollingBack: Boolean
}
