package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryEditUiEvent : UiEvent {
    data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDiaryItemTitleEditFragment(val diaryItemTitleSelection: DiaryItemTitleSelectionUi) : DiaryEditUiEvent()
    data class NavigateDiaryLoadDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateWeatherInfoFetchDialog(val date: LocalDate) : DiaryEditUiEvent()
    data class NavigateDiaryItemDeleteDialog(val itemNumber: Int) : DiaryEditUiEvent()
    data object NavigateDiaryImageDeleteDialog : DiaryEditUiEvent()
    data object NavigateExitWithoutDiarySaveConfirmationDialog : DiaryEditUiEvent()
    data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditUiEvent()
    data class NavigatePreviousFragmentOnInitialDiaryLoadFailed(val result: FragmentResult.None = FragmentResult.None) : DiaryEditUiEvent()
    data class UpdateDiaryItemLayout(val numVisibleItems: Int) : DiaryEditUiEvent()
    data class TransitionDiaryItemToInvisibleState(val itemNumber: Int) : DiaryEditUiEvent()
    data object CheckAccessLocationPermissionBeforeWeatherInfoFetch : DiaryEditUiEvent()
    data object ItemAddition : DiaryEditUiEvent()
    data object SelectImage : DiaryEditUiEvent()
}
