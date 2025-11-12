package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi

@JvmName("toUiModelStandardTest")
internal suspend fun DiaryYearMonthList<DiaryDayListItem.Standard>.toUiModel(
    processBuildPathFromFileName: suspend (DiaryImageFileName?) -> FilePathUi?
): DiaryListUi<DiaryListItemContainerUi.Standard> {
    return itemList.flatMap { yearMonthListItem ->
        when (yearMonthListItem) {
            is DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard> -> {
                val header =
                    DiaryListItemUi.Header<DiaryListItemContainerUi.Standard>(
                        yearMonthListItem.yearMonth
                    )
                val diaryList = yearMonthListItem.diaryDayList.itemList.map { dayListItem ->
                    DiaryListItemUi.Diary(
                        dayListItem.toUiModel(processBuildPathFromFileName)
                    )
                }
                listOf(header) + diaryList
            }
            is DiaryYearMonthListItem.NoDiaryMessage -> {
                val noDiaryMessage =
                    DiaryListItemUi.NoDiaryMessage<DiaryListItemContainerUi.Standard>()
                listOf(noDiaryMessage)
            }
            is DiaryYearMonthListItem.ProgressIndicator -> {
                val progressIndicator =
                    DiaryListItemUi.ProgressIndicator<DiaryListItemContainerUi.Standard>()
                listOf(progressIndicator)
            }
        }
    }.let { DiaryListUi(it) }
}

@JvmName("toUiModelWordSearchResultTest")
internal fun DiaryYearMonthList<DiaryDayListItem.WordSearchResult>.toUiModel(
): DiaryListUi<DiaryListItemContainerUi.WordSearchResult> {
    return itemList.flatMap { yearMonthListItem ->
        when (yearMonthListItem) {
            is DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult> -> {
                val header =
                    DiaryListItemUi.Header<DiaryListItemContainerUi.WordSearchResult>(
                        yearMonthListItem.yearMonth
                    )
                val diaryList = yearMonthListItem.diaryDayList.itemList.map { dayListItem ->
                    DiaryListItemUi.Diary(
                        dayListItem.toUiModel()
                    )
                }
                listOf(header) + diaryList
            }
            is DiaryYearMonthListItem.NoDiaryMessage -> {
                val noDiaryMessage =
                    DiaryListItemUi.NoDiaryMessage<DiaryListItemContainerUi.WordSearchResult>()
                listOf(noDiaryMessage)
            }
            is DiaryYearMonthListItem.ProgressIndicator -> {
                val progressIndicator =
                    DiaryListItemUi.ProgressIndicator<DiaryListItemContainerUi.WordSearchResult>()
                listOf(progressIndicator)
            }
        }
    }.let { DiaryListUi(it) }
}

@JvmName("toDomainModelStandard")
internal fun DiaryListUi<DiaryListItemContainerUi.Standard>.toDomainModel(
): DiaryYearMonthList<DiaryDayListItem.Standard> {
    return toDomainModel { it.toDomainModel() }
}

@JvmName("toDomainModelWordSearchResult")
internal fun DiaryListUi<DiaryListItemContainerUi.WordSearchResult>.toDomainModel(
): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
    return toDomainModel { it.toDomainModel() }
}

private fun <TContainerUi, TDomainItem> DiaryListUi<TContainerUi>.toDomainModel(
    containerUiToDomain: (TContainerUi) -> TDomainItem
): DiaryYearMonthList<TDomainItem> where TContainerUi : DiaryListItemContainerUi, TDomainItem : DiaryDayListItem {
    val groupedItems = groupItemsByHeader(itemList)

    val domainItemList = groupedItems.mapNotNull { group ->
        val firstItem = group.firstOrNull() ?: return@mapNotNull null
        when (firstItem) {
            is DiaryListItemUi.Header -> {
                val header = group.filterIsInstance<DiaryListItemUi.Header<TContainerUi>>().firstOrNull()
                    ?: return@mapNotNull null

                val diaryItems = group.filterIsInstance<DiaryListItemUi.Diary<TContainerUi>>()
                    .map { diaryUiItem ->
                        containerUiToDomain(diaryUiItem.containerUi)
                    }

                val domainDayList = DiaryDayList(diaryItems)
                DiaryYearMonthListItem.Diary(
                    yearMonth = header.yearMonth,
                    diaryDayList = domainDayList
                )
            }
            is DiaryListItemUi.NoDiaryMessage -> {
                DiaryYearMonthListItem.NoDiaryMessage()
            }
            is DiaryListItemUi.ProgressIndicator -> {
                DiaryYearMonthListItem.ProgressIndicator()
            }
            is DiaryListItemUi.Diary -> throw IllegalStateException()
        }

    }
    return DiaryYearMonthList(domainItemList)
}

private fun <T : DiaryListItemContainerUi> groupItemsByHeader(
    flatList: List<DiaryListItemUi<T>>
): List<List<DiaryListItemUi<T>>> {
    val groupedItems = mutableListOf<List<DiaryListItemUi<T>>>()
    var currentGroup = mutableListOf<DiaryListItemUi<T>>()

    flatList.forEach { item ->
        if (item !is DiaryListItemUi.Diary) {
            // 現在のグループが空でなければ、確定リストに追加
            if (currentGroup.isNotEmpty()) {
                groupedItems.add(currentGroup)
            }
            // 新しいグループを開始
            currentGroup = mutableListOf()
        }
        currentGroup.add(item)
    }
    // 最後のグループを確定リストに追加
    if (currentGroup.isNotEmpty()) {
        groupedItems.add(currentGroup)
    }

    return groupedItems
}
