package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleSelectionUi

sealed class DiaryItemTitleEditEvent : UiEvent {
    internal data class NavigateSelectionHistoryItemDeleteDialog(val itemTitle: String) : DiaryItemTitleEditEvent()
    internal data object CloseSwipedItem : DiaryItemTitleEditEvent()
    internal data class CompleteEdit(val diaryItemTitleSelection: DiaryItemTitleSelectionUi) : DiaryItemTitleEditEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryItemTitleEditEvent()
}
