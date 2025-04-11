package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem

internal abstract class WordSearchResultYearMonthListAdapter(
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
                    onClickChildItemListener?.onClick(item) ?: return@OnClickItemListener
                }
        }
    }

    private class DiffUtilItemCallback : DiaryYearMonthListBaseAdapter.DiffUtilItemCallback() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryYearMonthListBaseItem,
            newItem: DiaryYearMonthListBaseItem
        ): Boolean {
            Log.d(logTag, "areContentsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth)
            Log.d(logTag, "areContentsTheSame()_newItem.yearMonth = " + newItem.yearMonth)

            if (oldItem !is WordSearchResultYearMonthListItem) throw IllegalStateException()
            if (newItem !is WordSearchResultYearMonthListItem) throw IllegalStateException()

            // 日
            val oldChildListSize =
                oldItem.wordSearchResultDayList.wordSearchResultDayListItemList.size
            val newChildListSize =
                newItem.wordSearchResultDayList.wordSearchResultDayListItemList.size
            if (oldChildListSize != newChildListSize) {
                Log.d(logTag, "areContentsTheSame()_ChildList_Size不一致")
                return false
            }

            for (i in 0 until oldChildListSize) {
                val oldChildListItem =
                    oldItem.wordSearchResultDayList.wordSearchResultDayListItemList[i]
                val newChildListItem =
                    newItem.wordSearchResultDayList.wordSearchResultDayListItemList[i]
                Log.d(
                    logTag,
                    "areContentsTheSame()_oldChildListItem_Date:" + oldChildListItem.date
                )
                Log.d(
                    logTag,
                    "areContentsTheSame()_newChildListItem_Date:" + newChildListItem.date
                )

                if (oldChildListItem.date != newChildListItem.date) {
                    Log.d(logTag, "areContentsTheSame()_ChildListItem_Date不一致")
                    return false
                }
                if (oldChildListItem.title != newChildListItem.title) {
                    Log.d(logTag, "areContentsTheSame()_ChildListItem_Title不一致")
                    return false
                }
                if (oldChildListItem.itemNumber !== newChildListItem.itemNumber) {
                    Log.d(logTag, "areContentsTheSame()_ChildListItem_ItemNumber不一致")
                    return false
                }
                if (oldChildListItem.itemTitle != newChildListItem.itemTitle) {
                    Log.d(logTag, "areContentsTheSame()_ChildListItem_ItemTitle不一致")
                    return false
                }
                if (oldChildListItem.itemComment != newChildListItem.itemComment) {
                    Log.d(logTag, "areContentsTheSame()_ChildListItem_ItemComment不一致")
                    return false
                }
            }
            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
