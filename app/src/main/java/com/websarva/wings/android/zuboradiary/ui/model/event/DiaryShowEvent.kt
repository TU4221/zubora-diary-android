package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import java.time.LocalDate

sealed class DiaryShowEvent : UiEvent {
    internal data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowEvent()
    internal data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowEvent()
    internal data class NavigateDiaryDeleteDialog(val parameters: DiaryDeleteParameters) : DiaryShowEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryShowEvent()
}
