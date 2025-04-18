package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryListBaseItem
import java.time.LocalDate

internal abstract class DiaryDayListBaseItem(listItem: DiaryListBaseItem) {
    val date: LocalDate = listItem.date.let { LocalDate.parse(it) }

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
