package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber

internal sealed class DiaryItemTitleEditEvent : ViewModelEvent() {
    data class NavigateSelectionHistoryItemDeleteDialog(
        val itemPosition: Int,
        val itemTitle: String
    ) : DiaryItemTitleEditEvent()
    data object CloseSwipedItem : DiaryItemTitleEditEvent()
    data class CompleteEdit(
        val itemNumber: ItemNumber,
        val itemTitle: String
    ) : DiaryItemTitleEditEvent()
}
