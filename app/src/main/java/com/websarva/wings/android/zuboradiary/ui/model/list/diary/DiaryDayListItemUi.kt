package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import java.time.LocalDate

internal sealed class DiaryDayListItemUi(
    open val id: DiaryIdUi,
    open val date: LocalDate
) {

    data class Standard(
        override val id: DiaryIdUi,
        override val date: LocalDate,
        val title: String,
        val imageFileName: ImageFileNameUi?,
        val imageFilePath: ImageFilePathUi?
    ) : DiaryDayListItemUi(id, date)

    data class WordSearchResult(
        override val id: DiaryIdUi,
        override val date: LocalDate,
        val title: String,
        val itemNumber: Int,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItemUi(id, date)
}
