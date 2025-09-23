package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import java.time.LocalDate

internal sealed class DiaryDayListItemUi(
    open val date: LocalDate
) {

    data class Standard(
        override val date: LocalDate,
        val title: String,
        val imageFileName: ImageFileNameUi?,
        val imageFilePath: ImageFilePathUi?
    ) : DiaryDayListItemUi(date)

    data class WordSearchResult(
        override val date: LocalDate,
        val title: String,
        val itemNumber: ItemNumber,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItemUi(date)
}
