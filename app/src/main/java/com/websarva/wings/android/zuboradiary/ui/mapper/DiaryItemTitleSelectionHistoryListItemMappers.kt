package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItemUi

internal fun DiaryItemTitleSelectionHistoryListItem.toUiModel(): DiaryItemTitleSelectionHistoryListItemUi {
    return DiaryItemTitleSelectionHistoryListItemUi(id.toUiModel(), title.value)
}
