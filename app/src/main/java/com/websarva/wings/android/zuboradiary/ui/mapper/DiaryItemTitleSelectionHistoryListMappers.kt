package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryList
import com.websarva.wings.android.zuboradiary.ui.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListUi

internal fun DiaryItemTitleSelectionHistoryList.toUiModel(): DiaryItemTitleSelectionHistoryListUi {
    return DiaryItemTitleSelectionHistoryListUi(
        itemList.map { it.toUiModel() }
    )
}
