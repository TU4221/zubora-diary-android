package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryYearMonthListBaseAdapter

internal abstract class DiaryYearMonthListAdapter(
    context: Context,
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter(context, recyclerView, themeColor,
    DiffUtilItemCallback()
) {

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
                SwipeDiaryDayListBaseAdapter.OnClickDeleteButtonListener { item: DiaryDayListBaseItem ->
                    onClickChildItemBackgroundButtonListener?.onClick(item)
                        ?: return@OnClickDeleteButtonListener
                }
        }
    }

    private class DiffUtilItemCallback : DiaryYearMonthListBaseAdapter.DiffUtilItemCallback() {

        private val logTag = createLogTag()

            override fun areContentsTheSame(
                oldItem: DiaryYearMonthListBaseItem,
                newItem: DiaryYearMonthListBaseItem
            ): Boolean {
                Log.d(
                    logTag,
                    "areContentsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth
                )
                Log.d(
                    logTag,
                    "areContentsTheSame()_newItem.yearMonth = " + newItem.yearMonth
                )

                if (oldItem !is DiaryYearMonthListItem) throw IllegalStateException()
                if (newItem !is DiaryYearMonthListItem) throw IllegalStateException()

                // 日
                val oldChildListSize = oldItem.diaryDayList.diaryDayListItemList.size
                val newChildListSize = newItem.diaryDayList.diaryDayListItemList.size
                if (oldChildListSize != newChildListSize) {
                    Log.d(logTag, "areContentsTheSame()_ChildList_Size不一致")
                    return false
                }

                for (i in 0 until oldChildListSize) {
                    val oldChildListItem = oldItem.diaryDayList.diaryDayListItemList[i]
                    val newChildListItem = newItem.diaryDayList.diaryDayListItemList[i]
                    if (oldChildListItem.date != newChildListItem.date) {
                        Log.d(logTag, "areContentsTheSame()_ChildListItem_Date不一致")
                        return false
                    }
                    if (oldChildListItem.title != newChildListItem.title) {
                        Log.d(logTag, "areContentsTheSame()ChildListItem_Title不一致")
                        return false
                    }
                    if (oldChildListItem.picturePath == null && newChildListItem.picturePath != null) {
                        Log.d(logTag, "areContentsTheSame()ChildListItem_PicturePath不一致")
                        return false
                    }
                    if (oldChildListItem.picturePath != null && newChildListItem.picturePath == null) {
                        Log.d(logTag, "areContentsTheSame()ChildListItem_PicturePath不一致")
                        return false
                    }
                    if ((oldChildListItem.picturePath != null/* && newChildListItem.picturePath != null*/)
                        && (oldChildListItem.picturePath != newChildListItem.picturePath)
                    ) {
                        Log.d(logTag, "areContentsTheSame()ChildListItem_PicturePath不一致")
                        return false
                    }
                }
                Log.d(logTag, "areContentsTheSame()_全項目一致")
                return true
            }
    }
}
