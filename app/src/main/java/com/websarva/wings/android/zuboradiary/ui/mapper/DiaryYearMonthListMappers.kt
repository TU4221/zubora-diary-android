package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi

@JvmName("toUiModelStandard")
internal suspend fun DiaryYearMonthList<DiaryDayListItem.Standard>.toUiModel(
    processFileNameToPath: suspend (DiaryImageFileName?) -> FilePathUi?
): DiaryYearMonthListUi<DiaryDayListItemUi.Standard> {
    return DiaryYearMonthListUi(
        itemList.map {
            when (it) {
                is DiaryYearMonthListItem.Diary -> it.toUiModel(processFileNameToPath)
                is DiaryYearMonthListItem.NoDiaryMessage -> DiaryYearMonthListItemUi.NoDiaryMessage()
                is DiaryYearMonthListItem.ProgressIndicator -> DiaryYearMonthListItemUi.ProgressIndicator()
            }
        }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryYearMonthList<DiaryDayListItem.WordSearchResult>.toUiModel(
): DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult> {
    return DiaryYearMonthListUi(
        itemList.map {
            when (it) {
                is DiaryYearMonthListItem.Diary -> it.toUiModel()
                is DiaryYearMonthListItem.NoDiaryMessage -> DiaryYearMonthListItemUi.NoDiaryMessage()
                is DiaryYearMonthListItem.ProgressIndicator -> DiaryYearMonthListItemUi.ProgressIndicator()
            }
        }
    )
}

@JvmName("toUiModelStandard")
internal fun DiaryYearMonthListUi<DiaryDayListItemUi.Standard>.toDomainModel(
): DiaryYearMonthList<DiaryDayListItem.Standard> {
    return DiaryYearMonthList(
        itemList.map {
            when (it) {
                is DiaryYearMonthListItemUi.Diary -> it.toDomainModel()
                is DiaryYearMonthListItemUi.NoDiaryMessage -> DiaryYearMonthListItem.NoDiaryMessage()
                is DiaryYearMonthListItemUi.ProgressIndicator -> DiaryYearMonthListItem.ProgressIndicator()
            }
        }
    )
}

@JvmName("toUiModelWordSearchResult")
internal fun DiaryYearMonthListUi<DiaryDayListItemUi.WordSearchResult>.toDomainModel(
): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
    return DiaryYearMonthList(
        itemList.map {
            when (it) {
                is DiaryYearMonthListItemUi.Diary -> it.toDomainModel()
                is DiaryYearMonthListItemUi.NoDiaryMessage -> DiaryYearMonthListItem.NoDiaryMessage()
                is DiaryYearMonthListItemUi.ProgressIndicator -> DiaryYearMonthListItem.ProgressIndicator()
            }
        }
    )
}
