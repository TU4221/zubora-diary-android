package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import java.io.Serializable

internal data class DiaryUpdateParameters(
    val diary: Diary,
    val diaryItemTitleSelectionHistoryItemList: List<DiaryItemTitleSelectionHistoryItem>,
    val originalDiary: Diary,
    val isNewDiary: Boolean
) : Serializable
