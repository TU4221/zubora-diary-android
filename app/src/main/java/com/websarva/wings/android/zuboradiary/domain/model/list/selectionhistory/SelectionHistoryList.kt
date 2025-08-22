package com.websarva.wings.android.zuboradiary.domain.model.list.selectionhistory

internal data class SelectionHistoryList(
    val itemList: List<SelectionHistoryListItem>
) {

    val isEmpty = itemList.isEmpty()
}
