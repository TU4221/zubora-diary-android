package com.websarva.wings.android.zuboradiary.data.mapper

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItemData
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import java.time.LocalDate

internal fun DiaryListItemData.toDomainModel(): DiaryListItem {
    val uri =
        if (picturePath.isEmpty()) {
            null
        } else {
            Uri.parse(picturePath)
        }
    return DiaryListItem(
        LocalDate.parse(date),
        title,
        uri
    )
}
