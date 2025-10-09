package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.ui.model.ImageFileNameUi

internal fun DiaryImageFileName.toUiModel(): ImageFileNameUi {
    return ImageFileNameUi(fullName)
}

internal fun ImageFileNameUi.toDomainModel(): DiaryImageFileName {
    return DiaryImageFileName(fullName)
}
