package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import java.time.LocalDateTime

internal fun DiaryItemTitleSelectionHistoryItemEntity.toDomainModel(): DiaryItemTitleSelectionHistoryItem {
    return DiaryItemTitleSelectionHistoryItem(
        title,
        LocalDateTime.parse(log)
    )
}

internal fun DiaryItemTitleSelectionHistoryItem.toDataModel(): DiaryItemTitleSelectionHistoryItemEntity {
    return DiaryItemTitleSelectionHistoryItemEntity(
        title,
        log.toString()
    )
}
