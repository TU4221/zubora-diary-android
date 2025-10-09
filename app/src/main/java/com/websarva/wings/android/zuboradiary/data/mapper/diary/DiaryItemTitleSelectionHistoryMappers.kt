package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryEntity
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem

internal fun DiaryItemTitleSelectionHistoryEntity.toListItemDomainModel(): DiaryItemTitleSelectionHistoryListItem {
    return DiaryItemTitleSelectionHistoryListItem(
        DiaryItemTitleSelectionHistoryId(id),
        DiaryItemTitle(title)
    )
}

internal fun DiaryItemTitleSelectionHistoryEntity.toDomainModel(): DiaryItemTitleSelectionHistory {
    return DiaryItemTitleSelectionHistory(
        DiaryItemTitleSelectionHistoryId(id),
        DiaryItemTitle(title),
        log
    )
}

internal fun DiaryItemTitleSelectionHistory.toDataModel(): DiaryItemTitleSelectionHistoryEntity {
    return DiaryItemTitleSelectionHistoryEntity(id.value, title.value, log)
}
