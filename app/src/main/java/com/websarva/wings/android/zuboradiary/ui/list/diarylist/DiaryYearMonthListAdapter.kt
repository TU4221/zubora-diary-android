package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.SwipeDiaryYearMonthListBaseAdapter

abstract class DiaryYearMonthListAdapter(
    context: Context,
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter(context, recyclerView, themeColor, DiffUtilItemCallback()) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder,
        item: DiaryYearMonthListBaseItem
    ) {
        if (item !is DiaryYearMonthListItem) throw IllegalStateException()

        val diaryDayListAdapter = createDiaryDayListAdapter(holder)
        val diaryDayList = item.diaryDayList.diaryDayListItemList
        val convertedList: List<DiaryDayListBaseItem> = diaryDayList
        diaryDayListAdapter.submitList(convertedList)
    }

    private fun createDiaryDayListAdapter(
        holder: DiaryYearMonthListViewHolder
    ): DiaryDayListAdapter {
        val diaryDayListAdapter =
            DiaryDayListAdapter(context, holder.binding.recyclerDayList, themeColor)
        return diaryDayListAdapter.apply {
            build()
            onClickItemListener =
                DiaryDayListBaseAdapter.OnClickItemListener { item: DiaryDayListBaseItem ->
                    onClickChildItemListener?.onClick(item) ?: return@OnClickItemListener
                }
            onClickDeleteButtonListener =
                SwipeDiaryDayListBaseAdapter.OnClickDeleteButtonListener { item: DiaryDayListBaseItem? ->
                    onClickChildItemBackgroundButtonListener?.onClick(item)
                        ?: return@OnClickDeleteButtonListener
                }
        }
    }

    private class DiffUtilItemCallback : DiaryYearMonthListBaseAdapter.DiffUtilItemCallback() {
        override fun areContentsTheSame(
            oldItem: DiaryYearMonthListBaseItem,
            newItem: DiaryYearMonthListBaseItem
        ): Boolean {
            Log.d("DiaryYearMonthList", "DiffUtil.ItemCallback_areContentsTheSame()")
            Log.d("DiaryYearMonthList", "oldItem_YearMonth:" + oldItem.yearMonth)
            Log.d("DiaryYearMonthList", "newItem_YearMonth:" + newItem.yearMonth)

            if (oldItem !is DiaryYearMonthListItem) throw IllegalStateException()
            if (newItem !is DiaryYearMonthListItem) throw IllegalStateException()

            // 日
            Log.d("DiaryYearMonthList", "DiaryYearMonthListItem")

            val oldChildListSize = oldItem.diaryDayList.diaryDayListItemList.size
            val newChildListSize = newItem.diaryDayList.diaryDayListItemList.size
            if (oldChildListSize != newChildListSize) {
                Log.d("DiaryYearMonthList", "ChildList_Size不一致")
                return false
            }

            for (i in 0 until oldChildListSize) {
                val oldChildListItem = oldItem.diaryDayList.diaryDayListItemList[i]
                val newChildListItem = newItem.diaryDayList.diaryDayListItemList[i]
                if (oldChildListItem.date != newChildListItem.date) {
                    Log.d("DiaryYearMonthList", "ChildListItem_Date不一致")
                    return false
                }
                if (oldChildListItem.title != newChildListItem.title) {
                    Log.d("DiaryYearMonthList", "ChildListItem_Title不一致")
                    return false
                }
                if (oldChildListItem.picturePath == null && newChildListItem.picturePath != null) {
                    Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致")
                    return false
                }
                if (oldChildListItem.picturePath != null && newChildListItem.picturePath == null) {
                    Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致")
                    return false
                }
                if ((oldChildListItem.picturePath != null/* && newChildListItem.picturePath != null*/)
                    && (oldChildListItem.picturePath != newChildListItem.picturePath)
                ) {
                    Log.d("DiaryYearMonthList", "ChildListItem_PicturePath不一致")
                    return false
                }
            }
            Log.d("DiaryYearMonthList", "一致")
            return true
        }
    }
}
