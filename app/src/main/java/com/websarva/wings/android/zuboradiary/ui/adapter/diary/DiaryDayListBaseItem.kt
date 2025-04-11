package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryListBaseItem
import java.time.LocalDate

internal abstract class DiaryDayListBaseItem(listItem: DiaryListBaseItem) {
    val date: LocalDate = listItem.date.let { LocalDate.parse(it) }
}
