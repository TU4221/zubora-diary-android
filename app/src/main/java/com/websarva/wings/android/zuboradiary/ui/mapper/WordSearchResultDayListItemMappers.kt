package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem

internal fun WordSearchResultListItem.toUiModel(searchWord: String): DiaryDayListItem.WordSearchResult {
    val diaryItem = extractTargetItem(this, searchWord)
    val _itemNumber = diaryItem[itemNumberKey] as Int

    val title = this.title
    val itemNumber = ItemNumber(_itemNumber)
    val itemTitle = diaryItem[itemTitleKey] as String
    val itemComment = diaryItem[itemCommentKey] as String
    val searchWord = searchWord


    return DiaryDayListItem.WordSearchResult(
        date,
        title,
        itemNumber,
        itemTitle,
        itemComment,
        searchWord
    )
}
// TODO:下記関数は最終的にUseCaseへ以降
private val itemNumberKey = "ItemNumber"
private val itemTitleKey = "ItemTitle"
private val itemCommentKey = "ItemComment"
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
        val targetItemTitle = itemTitles[i] ?: continue
        val targetItemComment = itemComments[i] ?: continue

        if (targetItemTitle.matches(regex.toRegex())
            || targetItemComment.matches(regex.toRegex())
        ) {
            itemNumber = i + 1
            itemTitle = targetItemTitle
            itemComment = targetItemComment
            break
        }
    }

    // 検索ワードが項目タイトル、コメントに含まれていない場合、アイテムNo.1を抽出
    if (itemNumber == 0) {
        itemNumber = 1
        itemTitle = itemTitles[0] ?: throw IllegalStateException()
        itemComment = itemComments[0] ?: throw IllegalStateException()
    }

    val result: MutableMap<String, Any> = HashMap()
    result[itemNumberKey] = itemNumber
    result[itemTitleKey] = itemTitle
    result[itemCommentKey] = itemComment
    return result
}
