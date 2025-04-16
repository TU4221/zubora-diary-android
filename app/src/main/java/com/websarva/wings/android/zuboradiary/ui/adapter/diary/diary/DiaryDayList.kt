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

    fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList): DiaryDayList {
        require(additionList.itemList.isNotEmpty())

        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
