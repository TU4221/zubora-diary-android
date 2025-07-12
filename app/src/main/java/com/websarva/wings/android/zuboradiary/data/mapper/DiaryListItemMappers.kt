package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItemData
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import java.time.LocalDate

internal fun DiaryListItemData.toDomainModel(): DiaryListItem {
    return DiaryListItem(
        LocalDate.parse(date),
        title,
        picturePath
    )
}
