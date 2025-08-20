package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryDayListBaseDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult.WordSearchResultDayListAdapter.WordSearchResultDayViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import java.text.NumberFormat

internal class WordSearchResultDayListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : ListBaseAdapter<DiaryDayListItem.WordSearchResult, WordSearchResultDayViewHolder>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun build() {
        super.build()

        // MEMO:DiaryYearMonthListBaseAdapter#build()内にて理由記載)
        recyclerView.itemAnimator = null
    }

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): WordSearchResultDayViewHolder {
        val binding =
            RowWordSearchResultListBinding.inflate(themeColorInflater, parent, false)
        return WordSearchResultDayViewHolder(binding)
    }

    override fun bindViewHolder(
        holder: WordSearchResultDayViewHolder,
        item: DiaryDayListItem.WordSearchResult
    ) {
        holder.bind(
            item,
            themeColor
        ) { onItemClickListener?.onClick(it) }
    }

    class WordSearchResultDayViewHolder(
        val binding: RowWordSearchResultListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: DiaryDayListItem.WordSearchResult,
            themeColor: ThemeColor,
            onItemClick: (DiaryDayListItem.WordSearchResult) -> Unit,
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

    private class DiffUtilItemCallback :
        DiaryDayListBaseDiffUtilItemCallback<DiaryDayListItem.WordSearchResult>() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListItem.WordSearchResult,
            newItem: DiaryDayListItem.WordSearchResult
        ): Boolean {
            val result =
                oldItem.title == newItem.title
                        && oldItem.itemNumber == newItem.itemNumber
                        && oldItem.itemTitle == newItem.itemTitle
                        && oldItem.itemComment == newItem.itemComment

            Log.d(
                logTag,
                "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
            )
            return result
        }
    }
}
