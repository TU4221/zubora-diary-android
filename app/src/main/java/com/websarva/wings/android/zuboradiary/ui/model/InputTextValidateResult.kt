package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class InputTextValidateResult {
    data object Valid : InputTextValidateResult()
    data object InvalidEmpty : InputTextValidateResult()
    data object InvalidInitialCharUnmatched : InputTextValidateResult()
}
