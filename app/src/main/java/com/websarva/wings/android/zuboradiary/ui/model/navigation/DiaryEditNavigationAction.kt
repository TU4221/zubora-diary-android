package com.websarva.wings.android.zuboradiary.ui.model.navigation

import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import java.time.LocalDate

internal sealed class DiaryEditNavigationAction : NavigationAction() {
    data class DiaryShowFragment(val date: LocalDate) : DiaryEditNavigationAction()
    data class DiaryItemTitleEditFragment(
        val itemNumber: ItemNumber,
        val itemTitle: String
    ) : DiaryEditNavigationAction()
    data class NavigateDiaryLoadingDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateWeatherInfoFetchingDialog(val date: LocalDate) : DiaryEditNavigationAction()
    data class NavigateDiaryItemDeleteDialog(val itemNumber: ItemNumber) : DiaryEditNavigationAction()
    data object NavigateDiaryPictureDeleteDialog : DiaryEditNavigationAction()
    data object NavigatePreviousFragmentOnDiaryDelete : DiaryEditNavigationAction()
}
