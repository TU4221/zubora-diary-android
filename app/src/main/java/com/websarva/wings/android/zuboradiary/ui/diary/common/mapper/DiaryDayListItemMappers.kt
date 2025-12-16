package com.websarva.wings.android.zuboradiary.ui.diary.common.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.common.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryListItemContainerUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryDayListItem.Standard.toUiModel(
    processBuildPathFromFileName: suspend (DiaryImageFileName?) -> FilePathUi?
): DiaryListItemContainerUi.Standard {
    return DiaryListItemContainerUi.Standard(
        id.value,
        date,
        title.value,
        imageFileName?.fullName,
        processBuildPathFromFileName(imageFileName)
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListItem.WordSearchResult.toUiModel(): DiaryListItemContainerUi.WordSearchResult {
    return DiaryListItemContainerUi.WordSearchResult(
        id.value,
        date,
        title.value,
        itemNumber.value,
        itemTitle.value,
        itemComment.value,
        searchWord.value
    )
}

@JvmName("toDomainModelStandard")
internal fun DiaryListItemContainerUi.Standard.toDomainModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        DiaryId(id),
        date,
        DiaryTitle(title),
        imageFileName?.let { DiaryImageFileName(it) }
    )
}

@JvmName("toDomainModelWordSearchResult")
internal fun DiaryListItemContainerUi.WordSearchResult.toDomainModel(): DiaryDayListItem.WordSearchResult {
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
