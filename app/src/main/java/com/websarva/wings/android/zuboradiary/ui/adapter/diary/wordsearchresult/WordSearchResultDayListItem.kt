package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem

internal class WordSearchResultDayListItem(
    listItem: WordSearchResultListItem,
    searchWord: String,
    textColor: Int,
    backgroundColor: Int
) : DiaryDayListBaseItem(listItem) {

    val title: SpannableString
    val itemNumber: ItemNumber
    val itemTitle: SpannableString
    val itemComment: SpannableString

    private val itemNumberKey = "ItemNumber"
    private val itemTitleKey = "ItemTitle"
    private val itemCommentKey = "ItemComment"

    init {
        val title = listItem.title
        this.title = toSpannableString(title, searchWord, textColor, backgroundColor)

        val diaryItem = extractTargetItem(listItem, searchWord)
        val itemNumber = diaryItem[itemNumberKey] as Int
        this.itemNumber = ItemNumber(itemNumber)

        val itemTitle = diaryItem[itemTitleKey] as String
        this.itemTitle = toSpannableString(itemTitle, searchWord, textColor, backgroundColor)

        val itemComment = diaryItem[itemCommentKey] as String
        this.itemComment = toSpannableString(itemComment, searchWord, textColor, backgroundColor)
    }

    // 対象ワードをマーキング
    private fun toSpannableString(
        string: String,
        targetWord: String,
        textColor: Int,
        backgroundColor: Int
    ): SpannableString {
        val spannableString = SpannableString(string)
        var fromIndex = 0
        while (string.indexOf(targetWord, fromIndex) != -1) {
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

    private fun extractTargetItem(
        item: WordSearchResultListItem,
        searchWord: String
    ): Map<String, Any> {
        val regex = ".*$searchWord.*"
        val itemTitles = arrayOf(
            item.item1Title,
            item.item2Title,
            item.item3Title,
            item.item4Title,
            item.item5Title,
        )
        val itemComments = arrayOf(
            item.item1Comment,
            item.item2Comment,
            item.item3Comment,
            item.item4Comment,
            item.item5Comment,
        )
        var itemNumber = 0
        var itemTitle = ""
        var itemComment = ""
        for (i in itemTitles.indices) {
            if (itemTitles[i].matches(regex.toRegex())
                || itemComments[i].matches(regex.toRegex())
            ) {
                itemNumber = i + 1
                itemTitle = itemTitles[i]
                itemComment = itemComments[i]
                break
            }

            // 対象アイテムが無かった場合、アイテムNo.1を抽出
            if (i == (itemTitles.size - 1)) {
                itemNumber = 1
                itemTitle = itemTitles[0]
                itemComment = itemComments[0]
            }
        }

        val result: MutableMap<String, Any> = HashMap()
        result[itemNumberKey] = itemNumber
        result[itemTitleKey] = itemTitle
        result[itemCommentKey] = itemComment
        return result
    }

    override fun areContentsTheSame(item: DiaryDayListBaseItem): Boolean {
        if (this === item) return true
        if (item !is WordSearchResultDayListItem) return false

        return title == item.title
                && itemNumber == item.itemNumber
                && itemTitle == item.itemTitle
                && itemComment == item.itemComment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordSearchResultDayListItem) return false
        if (!super.equals(other)) return false

        return title == other.title
                && itemNumber == other.itemNumber
                && itemTitle == other.itemTitle
                && itemComment == other.itemComment
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + itemNumber.hashCode()
        result = 31 * result + itemTitle.hashCode()
        result = 31 * result + itemComment.hashCode()
        return result
    }
}
