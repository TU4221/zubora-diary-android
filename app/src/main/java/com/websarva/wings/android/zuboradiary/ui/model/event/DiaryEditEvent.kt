package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParametersForDiaryEdit
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoFetchParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

sealed class DiaryEditEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateDiaryItemTitleEditFragment(val diaryItemTitle: DiaryItemTitle) : DiaryEditEvent()
    internal data class NavigateDiaryLoadDialog(val parameters: DiaryLoadParameters) : DiaryEditEvent()
    internal data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateDiaryDeleteDialog(val parameters: DiaryDeleteParameters) : DiaryEditEvent()
    internal data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditEvent()
    internal data class NavigateWeatherInfoFetchDialog(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    internal data class NavigateDiaryItemDeleteDialog(val parameters: DiaryItemDeleteParameters) : DiaryEditEvent()
    internal data object NavigateDiaryImageDeleteDialog : DiaryEditEvent()
    internal data class NavigateExitWithoutDiarySaveConfirmationDialog(val parameters: NavigatePreviousParametersForDiaryEdit) : DiaryEditEvent()
    internal data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditEvent()
    internal data class NavigatePreviousFragmentOnInitialDiaryLoadFailed(val result: FragmentResult.None = FragmentResult.None) : DiaryEditEvent()
    internal data class TransitionDiaryItemToInvisibleState(val itemNumber: Int) : DiaryEditEvent()
    internal data class CheckAccessLocationPermissionBeforeWeatherInfoFetch(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    internal data object ItemAddition : DiaryEditEvent()
    internal data object SelectImage : DiaryEditEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryEditEvent()
}
