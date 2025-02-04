package com.websarva.wings.android.zuboradiary.ui.list.diarylist

class DiaryDayList {

    val diaryDayListItemList: List<DiaryDayListItem>

    constructor(itemList: List<DiaryDayListItem>) {
        require(itemList.isNotEmpty())

        this.diaryDayListItemList = itemList.toList()
    }

    constructor(): this(ArrayList())

    fun countDiaries(): Int {
        return diaryDayListItemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList): DiaryDayList {
        require(additionList.diaryDayListItemList.isNotEmpty())

        val resultItemList = diaryDayListItemList + additionList.diaryDayListItemList
        return DiaryDayList(resultItemList)
    }
}
