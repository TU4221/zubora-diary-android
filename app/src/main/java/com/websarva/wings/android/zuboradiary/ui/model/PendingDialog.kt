package com.websarva.wings.android.zuboradiary.ui.model

import java.time.LocalDate

internal sealed interface PendingDialog

internal sealed class DiaryEditPendingDialog : PendingDialog {
    data class DiaryLoading(val date: LocalDate): DiaryEditPendingDialog()
    data class DiaryLoadingFailure(val date: LocalDate): DiaryEditPendingDialog()
    data class WeatherInfoFetching(val date: LocalDate): DiaryEditPendingDialog()
}

internal sealed class DiaryShowPendingDialog : PendingDialog {
    data class DiaryLoadingFailure(val date: LocalDate): DiaryShowPendingDialog()
}
