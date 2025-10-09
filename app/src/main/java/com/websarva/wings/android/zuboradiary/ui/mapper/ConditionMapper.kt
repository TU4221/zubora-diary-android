package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.Condition
import com.websarva.wings.android.zuboradiary.ui.model.ConditionUi

internal fun Condition.toUiModel(): ConditionUi {
    return when (this) {
        Condition.UNKNOWN -> ConditionUi.UNKNOWN
        Condition.HAPPY -> ConditionUi.HAPPY
        Condition.GOOD -> ConditionUi.GOOD
        Condition.AVERAGE -> ConditionUi.AVERAGE
        Condition.POOR -> ConditionUi.POOR
        Condition.BAD -> ConditionUi.BAD
    }
}

internal fun ConditionUi.toDomainModel(): Condition {
    return when (this) {
        ConditionUi.UNKNOWN -> Condition.UNKNOWN
        ConditionUi.HAPPY -> Condition.HAPPY
        ConditionUi.GOOD -> Condition.GOOD
        ConditionUi.AVERAGE -> Condition.AVERAGE
        ConditionUi.POOR -> Condition.POOR
        ConditionUi.BAD -> Condition.BAD
    }
}
