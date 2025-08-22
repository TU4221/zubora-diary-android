package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi

@JvmName("toUiModelStandard")
internal fun DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>.toUiModel(
): DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard> {
    return DiaryYearMonthListItemUi.Diary(
        yearMonth,
        diaryDayList.toUiModel()
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
