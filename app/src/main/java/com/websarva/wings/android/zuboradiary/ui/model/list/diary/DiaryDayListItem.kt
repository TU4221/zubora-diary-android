package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import java.time.LocalDate

internal sealed class DiaryDayListItem(
    open val date: LocalDate
) {

    data class Standard(
        override val date: LocalDate,
        val title: String,
        val imageUri: Uri?
    ) : DiaryDayListItem(date)

    data class WordSearchResult(
        override val date: LocalDate,
        val title: String,
        val itemNumber: ItemNumber,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItem(date)
}
