package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import android.content.Context
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import java.time.LocalDate

internal sealed class DiaryDayListItem(
    open val date: LocalDate
) {
    fun areItemsTheSame(item: DiaryDayListItem): Boolean {
        if (this === item) return true

        return date == item.date
    }

    abstract fun areContentsTheSame(item: DiaryDayListItem): Boolean

    data class Standard(
        override val date: LocalDate,
        val title: String,
        val imageUri: Uri?
    ) : DiaryDayListItem(date) {

        override fun areContentsTheSame(item: DiaryDayListItem): Boolean {
            if (this === item) return true
            if (item !is Standard) return false

            return title == item.title && imageUri == item.imageUri
        }
    }

    data class WordSearchResult(
        override val date: LocalDate,
        val title: String,
        val itemNumber: ItemNumber,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItem(date) {

        override fun areContentsTheSame(item: DiaryDayListItem): Boolean {
            if (this === item) return true
            if (item !is WordSearchResult) return false

            return title == item.title
                    && itemNumber == item.itemNumber
                    && itemTitle == item.itemTitle
                    && itemComment == item.itemComment
        }


        // TODO:下記関数は最終的にViewHolderへ以降
        fun createTitleSpannableString(context: Context, themeColor: ThemeColor): SpannableString {
            return toSpannableString(context, title, searchWord, themeColor)
        }

        fun createItemTitleSpannableString(context: Context, themeColor: ThemeColor): SpannableString {
            return toSpannableString(context, itemTitle, searchWord, themeColor)
        }

        fun createItemCommentSpannableString(context: Context, themeColor: ThemeColor): SpannableString {
            return toSpannableString(context, itemComment, searchWord, themeColor)
        }

        // 対象ワードをマーキング
        private fun toSpannableString(
            context: Context,
            string: String,
            targetWord: String,
            themeColor: ThemeColor
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
}
