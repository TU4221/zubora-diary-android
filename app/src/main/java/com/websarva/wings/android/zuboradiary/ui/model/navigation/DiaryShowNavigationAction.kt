package com.websarva.wings.android.zuboradiary.ui.model.navigation

import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionAction
import java.time.LocalDate

internal sealed class DiaryShowNavigationAction : NavigationAction() {
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowNavigationAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowNavigationAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowNavigationAction()
    data class NavigatePreviousDialogOnDiaryDelete(
        val uriPermissionAction: UriPermissionAction
    ) : DiaryShowNavigationAction()
}
