package com.websarva.wings.android.zuboradiary.ui.model.action

import java.time.LocalDate

internal sealed class DiaryShowFragmentAction : FragmentAction() {
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigatePreviousFragment(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigatePreviousFragmentOnDiaryDelete(val date: LocalDate) : DiaryShowFragmentAction()
}
