package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItemData
import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem

internal fun DiaryListItemData.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        UUIDString(id),
        date,
        title,
        imageFileName?.let { FileName(it) }
    )
}
