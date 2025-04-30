package com.websarva.wings.android.zuboradiary.ui.model.action

import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionAction
import java.time.LocalDate

internal sealed class DiaryShowFragmentAction : FragmentAction() {
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigatePreviousDialogOnDiaryDelete(
        val uriPermissionAction: UriPermissionAction
    ) : DiaryShowFragmentAction()
}
