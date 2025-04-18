package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

internal class DiaryDayList {

    val itemList: List<DiaryDayListItem>

    constructor(itemList: List<DiaryDayListItem>) {
        require(itemList.isNotEmpty())

        this.itemList = itemList.toList()
    }

    constructor() {
        this.itemList = ArrayList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryDayList) return false

        return itemList == other.itemList
    }

    override fun hashCode(): Int {
        return itemList.hashCode()
    }

    fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList): DiaryDayList {
        require(additionList.itemList.isNotEmpty())

        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
