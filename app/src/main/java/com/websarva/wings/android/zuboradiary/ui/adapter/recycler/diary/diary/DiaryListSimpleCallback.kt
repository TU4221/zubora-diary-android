package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.LeftSwipeBackgroundButtonSimpleCallback
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal class DiaryListSimpleCallback(
    private val parentRecyclerView: RecyclerView,
    recyclerView: SwipeRecyclerView
) : LeftSwipeBackgroundButtonSimpleCallback(recyclerView) {

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        // 他ChildRecyclerView(DayList)のスワイプ状態を閉じる
        val adapter = parentRecyclerView.adapter
        val listAdapter = adapter as DiaryYearMonthListAdapter
        listAdapter.closeSwipedItemOtherDayList(this)
    }
}
