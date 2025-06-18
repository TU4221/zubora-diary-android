package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryItemDeleteParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryLoadingParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.DiaryUpdateParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.NavigatePreviousParameters
import com.websarva.wings.android.zuboradiary.ui.model.parameters.WeatherInfoAcquisitionParameters
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import java.time.LocalDate

internal sealed class DiaryEditEvent : ViewModelEvent() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryItemTitleEditFragment(
        val itemNumber: ItemNumber,
        val itemTitle: String
    ) : DiaryEditEvent()
    data class NavigateDiaryLoadingDialog(val parameters: DiaryLoadingParameters) : DiaryEditEvent()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateDiaryUpdateDialog(val parameters: DiaryUpdateParameters) : DiaryEditEvent()
    data class NavigateDiaryDeleteDialog(val parameters: DiaryDeleteParameters) : DiaryEditEvent()
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditEvent()
    data class NavigateWeatherInfoFetchingDialog(val parameters: WeatherInfoAcquisitionParameters) : DiaryEditEvent()
    data class NavigateDiaryItemDeleteDialog(val parameters: DiaryItemDeleteParameters) : DiaryEditEvent()
    data object NavigateDiaryPictureDeleteDialog : DiaryEditEvent()
    data class NavigateExitWithoutDiarySavingConfirmationDialog(val parameters: NavigatePreviousParameters) : DiaryEditEvent()
    data class NavigatePreviousFragment(val result: FragmentResult<LocalDate>) : DiaryEditEvent()
    data class NavigatePreviousFragmentOnDiaryDelete(val result: FragmentResult.Some<LocalDate>) : DiaryEditEvent()
    data class TransitionDiaryItemHidedState(val itemNumber: ItemNumber) : DiaryEditEvent()
    data class CheckAccessLocationPermissionBeforeWeatherInfoAcquisition(val parameters: WeatherInfoAcquisitionParameters) : DiaryEditEvent()
    data object ItemAddition : DiaryEditEvent()
}
