package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class InputTextValidationResult {
    data object Valid : InputTextValidationResult()
    data object InvalidEmpty : InputTextValidationResult()
    data object InvalidInitialCharUnmatched : InputTextValidationResult()
}
