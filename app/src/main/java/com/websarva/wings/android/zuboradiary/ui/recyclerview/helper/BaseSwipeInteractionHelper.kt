package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.BaseLeftSwipeCallback


internal abstract class BaseSwipeInteractionHelper<
        RV : RecyclerView,
        ADAPTER : ListBaseAdapter<*, *>,
        CALLBACK: BaseLeftSwipeCallback
>(
    protected val recyclerView: RV,
    protected val listAdapter: ADAPTER
) {

    protected open var simpleCallback: CALLBACK? = null
    protected var itemTouchHelper: ItemTouchHelper? = null
    private var swipeCloseScrollListener: RecyclerView.OnScrollListener? = null
    private var adapterDataObserver: RecyclerViewAdapterDataObserver? = null

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

    protected abstract fun createLeftSwipeSimpleCallback() : CALLBACK

    protected open fun onSetup() {
        // サブクラスがこのメソッドをオーバーライドして、
        // 追加設定処理を実装する。
        // デフォルトでは何もしない。
    }

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

    protected open fun onCleanup() {
        // サブクラスがこのメソッドをオーバーライドして、
        // 追加設定のクリーンアップ処理を実装する。
        // デフォルトでは何もしない。
    }

    fun updateItemSwipeEnabled(enabled: Boolean) {
        simpleCallback?.updateItemSwipeEnabledState(enabled)
    }

    fun closeSwipedItem() {
        simpleCallback?.closeSwipedItem()
    }

    // 元のFragmentにあった内部クラスをそのまま移動
    private class RecyclerViewAdapterDataObserver(
        private val onDataChanged: () -> Unit
    ) : RecyclerView.AdapterDataObserver() {
        // 元のコードにはonChanged()がなかったので、ここでも定義しない
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
