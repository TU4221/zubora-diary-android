package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryShowUiEvent : UiEvent {
    internal data class NavigateDiaryEditFragment(val id: String, val date: LocalDate) : DiaryShowUiEvent()
    internal data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryShowUiEvent()
    internal data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowUiEvent()
    internal data class NavigatePreviousFragmentOnDiaryDeleted(
        val result: FragmentResult.Some<LocalDate>
    ) : DiaryShowUiEvent()
    internal data class NavigatePreviousFragmentOnDiaryLoadFailed(
        val result: FragmentResult.None = FragmentResult.None
    ) : DiaryShowUiEvent()
}
