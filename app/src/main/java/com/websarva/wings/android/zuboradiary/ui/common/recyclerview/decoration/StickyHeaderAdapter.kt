package com.websarva.wings.android.zuboradiary.ui.common.recyclerview.decoration

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerViewにスティッキーヘッダー効果を実装するために必要な情報を提供するインターフェース。
 *
 * このインターフェースを実装した[RecyclerView.Adapter]は、
 * どのアイテムがヘッダーであるか、またそのヘッダーに対応するViewは何かを[StickyHeaderDecoration]に伝える責務を持つ。
 */
interface StickyHeaderAdapter {

    /**
     * 指定されたポジションのアイテムがヘッダーであるかを判定する。
     * @param itemPosition 判定するアイテムのリスト内での位置。
     * @return ヘッダーである場合は`true`。
     */
    fun isHeader(itemPosition: Int): Boolean

    /**
     * 指定されたポジションのアイテムに対応するヘッダーのViewを取得する。
     * @param itemPosition ヘッダービューを取得したいアイテムのリスト内での位置。
     * @param recyclerView 親となるRecyclerView。
     * @return スティッキーヘッダーとして表示するView。ヘッダーが存在しない場合は`null`。
     */
    fun getHeaderView(itemPosition: Int, recyclerView: RecyclerView): View?
}
