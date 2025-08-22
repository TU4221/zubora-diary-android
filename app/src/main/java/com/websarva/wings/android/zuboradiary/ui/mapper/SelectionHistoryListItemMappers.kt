package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.list.selectionhistory.SelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.selectionhistory.SelectionHistoryListItemUi

internal fun SelectionHistoryListItem.toUiModel(): SelectionHistoryListItemUi {
    return SelectionHistoryListItemUi(title)
}
