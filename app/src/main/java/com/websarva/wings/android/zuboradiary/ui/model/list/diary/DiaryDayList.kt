package com.websarva.wings.android.zuboradiary.ui.model.list.diary

internal class DiaryDayList<T: DiaryDayListItem>(itemList: List<T>) : DiaryDayBaseList() {

    val itemList: List<T>

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(itemList.isNotEmpty())
        this.itemList = itemList.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryDayList<*>) return false

        return itemList == other.itemList
    }

    override fun hashCode(): Int {
        return itemList.hashCode()
    }

    override fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: DiaryDayList<T>): DiaryDayList<T> {
        require(additionList.isNotEmpty)

        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
