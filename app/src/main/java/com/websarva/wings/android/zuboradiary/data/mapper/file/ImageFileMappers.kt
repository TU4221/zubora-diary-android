package com.websarva.wings.android.zuboradiary.data.mapper.file

import com.websarva.wings.android.zuboradiary.data.file.ImageFileName


internal fun ImageFileName.toDomainModel(): com.websarva.wings.android.zuboradiary.domain.model.ImageFileName {
    return com.websarva.wings.android.zuboradiary.domain.model.ImageFileName(fullName)
}

internal fun com.websarva.wings.android.zuboradiary.domain.model.ImageFileName.toDataModel(): ImageFileName {
    return ImageFileName(fullName)
}
