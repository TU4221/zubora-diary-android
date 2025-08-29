package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * [itemView] がウィンドウにアタッチされた時に、WindowInsetsの適用を要求する [RecyclerView.ViewHolder]。
 *
 * RecyclerViewのアイテムビューが最初に表示される際、インセット関連のリスナー
 * ([View.OnApplyWindowInsetsListener] など) が確実に呼び出されるようにします。(Edge-to-Edge対応)
 *
 * [itemView]がアタッチされると [ViewCompat.requestApplyInsets] を呼び出し、
 * 現在のインセットでリスナーがトリガーされることを促します。
 *
 * 使用方法: このViewHolderを継承して使用します。
 *
 * @param itemView RecyclerView内のアイテムビュー。
 * @see ViewCompat.requestApplyInsets
 * @see View.OnApplyWindowInsetsListener
 */
internal abstract class WindowInsetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            /**
             * ItemViewがウィンドウにアタッチされた時、インセットの適用を要求。
             * これにより、初期表示時にインセットが正しく反映されることを保証。
             */
            override fun onViewAttachedToWindow(v: View) {
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) {
                // 処理不要
            }
        })
    }

}
