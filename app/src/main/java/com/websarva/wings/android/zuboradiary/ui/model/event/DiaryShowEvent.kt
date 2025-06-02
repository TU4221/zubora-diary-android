package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

internal sealed class DiaryShowEvent : ViewModelEvent() {
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowEvent()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowEvent()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowEvent()
    data class NavigatePreviousFragment(val result: FragmentResult.Some<LocalDate>) : DiaryShowEvent()
    data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryShowEvent()
}
