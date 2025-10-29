package com.websarva.wings.android.zuboradiary.ui.model.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class InputTextValidationState : Parcelable {
    data object Valid : InputTextValidationState()
    data object Invalid : InputTextValidationState()
    data object InvalidEmpty : InputTextValidationState()
    data object InvalidInitialCharUnmatched : InputTextValidationState()
}
