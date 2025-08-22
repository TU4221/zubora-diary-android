package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import java.time.YearMonth

internal sealed class DiaryYearMonthListItemUi<T : DiaryDayListItemUi> {

    data class Diary<T : DiaryDayListItemUi>(
        val yearMonth: YearMonth,
        val diaryDayList: DiaryDayListUi<T>
    ) : DiaryYearMonthListItemUi<T>()

    class NoDiaryMessage<T : DiaryDayListItemUi> : DiaryYearMonthListItemUi<T>()

    class ProgressIndicator<T : DiaryDayListItemUi> : DiaryYearMonthListItemUi<T>()
}
