package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.LeftSwipeBackgroundButtonSimpleCallback

internal class DiaryListSimpleCallback(
    private val parentRecyclerView: RecyclerView,
    recyclerView: RecyclerView
) : LeftSwipeBackgroundButtonSimpleCallback(recyclerView) {

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        // 他ChildRecyclerView(DayList)のスワイプ状態を閉じる
        val adapter = parentRecyclerView.adapter
        val listAdapter = adapter as DiaryYearMonthListAdapter
        listAdapter.closeSwipedItemOtherDayList(this)
    }
}
