package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration.SpacingItemDecoration
import com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration.StickyHeaderDecoration
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemUi

internal class DiaryListSetupHelper(
    private val recyclerView: RecyclerView,
    private val adapter: DiaryListBaseAdapter<*, *>,
    private val onDiaryListEndScrolled: () -> Unit
) {
    private var paginationScrollListener: PaginationScrollListener? = null

    fun setup() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(SpacingItemDecoration(recyclerView.context))
        recyclerView.addItemDecoration(StickyHeaderDecoration(adapter))

        paginationScrollListener = PaginationScrollListener(onDiaryListEndScrolled).also {
            recyclerView.addOnScrollListener(it)
        }
    }

    fun cleanup() {
        paginationScrollListener?.let { recyclerView.removeOnScrollListener(it) }
        paginationScrollListener = null
    }

    private class PaginationScrollListener(
        private val onDiaryListEndScrolled: () -> Unit
    ) : RecyclerView.OnScrollListener() {
        private val threshold = 5 // 追加読み込みを開始する手前のアイテム数

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0) return
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val adapter = recyclerView.adapter as? DiaryListBaseAdapter<*, *> ?: return
            val lastItem = adapter.currentList.getOrNull(totalItemCount - 1)

            // 最後のアイテムが「日記なしメッセージ」の場合は、追加読み込みをしない
            if (lastItem is DiaryListItemUi.NoDiaryMessage) return

            // 「リストの末尾から閾値までスクロールされたら」次のデータを読み込む
            if (totalItemCount <= lastVisibleItemPosition + threshold) {
                onDiaryListEndScrolled()
            }
        }
    }
}
