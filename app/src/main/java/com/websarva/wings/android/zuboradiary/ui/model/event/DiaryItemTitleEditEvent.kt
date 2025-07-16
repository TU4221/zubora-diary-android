package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemTitleSelectionHistoryItemDeleteParameters

internal sealed class DiaryItemTitleEditEvent : ViewModelEvent() {
    data class NavigateSelectionHistoryItemDeleteDialog(
        val parameters: DiaryItemTitleSelectionHistoryItemDeleteParameters
    ) : DiaryItemTitleEditEvent()
    data object CloseSwipedItem : DiaryItemTitleEditEvent()
    data class CompleteEdit(val diaryItemTitle: DiaryItemTitle) : DiaryItemTitleEditEvent()
}
