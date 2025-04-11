package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import java.time.YearMonth

internal abstract class DiaryYearMonthListBaseItem {

    val yearMonth: YearMonth
    val viewType: DiaryYearMonthListBaseAdapter.ViewType

    val isNotDiaryViewType
        get() = viewType != DiaryYearMonthListBaseAdapter.ViewType.DIARY

    constructor(viewType: DiaryYearMonthListBaseAdapter.ViewType): this(YearMonth.now(), viewType)

    constructor(yearMonth: YearMonth, viewType: DiaryYearMonthListBaseAdapter.ViewType) {
        this.yearMonth = yearMonth
        this.viewType = viewType
    }
}
