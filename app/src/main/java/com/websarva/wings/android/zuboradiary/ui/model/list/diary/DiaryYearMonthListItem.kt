package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import java.time.YearMonth

internal sealed class DiaryYearMonthListItem<T: DiaryDayBaseList> {

    data class Diary<T : DiaryDayBaseList>(
        val yearMonth: YearMonth,
        val diaryDayList: T
    ) : DiaryYearMonthListItem<T>()

    class NoDiaryMessage<T : DiaryDayBaseList> : DiaryYearMonthListItem<T>()

    class ProgressIndicator<T : DiaryDayBaseList> : DiaryYearMonthListItem<T>()
}
