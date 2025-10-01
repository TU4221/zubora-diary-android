package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi

internal fun FileName.toUiModel(): ImageFileNameUi {
    return ImageFileNameUi(this)
}

internal fun ImageFileNameUi.toDomainModel(): FileName {
    return fullName
}
