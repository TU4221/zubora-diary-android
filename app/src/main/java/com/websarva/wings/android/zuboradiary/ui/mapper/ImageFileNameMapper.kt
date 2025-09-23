package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi

internal fun ImageFileName.toUiModel(): ImageFileNameUi {
    return ImageFileNameUi(this)
}

internal fun ImageFileNameUi.toDomainModel(): ImageFileName {
    return fullName
}
