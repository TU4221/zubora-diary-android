package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity
import java.time.LocalDateTime

internal class SelectionHistoryListItem(item: DiaryItemTitleSelectionHistoryItemEntity) {
    val title: String = item.title
    val log: LocalDateTime = LocalDateTime.parse(item.log)
}
