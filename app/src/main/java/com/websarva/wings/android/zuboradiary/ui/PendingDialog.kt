package com.websarva.wings.android.zuboradiary.ui

sealed interface PendingDialog

sealed class DiaryEditPendingDialog : PendingDialog {
    data object DiaryLoading: DiaryEditPendingDialog()
    data object DiaryLoadingFailure: DiaryEditPendingDialog()
    data object WeatherInfoFetching: DiaryEditPendingDialog()
}

sealed class DiaryShowPendingDialog : PendingDialog {
    data object DiaryLoadingFailure: DiaryShowPendingDialog()
}
