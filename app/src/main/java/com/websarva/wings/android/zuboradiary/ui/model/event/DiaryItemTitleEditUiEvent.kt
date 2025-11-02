package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi

sealed class DiaryItemTitleEditUiEvent : UiEvent {
    internal data class NavigateSelectionHistoryItemDeleteDialog(val itemTitle: String) : DiaryItemTitleEditUiEvent()
    internal data object CloseSwipedItem : DiaryItemTitleEditUiEvent()
    internal data class CompleteEdit(val diaryItemTitleSelection: DiaryItemTitleSelectionUi) : DiaryItemTitleEditUiEvent()
}
