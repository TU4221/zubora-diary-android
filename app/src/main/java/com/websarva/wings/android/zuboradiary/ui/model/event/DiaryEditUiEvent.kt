package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryEditUiEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDiaryItemTitleEditFragment(val diaryItemTitleSelection: DiaryItemTitleSelectionUi) : DiaryEditUiEvent()
    internal data class NavigateDiaryLoadDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateWeatherInfoFetchDialog(val date: LocalDate) : DiaryEditUiEvent()
    internal data class NavigateDiaryItemDeleteDialog(val itemNumber: Int) : DiaryEditUiEvent()
    internal data object NavigateDiaryImageDeleteDialog : DiaryEditUiEvent()
    internal data object NavigateExitWithoutDiarySaveConfirmationDialog : DiaryEditUiEvent()
    internal data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditUiEvent()
    internal data class NavigatePreviousFragmentOnInitialDiaryLoadFailed(val result: FragmentResult.None = FragmentResult.None) : DiaryEditUiEvent()
    internal data class UpdateDiaryItemLayout(val numVisibleItems: Int) : DiaryEditUiEvent()
    internal data class TransitionDiaryItemToInvisibleState(val itemNumber: Int) : DiaryEditUiEvent()
    internal data object CheckAccessLocationPermissionBeforeWeatherInfoFetch : DiaryEditUiEvent()
    internal data object ItemAddition : DiaryEditUiEvent()
    internal data object SelectImage : DiaryEditUiEvent()
}
