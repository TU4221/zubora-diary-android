package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

internal sealed class DiaryEditEvent : ViewModelEvent() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryItemTitleEditFragment(
        val itemNumber: ItemNumber,
        val itemTitle: String
    ) : DiaryEditEvent()
    data class NavigateDiaryLoadingDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateWeatherInfoFetchingDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryItemDeleteDialog(val itemNumber: ItemNumber) : DiaryEditEvent()
    data object NavigateDiaryPictureDeleteDialog : DiaryEditEvent()
    data class NavigatePreviousFragment(val result: FragmentResult<LocalDate>) : DiaryEditEvent()
    data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditEvent()
    data class TransitionDiaryItemHidedState(val itemNumber: ItemNumber) : DiaryEditEvent()
    data class CheckAccessLocationPermission(val date: LocalDate) : DiaryEditEvent()
    data object ItemAddition : DiaryEditEvent()
}
