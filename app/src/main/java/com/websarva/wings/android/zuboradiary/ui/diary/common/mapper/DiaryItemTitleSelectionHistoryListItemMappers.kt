package com.websarva.wings.android.zuboradiary.ui.diary.common.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionHistoryListItemUi

internal fun DiaryItemTitleSelectionHistoryListItem.toUiModel(): DiaryItemTitleSelectionHistoryListItemUi {
    return DiaryItemTitleSelectionHistoryListItemUi(id.value, title.value)
}
