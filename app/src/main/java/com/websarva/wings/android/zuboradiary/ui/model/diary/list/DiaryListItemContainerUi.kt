package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
sealed class DiaryListItemContainerUi(
    open val id: String,
    open val date: LocalDate
) : Parcelable {

    data class Standard(
        override val id: String,
        override val date: LocalDate,
        val title: String,
        val imageFileName: String?,
        val imageFilePath: FilePathUi?
    ) : DiaryListItemContainerUi(id, date)

    data class WordSearchResult(
        override val id: String,
        override val date: LocalDate,
        val title: String,
        val itemNumber: Int,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryListItemContainerUi(id, date)
}
