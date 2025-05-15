package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import java.time.YearMonth

internal abstract class DiaryYearMonthListBaseItem {

    val yearMonth: YearMonth
    val viewType: DiaryYearMonthListBaseAdapter.ViewType
    abstract val diaryDayList: DiaryDayBaseList

    val isNotDiaryViewType
        get() = viewType != DiaryYearMonthListBaseAdapter.ViewType.DIARY

    constructor(viewType: DiaryYearMonthListBaseAdapter.ViewType): this(YearMonth.now(), viewType)

    constructor(yearMonth: YearMonth, viewType: DiaryYearMonthListBaseAdapter.ViewType) {
        this.yearMonth = yearMonth
        this.viewType = viewType
    }


    fun areItemsTheSame(item: DiaryYearMonthListBaseItem): Boolean {
        if (this === item) return true

        return yearMonth == item.yearMonth && viewType == item.viewType
    }

    abstract fun areContentsTheSame(item: DiaryYearMonthListBaseItem): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is  DiaryYearMonthListBaseItem) return false

        return yearMonth == other.yearMonth && viewType == other.viewType
    }

    override fun hashCode(): Int {
        var result = yearMonth.hashCode()
        result = 31 * result + viewType.hashCode()
        return result
    }
}
