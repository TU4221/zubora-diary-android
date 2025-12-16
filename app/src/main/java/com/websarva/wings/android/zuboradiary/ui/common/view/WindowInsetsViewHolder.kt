package com.websarva.wings.android.zuboradiary.ui.common.view

import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.common.binding.BindingAdapters

/**
 * `RecyclerView`のアイテムビューに適用された[BindingAdapters.applySystemInsetsToMargin]が、
 * ビューの再利用時にインセットを再適用するようにトリガーをかけるための抽象`ViewHolder`。
 *
 * `RecyclerView`のアイテムビューは再利用されるため、
 * [BindingAdapters.applySystemInsetsToMargin]で適用された[OnApplyWindowInsetsListener]は
 * 一度しか呼び出されないことがある。その結果、ビューが再アタッチされた際にインセットが正しく適用されない問題が発生する。
 *
 * この`ViewHolder`を継承することで、アイテムビューがウィンドウにアタッチされるたびに
 * [ViewCompat.requestApplyInsets]が呼び出され、
 * [OnApplyWindowInsetsListener]がインセットを再計算・再適用する機会が与えられる。
 * これにより、Edge-to-Edge対応のレイアウトでスクロールやデータ更新時にインセットが失われる問題を防ぐ。
 *
 * @param itemView ViewHolderが保持するアイテムのビュー。
 */
internal abstract class WindowInsetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            /**
             * ビューがウィンドウにアタッチされたときに呼び出される。
             * インセットの再適用を要求し、`BindingAdapter`がインセットを再評価するようトリガーする。
             */
            override fun onViewAttachedToWindow(v: View) {
                ViewCompat.requestApplyInsets(v)
            }

            /**
             * ビューがウィンドウからデタッチされたときに呼び出される。
             * この実装では特に処理は不要。
             */
            override fun onViewDetachedFromWindow(v: View) {
                // 処理不要
            }
        })
    }

}
