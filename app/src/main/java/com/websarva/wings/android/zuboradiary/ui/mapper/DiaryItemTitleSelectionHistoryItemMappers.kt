package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.ui.model.list.selectionhistory.SelectionHistoryListItem

internal fun DiaryItemTitleSelectionHistoryItem.toUiModel(): SelectionHistoryListItem {
    return SelectionHistoryListItem(title)
}
