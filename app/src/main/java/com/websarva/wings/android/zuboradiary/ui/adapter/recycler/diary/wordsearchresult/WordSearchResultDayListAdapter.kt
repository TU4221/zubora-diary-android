package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.RowWordSearchResultListBinding
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryDayListBaseDiffUtilItemCallback
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult.WordSearchResultDayListAdapter.WordSearchResultDayViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.utils.toDiaryListDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import java.text.NumberFormat

internal class WordSearchResultDayListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColorUi
) : ListBaseAdapter<DiaryDayListItemUi.WordSearchResult, WordSearchResultDayViewHolder>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun build() {
        super.build()

        // MEMO:DiaryYearMonthListBaseAdapter#build()内にて理由記載)
        recyclerView.itemAnimator = null
    }

    // HACK:ネストされたRecyclerViewの描画タイミングの問題への対応。
    //      Adapterインスタンスを再生成することで、親と子のリストのレイアウトパスが
    //      より同期的に実行されるようになり、表示のズレが軽減される模様。
    //      通常のデータ更新方法（空リストsubmit後に新データをsubmit等）では、
    //      このタイミング問題は十分に解決できなかった。
    //      親リスト(DiaryYearMonthList)のアイテムが、子リスト(DiaryDayList)の内容よりも
    //      先に描画されてしまうため、リスト更新時にセクションバーが一瞬ちらつく。
    //      このAdapter再生成は、そのちらつきを軽減するための策。
    //      (ViewHolderの完全再生成によるパフォーマンス影響に注意)
    fun refreshAdapter(): WordSearchResultDayListAdapter {
        val oldAdapter = this
        val newAdapter =
            WordSearchResultDayListAdapter(
                oldAdapter.recyclerView as SwipeRecyclerView,
                oldAdapter.themeColor
            ).apply {
                build()
                oldAdapter.onItemClickListener?.let { registerOnItemClickListener(it) }
            }
        return newAdapter
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
        item: DiaryDayListItemUi.WordSearchResult
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
            item: DiaryDayListItemUi.WordSearchResult,
            themeColor: ThemeColorUi,
            onItemClick: (DiaryDayListItemUi.WordSearchResult) -> Unit,
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
                    context.getString(R.string.fragment_word_search_result_item) + item.itemNumber.value
                textItemNumber.text = strItemNumber
                textItemTitle.text = item.createItemTitleSpannableString(context, themeColor)
                textItemComment.text = item.createItemCommentSpannableString(context, themeColor)
            }
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun DiaryDayListItemUi.WordSearchResult
            .createTitleSpannableString(context: Context, themeColor: ThemeColorUi): SpannableString {
            return toSpannableString(context, title, searchWord, themeColor)
        }

        private fun DiaryDayListItemUi.WordSearchResult
            .createItemTitleSpannableString(context: Context, themeColor: ThemeColorUi): SpannableString {
            return toSpannableString(context, itemTitle, searchWord, themeColor)
        }

        private fun DiaryDayListItemUi.WordSearchResult
            .createItemCommentSpannableString(context: Context, themeColor: ThemeColorUi): SpannableString {
            return toSpannableString(context, itemComment, searchWord, themeColor)
        }

        // 対象ワードをマーキング
        private fun toSpannableString(
            context: Context,
            string: String,
            targetWord: String,
            themeColor: ThemeColorUi
        ): SpannableString {
            val spannableString = SpannableString(string)
            var fromIndex = 0
            while (string.indexOf(targetWord, fromIndex) != -1) {
                val textColor = themeColor.getOnTertiaryContainerColor(context.resources)
                val backgroundColor = themeColor.getTertiaryContainerColor(context.resources)
                val backgroundColorSpan = BackgroundColorSpan(backgroundColor)
                val foregroundColorSpan = ForegroundColorSpan(textColor)
                val start = string.indexOf(targetWord, fromIndex)
                val end = start + targetWord.length
                spannableString.setSpan(
                    backgroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannableString.setSpan(
                    foregroundColorSpan,
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                fromIndex = end
            }
            return spannableString
        }
    }

    private class DiffUtilItemCallback :
        DiaryDayListBaseDiffUtilItemCallback<DiaryDayListItemUi.WordSearchResult>() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryDayListItemUi.WordSearchResult,
            newItem: DiaryDayListItemUi.WordSearchResult
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
