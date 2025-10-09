package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.list.DiaryItemTitleSelectionHistoryListItemUi

internal fun DiaryItemTitleSelectionHistoryListItem.toUiModel(): DiaryItemTitleSelectionHistoryListItemUi {
    return DiaryItemTitleSelectionHistoryListItemUi(id.value, title.value)
}
