package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleSelectionHistoryIdUi

internal fun DiaryItemTitleSelectionHistoryId.toUiModel(): DiaryItemTitleSelectionHistoryIdUi {
    return DiaryItemTitleSelectionHistoryIdUi(value)
}

internal fun DiaryItemTitleSelectionHistoryIdUi.toDomainModel(): DiaryItemTitleSelectionHistoryId {
    return DiaryItemTitleSelectionHistoryId(value)
}
