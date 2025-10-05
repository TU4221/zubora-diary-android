package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItemData
import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import java.time.LocalDate

internal fun DiaryListItemData.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        UUIDString(id),
        LocalDate.parse(date),
        title,
        imageFileName?.let { FileName(it) }
    )
}
