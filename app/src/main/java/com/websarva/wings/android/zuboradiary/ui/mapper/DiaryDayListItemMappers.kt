package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.SearchWord
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryDayListItem.Standard.toUiModel(
    processFileNameToPath: suspend (DiaryImageFileName?) -> ImageFilePathUi?
): DiaryDayListItemUi.Standard {
    return DiaryDayListItemUi.Standard(
        id.toUiModel(),
        date,
        title.value,
        imageFileName?.toUiModel(),
        processFileNameToPath(imageFileName)
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItem.WordSearchResult.toUiModel(): DiaryDayListItemUi.WordSearchResult {
    return DiaryDayListItemUi.WordSearchResult(
        id.toUiModel(),
        date,
        title.value,
        itemNumber.value,
        itemTitle.value,
        itemComment.value,
        searchWord.value
    )
}

@JvmName("toUiModelStandard")
internal fun DiaryDayListItemUi.Standard.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        id.toDomainModel(),
        date,
        DiaryTitle(title),
        imageFileName?.toDomainModel()
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItemUi.WordSearchResult.toDomainModel(): DiaryDayListItem.WordSearchResult {
    return DiaryDayListItem.WordSearchResult(
        id.toDomainModel(),
        date,
        DiaryTitle(title),
        ItemNumber(itemNumber),
        DiaryItemTitle(itemTitle),
        DiaryItemComment(itemComment),
        SearchWord(searchWord)
    )
}
