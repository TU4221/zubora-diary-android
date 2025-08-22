package com.websarva.wings.android.zuboradiary.domain.model.list.diary

import java.time.YearMonth

internal sealed class DiaryYearMonthListItem<T : DiaryDayListItem> {

    data class Diary<T : DiaryDayListItem>(
        val yearMonth: YearMonth,
        val diaryDayList: DiaryDayList<T>
    ) : DiaryYearMonthListItem<T>()

    class NoDiaryMessage<T : DiaryDayListItem> : DiaryYearMonthListItem<T>()

    class ProgressIndicator<T : DiaryDayListItem> : DiaryYearMonthListItem<T>()
}
