package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryDayListItem.Standard.toUiModel(
    processFileNameToPath: suspend (DiaryImageFileName?) -> FilePathUi?
): DiaryDayListItemUi.Standard {
    return DiaryDayListItemUi.Standard(
        id.value,
        date,
        title.value,
        imageFileName?.fullName,
        processFileNameToPath(imageFileName)
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItem.WordSearchResult.toUiModel(): DiaryDayListItemUi.WordSearchResult {
    return DiaryDayListItemUi.WordSearchResult(
        id.value,
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
        DiaryId(id),
        date,
        DiaryTitle(title),
        imageFileName?.let { DiaryImageFileName(it) }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItemUi.WordSearchResult.toDomainModel(): DiaryDayListItem.WordSearchResult {
    return DiaryDayListItem.WordSearchResult(
        DiaryId(id),
        date,
        DiaryTitle(title),
        DiaryItemNumber(itemNumber),
        DiaryItemTitle(itemTitle),
        DiaryItemComment(itemComment),
        SearchWord(searchWord)
    )
}
