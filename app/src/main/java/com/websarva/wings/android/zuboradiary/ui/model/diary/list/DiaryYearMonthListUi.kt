package com.websarva.wings.android.zuboradiary.ui.model.diary.list

internal data class DiaryYearMonthListUi<T: DiaryDayListItemUi>(
    val itemList: List<DiaryYearMonthListItemUi<T>> = emptyList()
) {

    val isNotEmpty get() = itemList.isNotEmpty()

    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item is DiaryYearMonthListItemUi.Diary) {
                count += item.diaryDayList.countDiaries()
            }
        }
        return count
    }
}
