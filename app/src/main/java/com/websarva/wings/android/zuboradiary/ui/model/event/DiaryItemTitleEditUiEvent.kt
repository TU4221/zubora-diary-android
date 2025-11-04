package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi

sealed class DiaryItemTitleEditUiEvent : UiEvent {
    data class NavigateSelectionHistoryItemDeleteDialog(val itemTitle: String) : DiaryItemTitleEditUiEvent()
    data object CloseSwipedItem : DiaryItemTitleEditUiEvent()
    data class CompleteEdit(val diaryItemTitleSelection: DiaryItemTitleSelectionUi) : DiaryItemTitleEditUiEvent()
}
