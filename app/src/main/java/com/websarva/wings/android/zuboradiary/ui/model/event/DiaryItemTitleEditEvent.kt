package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters

sealed class DiaryItemTitleEditEvent : UiEvent {
    internal data class NavigateSelectionHistoryItemDeleteDialog(
        val parameters: DiaryItemTitleSelectionHistoryItemDeleteParameters
    ) : DiaryItemTitleEditEvent()
    internal data object CloseSwipedItem : DiaryItemTitleEditEvent()
    internal data class CompleteEdit(val diaryItemTitle: DiaryItemTitle) : DiaryItemTitleEditEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryItemTitleEditEvent()
}
