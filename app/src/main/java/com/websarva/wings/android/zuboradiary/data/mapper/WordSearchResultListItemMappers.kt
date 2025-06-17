package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItemData
import com.websarva.wings.android.zuboradiary.domain.model.WordSearchResultListItem
import java.time.LocalDate

internal fun WordSearchResultListItemData.toDomainModel(): WordSearchResultListItem {
    return WordSearchResultListItem(
        LocalDate.parse(date),
        title,
        item1Title,
        item1Comment,
        item2Title,
        item2Comment,
        item3Title,
        item3Comment,
        item4Title,
        item4Comment,
        item5Title,
        item5Comment
    )
}
