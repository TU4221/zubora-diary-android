package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem

internal abstract class WordSearchResultYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) :
    DiaryYearMonthListBaseAdapter(recyclerView, themeColor, DiffUtilItemCallback()) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder, item: DiaryYearMonthListBaseItem
    ) {
        if (item !is WordSearchResultYearMonthListItem) throw IllegalStateException()

        val listAdapter = createWordSearchResultDayListAdapter(holder)
        listAdapter.submitList(item.diaryDayList.itemList)
    }

    private fun createWordSearchResultDayListAdapter(
        holder: DiaryYearMonthListViewHolder
    ): WordSearchResultDayListAdapter {
        val wordSearchResultDayListAdapter =
            WordSearchResultDayListAdapter(holder.binding.recyclerDayList, themeColor)
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

            if (!oldItem.areContentsTheSame(newItem)) {
                Log.d(logTag, "areContentsTheSame()_不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
