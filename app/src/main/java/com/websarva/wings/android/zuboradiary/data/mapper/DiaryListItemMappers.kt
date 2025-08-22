package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItemData
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import java.time.LocalDate

internal fun DiaryListItemData.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        LocalDate.parse(date),
        title,
        imageUriString
    )
}
