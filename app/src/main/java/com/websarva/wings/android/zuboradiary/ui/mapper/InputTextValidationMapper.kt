package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.common.InputTextValidation
import com.websarva.wings.android.zuboradiary.ui.model.state.InputTextValidationState

internal fun InputTextValidation.toUiModel(): InputTextValidationState {
    return when (this) {
        InputTextValidation.Valid -> InputTextValidationState.Valid
        InputTextValidation.Empty -> InputTextValidationState.InvalidEmpty
        InputTextValidation.InitialCharUnmatched -> InputTextValidationState.InvalidInitialCharUnmatched
    }
}
