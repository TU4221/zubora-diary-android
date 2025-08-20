package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult.WordSearchResultDayList

internal abstract class WordSearchResultYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : DiaryYearMonthListBaseAdapter<WordSearchResultDayList, DiaryDayListItem.WordSearchResult>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItem.Diary<WordSearchResultDayList>
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
            registerOnItemClickListener {
                onChildItemClickListener?.onClick(it)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<WordSearchResultDayList>() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryYearMonthListItem<WordSearchResultDayList>,
            newItem: DiaryYearMonthListItem<WordSearchResultDayList>
        ): Boolean {
            val result = when (oldItem) {
                is DiaryYearMonthListItem.Diary -> {
                    if (newItem !is DiaryYearMonthListItem.Diary<WordSearchResultDayList>) {
                        false
                    } else {
                        oldItem.diaryDayList == newItem.diaryDayList
                    }
                }

                is DiaryYearMonthListItem.NoDiaryMessage,
                is DiaryYearMonthListItem.ProgressIndicator -> {
                    return false
                }
            }

            Log.d(
                logTag,
                "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }
    }
}
