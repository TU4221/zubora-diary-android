package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import java.time.LocalDate

internal abstract class DiaryDayListBaseItem(
    val date: LocalDate
) {

    fun areItemsTheSame(item: DiaryDayListBaseItem): Boolean {
        if (this === item) return true

        return date == item.date
    }

    abstract fun areContentsTheSame(item: DiaryDayListBaseItem): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is  DiaryDayListBaseItem) return false

        return date == other.date
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }
}
