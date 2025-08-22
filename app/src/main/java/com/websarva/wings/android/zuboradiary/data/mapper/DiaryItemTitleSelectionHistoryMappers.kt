package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.list.selectionhistory.SelectionHistoryListItem

internal fun DiaryItemTitleSelectionHistoryItemEntity.toDomainModel(): SelectionHistoryListItem {
    return SelectionHistoryListItem(title)
}

internal fun DiaryItemTitleSelectionHistoryItem.toDataModel(): DiaryItemTitleSelectionHistoryItemEntity {
    return DiaryItemTitleSelectionHistoryItemEntity(
        title,
        log.toString()
    )
}
