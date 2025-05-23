package com.websarva.wings.android.zuboradiary.ui.model.action

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionAction
import java.time.LocalDate

internal sealed class DiaryEditFragmentAction : FragmentAction() {
    data class NavigateDiaryShowFragment(
        val date: LocalDate,
        val uriPermissionAction: UriPermissionAction
    ) : DiaryEditFragmentAction()
    data class NavigateDiaryItemTitleEditFragment(
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
    data class NavigatePreviousFragment(val loadedDate: LocalDate?) : DiaryEditFragmentAction()
    data class NavigatePreviousFragmentOnDiaryDelete(val loadedDate: LocalDate?, val uri: Uri?) : DiaryEditFragmentAction()
    data class TransitionDiaryItemHidedState(val itemNumber: ItemNumber) : DiaryEditFragmentAction()
    data class CheckAccessLocationPermission(val date: LocalDate) : DiaryEditFragmentAction()
}
