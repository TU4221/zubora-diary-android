package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class InputTextValidateResult {
    data object Valid : InputTextValidateResult()
    data class Invalid(val errorMessageResId: Int) : InputTextValidateResult()
}
