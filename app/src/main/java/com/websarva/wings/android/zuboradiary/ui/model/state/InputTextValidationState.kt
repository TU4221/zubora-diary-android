package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class InputTextValidationState : Parcelable {
    data object Valid : InputTextValidationState()
    data object Invalid : InputTextValidationState()
    data object InvalidEmpty : InputTextValidationState()
    data object InvalidInitialCharUnmatched : InputTextValidationState()
}
