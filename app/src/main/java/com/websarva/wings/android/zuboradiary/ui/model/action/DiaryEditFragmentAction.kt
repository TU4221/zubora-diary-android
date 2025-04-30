package com.websarva.wings.android.zuboradiary.ui.model.action

import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import java.time.LocalDate

internal sealed class DiaryEditFragmentAction : FragmentAction() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryLoadingDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateWeatherInfoFetchingDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data object NavigateDiaryPictureDeleteDialog : DiaryEditFragmentAction()
    data object NavigatePreviousFragmentOnDiaryDelete : DiaryEditFragmentAction()
}
