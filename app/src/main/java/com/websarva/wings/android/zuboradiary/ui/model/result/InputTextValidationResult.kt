package com.websarva.wings.android.zuboradiary.ui.model.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class InputTextValidationResult : Parcelable {
    data object Valid : InputTextValidationResult()
    data object Invalid : InputTextValidationResult()
    data object InvalidEmpty : InputTextValidationResult()
    data object InvalidInitialCharUnmatched : InputTextValidationResult()
}
