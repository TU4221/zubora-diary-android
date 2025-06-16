package com.websarva.wings.android.zuboradiary.ui.model.parameters

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import java.io.Serializable
import java.time.LocalDate

internal data class DiaryUpdateParameters(
    val diaryEntity: DiaryEntity,
    val diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>,
    val loadedDate: LocalDate?,
    val loadedPicturePath: Uri?
) : Serializable
