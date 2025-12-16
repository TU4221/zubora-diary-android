package com.websarva.wings.android.zuboradiary.ui.diary.common.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.decoration.SpacingItemDecoration
import com.websarva.wings.android.zuboradiary.ui.common.recyclerview.decoration.StickyHeaderDecoration
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemUi
import java.lang.IllegalArgumentException

/**
 * 日記リスト用のRecyclerViewのセットアップするヘルパークラス。
 *
 * 以下の責務を持つ:
 * - `RecyclerView`に`LinearLayoutManager`と`DiaryListBaseAdapter`を設定する。
 * - `SpacingItemDecoration`と`StickyHeaderDecoration`を`RecyclerView`に追加する。
 * - リストの末尾までスクロールされた際に新しいデータを読み込むためのページネーション機能（無限スクロール）をセットアップする。
 * - [cleanup]で、登録したリスナーをクリーンアップする。
 *
 * @property recyclerView セットアップ対象のRecyclerView。
 * @property adapter `RecyclerView`に設定するアダプター。
 * @property onDiaryListEndScrolled リストの末尾までスクロールされた際に呼び出されるコールバック。
 */
internal class DiaryListSetupHelper(
    private val recyclerView: RecyclerView,
    private val adapter: DiaryListBaseAdapter<*, *>,
    private val onDiaryListEndScrolled: () -> Unit
) {

    /** ページネーション（無限スクロール）を実現するためのスクロールリスナー。 */
    private var paginationScrollListener: PaginationScrollListener? = null

    /**
     * 日記リストに関するすべてコンポーネント（アダプター、レイアウトマネージャー、各種Decoration、スクロールリスナー）
     * をセットアップする。
     * */
    fun setup() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(SpacingItemDecoration(recyclerView.context))
        recyclerView.addItemDecoration(StickyHeaderDecoration(adapter))

        paginationScrollListener = PaginationScrollListener(onDiaryListEndScrolled).also {
            recyclerView.addOnScrollListener(it)
        }
    }

    /**
     *  セットアップされたすべてのコンポーネントとリスナーをクリーンアップする。
     *  Viewの破棄時に呼び出されることを想定。
     */
    fun cleanup() {
        paginationScrollListener?.let { recyclerView.removeOnScrollListener(it) }
        paginationScrollListener = null
    }

    /**
     * RecyclerViewのスクロールを監視し、リストの末尾に近づいた際にコールバックをトリガーするリスナクラス。
     *
     * @property onEndScrolled リストの末尾までスクロールされた際に呼び出されるコールバック。
     */
    private class PaginationScrollListener(
        private val onEndScrolled: () -> Unit
    ) : RecyclerView.OnScrollListener() {

        /** 追加読み込みを開始する、リストの末尾から数えたアイテム数の閾値。 */
        private val threshold = 5

        /**
         * RecyclerViewがスクロールされるたびに呼び出される。
         * リストの末尾から指定された閾値（[threshold]）以内にユーザーがスクロールした場合に、
         * [onEndScrolled]コールバックを呼び出す。
         */
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0) return
            val layoutManager =
                recyclerView.layoutManager as? LinearLayoutManager
                    ?: throw IllegalArgumentException()
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val adapter =
                recyclerView.adapter as? DiaryListBaseAdapter<*, *> ?: throw IllegalArgumentException()
            val lastItem = adapter.currentList.getOrNull(totalItemCount - 1)

            // 最後のアイテムが「日記なしメッセージ」の場合は、追加読み込みをしない
            if (lastItem is DiaryListItemUi.NoDiaryMessage) return

            // 「リストの末尾から閾値までスクロールされたら」次のデータを読み込む
            if (totalItemCount <= lastVisibleItemPosition + threshold) {
                onEndScrolled()
            }
        }
    }
}
