package com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle

internal data class DiaryItemTitleSelectionHistoryList(
    val itemList: List<DiaryItemTitleSelectionHistoryListItem>
) {

    val isEmpty = itemList.isEmpty()
}
