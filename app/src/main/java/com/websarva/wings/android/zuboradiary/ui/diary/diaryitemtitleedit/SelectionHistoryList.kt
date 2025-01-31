package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

internal class SelectionHistoryList {

    val selectionHistoryListItemList: List<SelectionHistoryListItem>

    constructor(itemList: List<SelectionHistoryListItem>) {
        selectionHistoryListItemList = itemList.toList()
    }

    constructor() : this(listOf())

    fun deleteItem(position: Int): SelectionHistoryList {
        val result = selectionHistoryListItemList.toMutableList()
        result.removeAt(position)
        return SelectionHistoryList(result)
    }
}
