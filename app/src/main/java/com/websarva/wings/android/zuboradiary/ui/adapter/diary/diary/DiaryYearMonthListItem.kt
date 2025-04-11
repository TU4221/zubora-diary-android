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
        require(diaryDayList.diaryDayListItemList.isNotEmpty())

        this.diaryDayList = diaryDayList
    }
}
