package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import java.io.Serializable

internal data class DiaryUpdateParameters(
    val diary: Diary,
    val diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
    val originalDiary: Diary,
    val isNewDiary: Boolean
) : Serializable
