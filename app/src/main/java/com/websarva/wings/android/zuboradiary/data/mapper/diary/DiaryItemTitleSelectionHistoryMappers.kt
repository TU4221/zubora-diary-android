package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryEntity
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem

internal fun DiaryItemTitleSelectionHistoryEntity.toDomainModel(): DiaryItemTitleSelectionHistoryListItem {
    return DiaryItemTitleSelectionHistoryListItem(title)
}

internal fun DiaryItemTitleSelectionHistory.toDataModel(): DiaryItemTitleSelectionHistoryEntity {
    return DiaryItemTitleSelectionHistoryEntity(
        title,
        log.toString()
    )
}
