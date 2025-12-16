package com.websarva.wings.android.zuboradiary.ui.diary.common.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionHistoryUi

internal fun DiaryItemTitleSelectionHistory.toUiModel(): DiaryItemTitleSelectionHistoryUi {
    return DiaryItemTitleSelectionHistoryUi(
        id.value,
        title.value,
        log
    )
}

internal fun DiaryItemTitleSelectionHistoryUi.toDomainModel(): DiaryItemTitleSelectionHistory {
    return DiaryItemTitleSelectionHistory(
        DiaryItemTitleSelectionHistoryId(id),
        DiaryItemTitle(title),
        log
    )
}
