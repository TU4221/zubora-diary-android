package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

internal class WordSearchResultDayList {

    val itemList: List<WordSearchResultDayListItem>

    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(itemList: List<WordSearchResultDayListItem>) {
        this.itemList = itemList.toList()
    }

    constructor() : this(ArrayList())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordSearchResultDayList) return false

        return itemList == other.itemList
    }

    override fun hashCode(): Int {
        return itemList.hashCode()
    }

    fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: WordSearchResultDayList): WordSearchResultDayList {
        require(additionList.isNotEmpty)

        val resultItemList =
            itemList + additionList.itemList
        return WordSearchResultDayList(resultItemList)
    }
}
