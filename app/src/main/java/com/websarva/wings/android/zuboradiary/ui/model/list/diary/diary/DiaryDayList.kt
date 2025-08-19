package com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary

import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayBaseList

internal class DiaryDayList : DiaryDayBaseList {

    val itemList: List<DiaryDayListItem>

    val isNotEmpty get() = itemList.isNotEmpty()

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

    override fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList): DiaryDayList {
        require(additionList.isNotEmpty)

        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
