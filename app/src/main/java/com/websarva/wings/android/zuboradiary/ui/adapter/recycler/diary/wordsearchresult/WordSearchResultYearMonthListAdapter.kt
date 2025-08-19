package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult.WordSearchResultDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult.WordSearchResultYearMonthListItem

internal abstract class WordSearchResultYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : DiaryYearMonthListBaseAdapter<WordSearchResultYearMonthListItem, WordSearchResultDayListItem>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item, item: WordSearchResultYearMonthListItem
    ) {
        val listAdapter = createWordSearchResultDayListAdapter(holder)
        listAdapter.submitList(item.diaryDayList.itemList)
    }

    private fun createWordSearchResultDayListAdapter(
        holder: DiaryYearMonthListViewHolder.Item
    ): WordSearchResultDayListAdapter {
        val wordSearchResultDayListAdapter =
            WordSearchResultDayListAdapter(holder.binding.recyclerDayList, themeColor)
        return wordSearchResultDayListAdapter.apply {
            build()
            registerOnClickItemListener {
                onClickChildItemListener?.onClick(it)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<WordSearchResultYearMonthListItem>() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: WordSearchResultYearMonthListItem,
            newItem: WordSearchResultYearMonthListItem
        ): Boolean {
            Log.d(logTag, "areContentsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth)
            Log.d(logTag, "areContentsTheSame()_newItem.yearMonth = " + newItem.yearMonth)

            if (!oldItem.areContentsTheSame(newItem)) {
                Log.d(logTag, "areContentsTheSame()_不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
