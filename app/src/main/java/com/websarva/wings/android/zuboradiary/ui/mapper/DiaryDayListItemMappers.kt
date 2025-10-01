package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryDayListItem.Standard.toUiModel(
    processFileNameToPath: suspend (FileName?) -> ImageFilePathUi?
): DiaryDayListItemUi.Standard {
    return DiaryDayListItemUi.Standard(
        date,
        title,
        imageFileName?.toUiModel(),
        processFileNameToPath(imageFileName)
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
        imageFileName?.toDomainModel()
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
