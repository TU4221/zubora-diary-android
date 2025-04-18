package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import java.time.YearMonth

internal class DiaryYearMonthListItem : DiaryYearMonthListBaseItem {

    val diaryDayList: DiaryDayList

   constructor(viewType: DiaryYearMonthListBaseAdapter.ViewType) : super(viewType) {
        diaryDayList = DiaryDayList()
    }

   constructor(yearMonth: YearMonth, diaryDayList: DiaryDayList) : super(
       yearMonth,
       DiaryYearMonthListBaseAdapter.ViewType.DIARY
    ) {
        require(diaryDayList.itemList.isNotEmpty())

        this.diaryDayList = diaryDayList
    }

    override fun areContentsTheSame(item: DiaryYearMonthListBaseItem): Boolean {
        if (this === item) return true
        if (item !is DiaryYearMonthListItem) return false

        return diaryDayList == item.diaryDayList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryYearMonthListItem) return false
        if (!super.equals(other)) return false

        return diaryDayList == other.diaryDayList
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + diaryDayList.hashCode()
        return result
    }
}
