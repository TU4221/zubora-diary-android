package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItemData
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem

internal fun WordSearchResultListItemData.toDomainModel(): RawWordSearchResultListItem {
    return RawWordSearchResultListItem(
        date,
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
