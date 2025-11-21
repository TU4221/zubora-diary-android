package com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.databinding.RowDiaryListWordSearchResultBinding
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.WordSearchResultDiaryListAdapter.DiaryListWordSearchResultViewHolder
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemUi
import com.websarva.wings.android.zuboradiary.ui.utils.asDiaryListDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.utils.asOnTertiaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asTertiaryContainerColorInt
import java.text.NumberFormat

/**
 * ワード検索結果リスト(`RecyclerView`)に表示されるリストのアダプター。
 *
 * [DiaryListBaseAdapter]を継承し、検索結果のリストアイテムに特化したViewHolderの生成とデータバインドを実装する。
 *
 * @param themeColor アイテムのViewに適用するテーマカラー。
 * @param onDiaryClick 日記アイテムがクリックされた際のコールバック。
 */
internal class WordSearchResultDiaryListAdapter (
    themeColor: ThemeColorUi,
    private val onDiaryClick: (DiaryListItemContainerUi.WordSearchResult) -> Unit
) : DiaryListBaseAdapter<
        DiaryListItemContainerUi.WordSearchResult, DiaryListWordSearchResultViewHolder>(themeColor) {

    override fun onCreateDiaryViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater
    ): DiaryListViewHolder {
        val binding =
            RowDiaryListWordSearchResultBinding.inflate(themeColorInflater, parent, false)
        return DiaryListWordSearchResultViewHolder(binding, themeColor, onDiaryClick)
    }

    override fun onBindDiaryViewHolder(
        holder: DiaryListViewHolder,
        item: DiaryListItemUi.Diary<DiaryListItemContainerUi.WordSearchResult>
    ) {
        if (holder is DiaryListWordSearchResultViewHolder) {
            holder.bind(item.containerUi)
        } else {
            Log.e(logTag, "予期しないViewHolderの型。")
        }
    }

    /**
     * ワード検索結果のリストアイテムデータ([DiaryListItemContainerUi.WordSearchResult])を
     * 表示するためのViewHolder。
     *
     * @property binding View Bindingのインスタンス。
     * @property themeColor アイテムのViewに適用するテーマカラー。
     * @property onDiaryClick 日記アイテムがクリックされた際のコールバック。
     */
    data class DiaryListWordSearchResultViewHolder(
        private val binding: RowDiaryListWordSearchResultBinding,
        private val themeColor: ThemeColorUi,
        private val onDiaryClick: (DiaryListItemContainerUi.WordSearchResult) -> Unit
    ) : DiaryListViewHolder(binding.root) {

        /**
         * 検索結果アイテムのデータをViewにバインドする。
         * @param item 表示する検索結果アイテム。
         */
        fun bind(item: DiaryListItemContainerUi.WordSearchResult) {
            with(binding) {
                val context = root.context
                val date = item.date
                val dayOfWeekString = date.dayOfWeek.asDiaryListDayOfWeekString(context)
                includeDay.textDayOfWeek.text = dayOfWeekString
                includeDay.textDayOfMonth.text = NumberFormat.getInstance().format(date.dayOfMonth)

                val title = item.createTitleSpannableString(context, themeColor)
                textTitle.text = title

                val strItemNumber =
                    context.getString(R.string.fragment_word_search_result_item) + item.itemNumber
                textItemNumber.text = strItemNumber
                textItemTitle.text = item.createItemTitleSpannableString(context, themeColor)
                textItemComment.text = item.createItemCommentSpannableString(context, themeColor)

                root.setOnClickListener { onDiaryClick(item) }
            }
        }

        /**
         * 日記タイトル内の検索ワードをハイライトした[SpannableString]を生成する。
         * @param context コンテキスト。
         * @param themeColor テーマカラー。
         * @return ハイライトされたタイトル文字列。
         */
        private fun DiaryListItemContainerUi.WordSearchResult.createTitleSpannableString(
            context: Context,
            themeColor: ThemeColorUi
        ): SpannableString {
            return toSpannableString(context, title, searchWord, themeColor)
        }

        /**
         * 項目タイトル内の検索ワードをハイライトした[SpannableString]を生成する。
         * @param context コンテキスト。
         * @param themeColor テーマカラー。
         * @return ハイライトされた項目タイトル文字列。
         */
        private fun DiaryListItemContainerUi.WordSearchResult
                .createItemTitleSpannableString(context: Context, themeColor: ThemeColorUi): SpannableString {
            return toSpannableString(context, itemTitle, searchWord, themeColor)
        }

        /**
         * 項目コメント内の検索ワードをハイライトした[SpannableString]を生成する。
         * @param context コンテキスト。
         * @param themeColor テーマカラー。
         * @return ハイライトされた項目コメント文字列。
         */
        private fun DiaryListItemContainerUi.WordSearchResult.createItemCommentSpannableString(
            context: Context,
            themeColor: ThemeColorUi
        ): SpannableString {
            return toSpannableString(context, itemComment, searchWord, themeColor)
        }

        /**
         * 指定された文字列内の[targetWord]に一致するすべての部分をハイライトした[SpannableString]を返す。
         * @param context コンテキスト。
         * @param string 対象の文字列。
         * @param targetWord ハイライトするワード。
         * @param themeColor テーマカラー。
         * @return ハイライトされたSpannableString。
         */
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
                val textColor = themeColor.asOnTertiaryContainerColorInt(context.resources)
                val backgroundColor = themeColor.asTertiaryContainerColorInt(context.resources)
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
}
