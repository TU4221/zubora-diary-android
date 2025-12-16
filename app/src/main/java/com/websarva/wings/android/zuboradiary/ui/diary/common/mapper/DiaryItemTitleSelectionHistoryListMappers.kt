package com.websarva.wings.android.zuboradiary.ui.diary.common.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionHistoryListUi

internal fun DiaryItemTitleSelectionHistoryList.toUiModel(): DiaryItemTitleSelectionHistoryListUi {
    return DiaryItemTitleSelectionHistoryListUi(
        itemList.map { it.toUiModel() }
    )
}
