package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadingParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryEditEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateDiaryItemTitleEditFragment(val diaryItemTitle: DiaryItemTitle) : DiaryEditEvent()
    internal data class NavigateDiaryLoadingDialog(val parameters: DiaryLoadingParameters) : DiaryEditEvent()
    internal data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateDiaryUpdateDialog(val parameters: DiaryUpdateParameters) : DiaryEditEvent()
    internal data class NavigateDiaryDeleteDialog(val parameters: DiaryDeleteParameters) : DiaryEditEvent()
    internal data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateWeatherInfoFetchDialog(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    internal data class NavigateDiaryItemDeleteDialog(val parameters: DiaryItemDeleteParameters) : DiaryEditEvent()
    internal data object NavigateDiaryImageDeleteDialog : DiaryEditEvent()
    internal data class NavigateExitWithoutDiarySavingConfirmationDialog(val parameters: NavigatePreviousParameters) : DiaryEditEvent()
    internal data class NavigatePreviousFragment(val result: FragmentResult<LocalDate>) : DiaryEditEvent()
    internal data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditEvent()
    internal data class TransitionDiaryItemHidedState(val itemNumber: ItemNumber) : DiaryEditEvent()
    internal data class CheckAccessLocationPermissionBeforeWeatherInfoFetch(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    internal data object ItemAddition : DiaryEditEvent()
    internal data object SelectImage : DiaryEditEvent()

    internal data class CommonEvent(val event: CommonUiEvent) : DiaryEditEvent()
}
