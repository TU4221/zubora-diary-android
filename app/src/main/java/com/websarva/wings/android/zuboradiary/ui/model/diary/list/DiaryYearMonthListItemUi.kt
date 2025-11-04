package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.YearMonth

@Parcelize
sealed class DiaryYearMonthListItemUi<T : DiaryDayListItemUi> : Parcelable {

    data class Diary<T : DiaryDayListItemUi>(
        val yearMonth: YearMonth,
        val diaryDayList: DiaryDayListUi<T>
    ) : DiaryYearMonthListItemUi<T>()

    class NoDiaryMessage<T : DiaryDayListItemUi> : DiaryYearMonthListItemUi<T>()

    class ProgressIndicator<T : DiaryDayListItemUi> : DiaryYearMonthListItemUi<T>()
}
