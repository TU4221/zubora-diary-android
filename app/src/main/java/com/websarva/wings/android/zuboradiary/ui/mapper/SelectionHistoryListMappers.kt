package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.list.selectionhistory.SelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.model.list.selectionhistory.SelectionHistoryListUi

internal fun SelectionHistoryList.toUiModel(): SelectionHistoryListUi {
    return SelectionHistoryListUi(
        itemList.map { it.toUiModel() }
    )
}
