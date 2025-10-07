package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryShowEvent : UiEvent {
    internal data class NavigateDiaryEditFragment(val id: DiaryIdUi, val date: LocalDate) : DiaryShowEvent()
    internal data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryShowEvent()
    internal data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowEvent()
    internal data class NavigatePreviousFragmentOnDiaryDeleted(
        val result: FragmentResult.Some<LocalDate>
    ) : DiaryShowEvent()
    internal data class NavigatePreviousFragmentOnDiaryLoadFailed(
        val result: FragmentResult.None = FragmentResult.None
    ) : DiaryShowEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryShowEvent()
}
