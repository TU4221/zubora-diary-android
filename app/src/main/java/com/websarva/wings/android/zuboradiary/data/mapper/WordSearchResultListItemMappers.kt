package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItemData
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem
import java.time.LocalDate

internal fun WordSearchResultListItemData.toDomainModel(): RawWordSearchResultListItem {
    return RawWordSearchResultListItem(
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
