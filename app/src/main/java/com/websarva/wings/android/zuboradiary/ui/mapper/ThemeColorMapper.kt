package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi

internal fun ThemeColor.toUiModel(): ThemeColorUi {
    return when (this) {
        ThemeColor.WHITE -> ThemeColorUi.WHITE
        ThemeColor.BLACK -> ThemeColorUi.BLACK
        ThemeColor.RED -> ThemeColorUi.RED
        ThemeColor.BLUE -> ThemeColorUi.BLUE
        ThemeColor.GREEN -> ThemeColorUi.GREEN
        ThemeColor.YELLOW -> ThemeColorUi.YELLOW
    }
}

internal fun ThemeColorUi.toDomainModel(): ThemeColor {
    return when (this) {
        ThemeColorUi.WHITE -> ThemeColor.WHITE
        ThemeColorUi.BLACK -> ThemeColor.BLACK
        ThemeColorUi.RED -> ThemeColor.RED
        ThemeColorUi.BLUE -> ThemeColor.BLUE
        ThemeColorUi.GREEN -> ThemeColor.GREEN
        ThemeColorUi.YELLOW -> ThemeColor.YELLOW
    }
}
