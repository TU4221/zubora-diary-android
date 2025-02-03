package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem

abstract class WordSearchResultYearMonthListAdapter(
    context: Context,
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) :
    DiaryYearMonthListBaseAdapter(context, recyclerView, themeColor, DiffUtilItemCallback()) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder, item: DiaryYearMonthListBaseItem
    ) {
        if (item !is WordSearchResultYearMonthListItem) throw IllegalStateException()

        val listAdapter = createWordSearchResultDayListAdapter(holder)
        listAdapter.submitList(item.wordSearchResultDayList.wordSearchResultDayListItemList)
    }

    private fun createWordSearchResultDayListAdapter(
        holder: DiaryYearMonthListViewHolder
    ): WordSearchResultDayListAdapter {
        val wordSearchResultDayListAdapter =
            WordSearchResultDayListAdapter(context, holder.binding.recyclerDayList, themeColor)
        return wordSearchResultDayListAdapter.apply {
            build()
            onClickItemListener =
                DiaryDayListBaseAdapter.OnClickItemListener { item: DiaryDayListBaseItem ->
                    if (onClickChildItemListener == null) return@OnClickItemListener
                    onClickChildItemListener!!.onClick(item)
                }
        }
    }

    private class DiffUtilItemCallback : DiaryYearMonthListBaseAdapter.DiffUtilItemCallback() {
        override fun areContentsTheSame(
            oldItem: DiaryYearMonthListBaseItem,
            newItem: DiaryYearMonthListBaseItem
        ): Boolean {
            Log.d("WordSearchYearMonthList", "DiffUtil.ItemCallback_areContentsTheSame()")
            Log.d("WordSearchYearMonthList", "oldItem_YearMonth:" + oldItem.yearMonth)
            Log.d("WordSearchYearMonthList", "newItem_YearMonth:" + newItem.yearMonth)

            if (oldItem !is WordSearchResultYearMonthListItem) throw IllegalStateException()
            if (newItem !is WordSearchResultYearMonthListItem) throw IllegalStateException()

            // 日
            Log.d("WordSearchYearMonthList", "WordSearchResultYearMonthListItem")
            val oldChildListSize =
                oldItem.wordSearchResultDayList.wordSearchResultDayListItemList.size
            val newChildListSize =
                newItem.wordSearchResultDayList.wordSearchResultDayListItemList.size
            if (oldChildListSize != newChildListSize) {
                Log.d("WordSearchYearMonthList", "ChildList_Size不一致")
                return false
            }

            for (i in 0 until oldChildListSize) {
                val oldChildListItem =
                    oldItem.wordSearchResultDayList.wordSearchResultDayListItemList[i]
                val newChildListItem =
                    newItem.wordSearchResultDayList.wordSearchResultDayListItemList[i]
                Log.d(
                    "WordSearchYearMonthList",
                    "oldChildListItem_Date:" + oldChildListItem.date
                )
                Log.d(
                    "WordSearchYearMonthList",
                    "newChildListItem_Date:" + newChildListItem.date
                )

                if (oldChildListItem.date != newChildListItem.date) {
                    Log.d("WordSearchYearMonthList", "ChildListItem_Date不一致")
                    return false
                }
                if (oldChildListItem.title != newChildListItem.title) {
                    Log.d("WordSearchYearMonthList", "ChildListItem_Title不一致")
                    return false
                }
                if (oldChildListItem.itemNumber !== newChildListItem.itemNumber) {
                    Log.d("WordSearchYearMonthList", "ChildListItem_ItemNumber不一致")
                    return false
                }
                if (oldChildListItem.itemTitle != newChildListItem.itemTitle) {
                    Log.d("WordSearchYearMonthList", "ChildListItem_ItemTitle不一致")
                    return false
                }
                if (oldChildListItem.itemComment != newChildListItem.itemComment) {
                    Log.d("WordSearchYearMonthList", "ChildListItem_ItemComment不一致")
                    return false
                }
            }
            Log.d("WordSearchYearMonthList", "一致")
            return true
        }
    }
}
