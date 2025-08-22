package com.websarva.wings.android.zuboradiary.ui.mapper

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi

@JvmName("toUiModelStandard")
internal fun DiaryDayListItem.Standard.toUiModel(): DiaryDayListItemUi.Standard {
    return DiaryDayListItemUi.Standard(
        date,
        title,
        imageUriString?.let { Uri.parse(it) }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItem.WordSearchResult.toUiModel(): DiaryDayListItemUi.WordSearchResult {
    return DiaryDayListItemUi.WordSearchResult(
        date,
        title,
        itemNumber,
        itemTitle,
        itemComment,
        searchWord
    )
}

@JvmName("toUiModelStandard")
internal fun DiaryDayListItemUi.Standard.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        date,
        title,
        imageUri?.let { toString() }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItemUi.WordSearchResult.toDomainModel(): DiaryDayListItem.WordSearchResult {
    return DiaryDayListItem.WordSearchResult(
        date,
        title,
        itemNumber,
        itemTitle,
        itemComment,
        searchWord
    )
}
