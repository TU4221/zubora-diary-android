package com.websarva.wings.android.zuboradiary.ui.model.parameters

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import java.io.Serializable
import java.time.LocalDate

internal data class DiaryUpdateParameters(
    val diary: Diary,
    val diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
    val loadedDate: LocalDate?,
    val loadedPicturePath: Uri?
) : Serializable
