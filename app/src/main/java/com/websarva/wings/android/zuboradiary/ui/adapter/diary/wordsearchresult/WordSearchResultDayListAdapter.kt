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

    override fun createDiaryDayViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): RecyclerView.ViewHolder {
        val binding =
            RowWordSearchResultListBinding.inflate(themeColorInflater, parent, false)
        return WordSearchResultDayViewHolder(binding)
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: DiaryDayListBaseItem) {
        holder as WordSearchResultDayViewHolder
        item as WordSearchResultDayListItem

        holder.bind(
            item,
            themeColor
        ) { onClickItemListener?.onClick(it) }
    }

    class WordSearchResultDayViewHolder(
        val binding: RowWordSearchResultListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: WordSearchResultDayListItem,
            themeColor: ThemeColor,
            onItemClick: (WordSearchResultDayListItem) -> Unit,
        ) {
            val context = binding.root.context
            binding.apply {
                val date = item.date
                val dayOfWeekString = date.dayOfWeek.toDiaryListDayOfWeekString(context)
                includeDay.textDayOfWeek.text = dayOfWeekString
                includeDay.textDayOfMonth.text = NumberFormat.getInstance().format(date.dayOfMonth)

                val title = item.createTitleSpannableString(context, themeColor)
                textTitle.text = title

                val strItemNumber =
                    context.getString(R.string.fragment_word_search_result_item) + item.itemNumber
                textItemNumber.text = strItemNumber
                textItemTitle.text = item.createItemTitleSpannableString(context, themeColor)
                textItemComment.text = item.createItemCommentSpannableString(context, themeColor)
            }
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

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
