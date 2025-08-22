package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItemUi

internal fun DiaryItemTitleSelectionHistoryListItem.toUiModel(): DiaryItemTitleSelectionHistoryListItemUi {
    return DiaryItemTitleSelectionHistoryListItemUi(title)
}
