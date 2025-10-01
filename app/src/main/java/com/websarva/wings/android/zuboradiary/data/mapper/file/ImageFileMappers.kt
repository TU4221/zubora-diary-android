package com.websarva.wings.android.zuboradiary.data.mapper.file

import com.websarva.wings.android.zuboradiary.data.file.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.FileName


internal fun ImageFileName.toDomainModel(): FileName {
    return FileName(fullName)
}

internal fun FileName.toImageFileNameDataModel(): ImageFileName {
    return ImageFileName(fullName)
}
