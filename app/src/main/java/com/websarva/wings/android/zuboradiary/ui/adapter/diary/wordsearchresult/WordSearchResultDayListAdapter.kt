package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import java.text.NumberFormat

internal class WordSearchResultDayListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : DiaryDayListBaseAdapter(recyclerView, themeColor, DiffUtilItemCallback()) {

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
        val context = holder.binding.root.context
        val dayOfWeekString = date.dayOfWeek.toDiaryListDayOfWeekString(context)
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
        val context = holder.binding.root.context
        val title = item.createTitleSpannableString(context, themeColor)
        holder.binding.textTitle.text = title
    }

    private fun onBindItem(
        holder: WordSearchResultDayViewHolder,
        item: WordSearchResultDayListItem
    ) {
        holder.binding.apply {
            val context = root.context
            val strItemNumber =
                context.getString(R.string.fragment_word_search_result_item) + item.itemNumber
            textItemNumber.text = strItemNumber
            textItemTitle.text = item.createItemTitleSpannableString(context, themeColor)
            textItemComment.text = item.createItemCommentSpannableString(context, themeColor)
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

            if (!oldItem.areContentsTheSame(newItem)) {
                Log.d(logTag, "areContentsTheSame()_不一致")
                return false
            }

            Log.d(logTag, "areContentsTheSame()_全項目一致")
            return true
        }
    }
}
