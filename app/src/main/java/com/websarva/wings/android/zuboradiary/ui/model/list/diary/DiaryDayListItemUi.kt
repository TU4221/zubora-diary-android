package com.websarva.wings.android.zuboradiary.ui.model.list.diary

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import java.time.LocalDate

internal sealed class DiaryDayListItemUi(
    open val date: LocalDate
) {

    data class Standard(
        override val date: LocalDate,
        val title: String,
        val imageFileName: ImageFileName?,
        val imageFilePath: ImageFilePathUi
    ) : DiaryDayListItemUi(date) {
        init {
            require(
                when (imageFilePath) {
                    is ImageFilePathUi.Valid,
                    is ImageFilePathUi.NoImage -> imageFileName != null
                    is ImageFilePathUi.Invalid -> imageFileName == null
                }
            )
        }
    }

    data class WordSearchResult(
        override val date: LocalDate,
        val title: String,
        val itemNumber: ItemNumber,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItemUi(date)
}
