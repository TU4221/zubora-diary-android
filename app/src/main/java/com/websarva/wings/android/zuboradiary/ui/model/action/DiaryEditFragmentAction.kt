package com.websarva.wings.android.zuboradiary.ui.model.action

import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import java.time.LocalDate

internal sealed class DiaryEditFragmentAction : FragmentAction() {
    data class DiaryShowFragment(val date: LocalDate) : DiaryEditFragmentAction()
    data class DiaryItemTitleEditFragment(
        val itemNumber: ItemNumber,
        val itemTitle: String
    ) : DiaryEditFragmentAction()
    data class NavigateDiaryLoadingDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateWeatherInfoFetchingDialog(val date: LocalDate) : DiaryEditFragmentAction()
    data class NavigateDiaryItemDeleteDialog(val itemNumber: ItemNumber) : DiaryEditFragmentAction()
    data object NavigateDiaryPictureDeleteDialog : DiaryEditFragmentAction()
    data object NavigatePreviousFragmentOnDiaryDelete : DiaryEditFragmentAction()
}
