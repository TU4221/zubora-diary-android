package com.websarva.wings.android.zuboradiary.ui.model

import java.time.LocalDate

sealed interface PendingDialog

sealed class DiaryEditPendingDialog : PendingDialog {
    data class DiaryLoading(val date: LocalDate): DiaryEditPendingDialog()
    data class DiaryLoadingFailure(val date: LocalDate): DiaryEditPendingDialog()
    data class WeatherInfoFetching(val date: LocalDate): DiaryEditPendingDialog()
}

sealed class DiaryShowPendingDialog : PendingDialog {
    data class DiaryLoadingFailure(val date: LocalDate): DiaryShowPendingDialog()
}
