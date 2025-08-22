package com.websarva.wings.android.zuboradiary.ui.model.list.diary

internal data class DiaryDayListUi<T: DiaryDayListItemUi>(
    val itemList: List<T>
) {

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(itemList.isNotEmpty())
    }

    fun countDiaries(): Int {
        return itemList.size
    }
}
