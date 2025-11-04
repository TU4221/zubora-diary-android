package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryYearMonthListUi<T: DiaryDayListItemUi>(
    val itemList: List<DiaryYearMonthListItemUi<T>> = emptyList()
) : Parcelable {

    val isEmpty get() = itemList.isEmpty()

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
