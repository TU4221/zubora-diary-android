package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryEntity
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem

internal fun DiaryItemTitleSelectionHistoryEntity.toDomainModel(): DiaryItemTitleSelectionHistoryListItem {
    return DiaryItemTitleSelectionHistoryListItem(DiaryItemTitleSelectionHistoryId(id), title)
}

internal fun DiaryItemTitleSelectionHistory.toDataModel(): DiaryItemTitleSelectionHistoryEntity {
    return DiaryItemTitleSelectionHistoryEntity(id.value, title, log)
}
