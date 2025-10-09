package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.ImageFilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryDayList<DiaryDayListItem.Standard>.toUiModel(
    processFileNameToPath: suspend (DiaryImageFileName?) -> ImageFilePathUi?
): DiaryDayListUi<DiaryDayListItemUi.Standard> {
    return DiaryDayListUi(
        itemList.map { it.toUiModel(processFileNameToPath) }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayList<DiaryDayListItem.WordSearchResult>.toUiModel(

): DiaryDayListUi<DiaryDayListItemUi.WordSearchResult> {
    return DiaryDayListUi(
        itemList.map { it.toUiModel() }
    )
}

@JvmName("toUiModelStandard")
internal fun DiaryDayListUi<DiaryDayListItemUi.Standard>.toDomainModel(

): DiaryDayList<DiaryDayListItem.Standard> {
    return DiaryDayList(
        itemList.map { it.toDomainModel() }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryDayListUi<DiaryDayListItemUi.WordSearchResult>.toDomainModel(

): DiaryDayList<DiaryDayListItem.WordSearchResult> {
    return DiaryDayList(
        itemList.map { it.toDomainModel() }
    )
}
