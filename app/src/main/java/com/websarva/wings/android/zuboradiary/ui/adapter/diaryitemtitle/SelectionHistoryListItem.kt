package com.websarva.wings.android.zuboradiary.ui.adapter.diaryitemtitle

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity

internal class SelectionHistoryListItem(item: DiaryItemTitleSelectionHistoryItemEntity) {
    val title: String = item.title
}
