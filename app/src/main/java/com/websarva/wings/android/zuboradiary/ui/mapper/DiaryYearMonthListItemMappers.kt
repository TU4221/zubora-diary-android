package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>.toUiModel(
    processFileNameToPath: suspend (DiaryImageFileName?) -> FilePathUi?
): DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard> {
    return DiaryYearMonthListItemUi.Diary(
        yearMonth,
        diaryDayList.toUiModel(processFileNameToPath)
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult>.toUiModel(
): DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.WordSearchResult> {
    return DiaryYearMonthListItemUi.Diary(
        yearMonth,
        diaryDayList.toUiModel()
    )
}

@JvmName("toUiModelStandard")
internal fun DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard>.toDomainModel(
): DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard> {
    return DiaryYearMonthListItem.Diary(
        yearMonth,
        diaryDayList.toDomainModel()
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.WordSearchResult>.toDomainModel(
): DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult> {
    return DiaryYearMonthListItem.Diary(
        yearMonth,
        diaryDayList.toDomainModel()
    )
}
