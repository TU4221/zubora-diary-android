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

internal sealed class DiaryEditEvent : ViewModelEvent() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryItemTitleEditFragment(val diaryItemTitle: DiaryItemTitle) : DiaryEditEvent()
    data class NavigateDiaryLoadingDialog(val parameters: DiaryLoadingParameters) : DiaryEditEvent()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryUpdateDialog(val parameters: DiaryUpdateParameters) : DiaryEditEvent()
    data class NavigateDiaryDeleteDialog(val parameters: DiaryDeleteParameters) : DiaryEditEvent()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateWeatherInfoFetchDialog(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    data class NavigateDiaryItemDeleteDialog(val parameters: DiaryItemDeleteParameters) : DiaryEditEvent()
    data object NavigateDiaryImageDeleteDialog : DiaryEditEvent()
    data class NavigateExitWithoutDiarySavingConfirmationDialog(val parameters: NavigatePreviousParameters) : DiaryEditEvent()
    data class NavigatePreviousFragment(val result: FragmentResult<LocalDate>) : DiaryEditEvent()
    data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditEvent()
    data class TransitionDiaryItemHidedState(val itemNumber: ItemNumber) : DiaryEditEvent()
    data class CheckAccessLocationPermissionBeforeWeatherInfoFetch(val parameters: WeatherInfoFetchParameters) : DiaryEditEvent()
    data object ItemAddition : DiaryEditEvent()
    data object SelectImage : DiaryEditEvent()
}
