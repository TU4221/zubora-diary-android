package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.DiaryId
import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi

internal fun DiaryId.toUiModel(): DiaryIdUi {
    return DiaryIdUi(value)
}

internal fun DiaryIdUi.toDomainModel(): DiaryId {
    return DiaryId(value)
}
