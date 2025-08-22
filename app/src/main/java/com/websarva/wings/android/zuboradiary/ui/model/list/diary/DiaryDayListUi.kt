package com.websarva.wings.android.zuboradiary.ui.model.list.diary

internal class DiaryDayListUi<T: DiaryDayListItemUi>(itemList: List<T>) {

    val itemList: List<T>

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(itemList.isNotEmpty())
        this.itemList = itemList.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryDayListUi<*>) return false

        return itemList == other.itemList
    }

    override fun hashCode(): Int {
        return itemList.hashCode()
    }

    fun countDiaries(): Int {
        return itemList.size
    }
}
