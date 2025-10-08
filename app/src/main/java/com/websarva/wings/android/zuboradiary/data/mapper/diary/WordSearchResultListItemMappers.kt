package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.WordSearchResultListItemData
import com.websarva.wings.android.zuboradiary.domain.model.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem

internal fun WordSearchResultListItemData.toDomainModel(): RawWordSearchResultListItem {
    return RawWordSearchResultListItem(
        DiaryId(id),
        date,
        DiaryTitle(title),
        DiaryItemTitle(item1Title),
        DiaryItemComment(item1Comment),
        item2Title?.let { DiaryItemTitle(it) },
        item2Comment?.let { DiaryItemComment(it) },
        item3Title?.let { DiaryItemTitle(it) },
        item3Comment?.let { DiaryItemComment(it) },
        item4Title?.let { DiaryItemTitle(it) },
        item4Comment?.let { DiaryItemComment(it) },
        item5Title?.let { DiaryItemTitle(it) },
        item5Comment?.let { DiaryItemComment(it) },
    )
}
