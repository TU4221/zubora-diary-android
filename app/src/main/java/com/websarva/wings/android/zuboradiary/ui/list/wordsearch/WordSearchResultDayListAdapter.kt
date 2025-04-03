package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem
import java.text.NumberFormat

internal class WordSearchResultDayListAdapter(
    context: Context,
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : DiaryDayListBaseAdapter(context, recyclerView, themeColor, DiffUtilItemCallback()) {

    override fun onCreateDiaryDayViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): RecyclerView.ViewHolder {
        val binding =
            RowWordSearchResultListBinding.inflate(themeColorInflater, parent, false)
        return WordSearchResultDayViewHolder(binding)
    }

    override fun onBindDate(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        if (holder !is WordSearchResultDayViewHolder) throw IllegalStateException()

        val date = item.date
        val dayOfWeekStringConverter = DayOfWeekStringConverter(context)
        val dayOfWeekString = dayOfWeekStringConverter.toDiaryListDayOfWeek(date.dayOfWeek)
        holder.binding.includeDay.textDayOfWeek.text = dayOfWeekString
        holder.binding.includeDay.textDayOfMonth.text =
            NumberFormat.getInstance().format(date.dayOfMonth)
    }

    override fun onBindItemClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    ) {
        holder.itemView.setOnClickListener { onClickItem(item) }
    }

    override fun onBindOtherView(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        if (holder !is WordSearchResultDayViewHolder) throw IllegalStateException()
        if (item !is WordSearchResultDayListItem) throw IllegalStateException()

        onBindTitle(holder, item)
        onBindItem(holder, item)
    }

    private fun onBindTitle(
        holder: WordSearchResultDayViewHolder,
        item: WordSearchResultDayListItem
    ) {
        val title = item.title
        holder.binding.textTitle.text = title
    }

    private fun onBindItem(
        holder: WordSearchResultDayViewHolder,
        item: WordSearchResultDayListItem
    ) {
        holder.binding.apply {
            val strItemNumber = context.getString(R.string.fragment_word_search_result_item) + item.itemNumber
            textItemNumber.text = strItemNumber
            textItemTitle.text = item.itemTitle
            textItemComment.text = item.itemComment
        }

    }

    class WordSearchResultDayViewHolder(val binding: RowWordSearchResultListBinding)
        : RecyclerView.ViewHolder(binding.root)

    private class DiffUtilItemCallback : DiaryDayListBaseAdapter.DiffUtilItemCallback() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListBaseItem,
            newItem: DiaryDayListBaseItem
        ): Boolean {
            if (oldItem !is WordSearchResultDayListItem) throw IllegalStateException()
            if (newItem !is WordSearchResultDayListItem) throw IllegalStateException()

            if (oldItem.title != newItem.title) {
                Log.d(logTag, "areContentsTheSame()_Title不一致")
                return false
            }
            if (oldItem.itemNumber !== newItem.itemNumber) {
                Log.d(logTag, "areContentsTheSame()_ItemNumber不一致")
                return false
            }
            if (oldItem.itemTitle != newItem.itemTitle) {
                Log.d(logTag, "areContentsTheSame()_ItemTitle不一致")
                return false
            }
            if (oldItem.itemComment != newItem.itemComment) {
                Log.d(logTag, "areContentsTheSame()_ItemComment不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
