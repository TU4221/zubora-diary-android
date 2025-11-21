package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.BaseLeftSwipeCallback

/**
 * RecyclerViewにスワイプ操作のインタラクションをセットアップするためのヘルパー抽象基底クラス。
 *
 * 以下の責務を持つ:
 * - [ItemTouchHelper]と[BaseLeftSwipeCallback]を連携させ、スワイプ機能をセットアップする。
 * - RecyclerViewのスクロール時に、開かれているスワイプアイテムを自動的に閉じるリスナーを登録する。
 * - アダプターのデータ変更を監視し、スワイプ状態をリセットするためのオブザーバーを登録する。
 * - ビューの破棄時に、登録した全てのリスナーとヘルパーをクリーンアップする。
 * - 具体的な[BaseLeftSwipeCallback]の実装は、サブクラスに委譲する。
 *
 * @param RV 対象となるRecyclerViewの型。
 * @param ADAPTER 対象となるアダプターの型。 [ListBaseAdapter]を継承する必要がある。
 * @param CALLBACK 使用するItemTouchHelper.Callbackの型。 [BaseLeftSwipeCallback]を継承する必要がある。
 * @property recyclerView インタラクションを適用するRecyclerView。
 * @property listAdapter 対象となるRecyclerViewのアダプター。
 */
internal abstract class BaseSwipeInteractionHelper<
        RV : RecyclerView,
        ADAPTER : ListBaseAdapter<*, *>,
        CALLBACK: BaseLeftSwipeCallback
>(
    protected val recyclerView: RV,
    protected val listAdapter: ADAPTER
) {

    /** スワイプコールバックの実装。 */
    protected open var simpleCallback: CALLBACK? = null

    /** スワイプやドラッグなどのタッチイベントを処理するヘルパー。 */
    protected var itemTouchHelper: ItemTouchHelper? = null

    /** スクロール時にスワイプされたアイテムを閉じるためのリスナー。 */
    private var swipeCloseScrollListener: RecyclerView.OnScrollListener? = null

    /** アダプターのデータ変更を監視し、スワイプ状態をリセットするためのオブザーバー。 */
    private var adapterDataObserver: RecyclerViewAdapterDataObserver? = null

    /**
     * スワイプインタラクションに関するすべてコンポーネントをセットアップする。
     */
    fun setup() {
        // ItemTouchHelperのセットアップ
        simpleCallback = createLeftSwipeSimpleCallback()
        itemTouchHelper = simpleCallback?.let { ItemTouchHelper(it) }
        itemTouchHelper?.attachToRecyclerView(recyclerView)

        // AdapterDataObserverのセットアップ
        adapterDataObserver = RecyclerViewAdapterDataObserver {
            simpleCallback?.resetSwipeStatePositions()
        }.also {
            listAdapter.registerAdapterDataObserver(it)
        }

        // スクロール時にスワイプを閉じるためのリスナー
        swipeCloseScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) return

                // スクロール時スワイプ閉
                simpleCallback?.closeSwipedItem()
            }
        }.also {
            recyclerView.addOnScrollListener(it)
        }

        onSetup()
    }

    /**
     * このヘルパーで使用する[BaseLeftSwipeCallback]の具象インスタンスを生成する。[setup]から呼び出される。
     * @return [CALLBACK]のインスタンス。
     */
    protected abstract fun createLeftSwipeSimpleCallback() : CALLBACK

    /** サブクラスで追加のセットアップ処理が必要な場合にオーバーライドして使用する。[setup]から呼び出される。 */
    protected open fun onSetup() {
        // デフォルトでは何もしない。
    }

    /**
     * セットアップされたすべてのコンポーネントとリスナーをクリーンアップする。
     * Viewの破棄時に呼び出すことを想定。
     */
    fun cleanup() {
        itemTouchHelper?.attachToRecyclerView(null)
        swipeCloseScrollListener?.let { recyclerView.removeOnScrollListener(it) }
        adapterDataObserver?.let { listAdapter.unregisterAdapterDataObserver(it) }

        simpleCallback = null
        itemTouchHelper = null
        swipeCloseScrollListener = null
        adapterDataObserver = null

        onCleanup()
    }

    /**
     * サブクラスで追加のクリーンアップ処理が必要な場合にオーバーライドして使用する。[cleanup]から呼び出される。
     */
    protected open fun onCleanup() {
        // デフォルトでは何もしない。
    }

    /**
     * RecyclerViewのアイテムスワイプ機能の有効/無効を切り替える。
     * @param enabled 有効にする場合は`true`。
     */
    fun updateItemSwipeEnabled(enabled: Boolean) {
        simpleCallback?.updateItemSwipeEnabledState(enabled)
    }

    /** 現在開かれている（スワイプされている）アイテムを閉じるアニメーションを開始する。 */
    fun closeSwipedItem() {
        simpleCallback?.closeSwipedItem()
    }

    /**
     * RecyclerViewのアダプターのデータ変更イベントを監視するための[RecyclerView.AdapterDataObserver]。
     * データが変更された際に、指定されたコールバックを呼び出す。
     *
     * @property onDataChanged データ変更イベントが発生した際に呼び出されるコールバック関数。
     */
    private class RecyclerViewAdapterDataObserver(
        private val onDataChanged: () -> Unit
    ) : RecyclerView.AdapterDataObserver() {

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onDataChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onDataChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onDataChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onDataChanged()
        }
    }
}
