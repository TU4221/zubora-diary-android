package com.websarva.wings.android.zuboradiary.ui.model.action

import android.net.Uri
import java.time.LocalDate

internal sealed class DiaryShowFragmentAction : FragmentAction() {
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryLoadingFailureDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryShowFragmentAction()
    data class NavigatePreviousDialogOnDiaryDelete(val uri: Uri?) : DiaryShowFragmentAction()
}
