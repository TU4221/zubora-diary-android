package com.websarva.wings.android.zuboradiary.domain.model.list.diary

internal data class DiaryDayList<T: DiaryDayListItem>(
    val itemList: List<T>
) {

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(itemList.isNotEmpty())
    }

    fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList<T>): DiaryDayList<T> {
        require(additionList.isNotEmpty)

        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
