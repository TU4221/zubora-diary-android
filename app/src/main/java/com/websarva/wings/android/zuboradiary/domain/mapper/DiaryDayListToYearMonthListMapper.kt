package com.websarva.wings.android.zuboradiary.domain.mapper

import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthListItem
import java.time.YearMonth

@JvmName("toDiaryYearMonthListItemListStandard")
internal fun DiaryDayList<DiaryDayListItem.Standard>.toDiaryYearMonthList(
): DiaryYearMonthList<DiaryDayListItem.Standard>{
    return DiaryYearMonthList(
        createDiaryYearMonthListItemList(this)
                + DiaryYearMonthListItem.ProgressIndicator()
    )
}

@JvmName("toDiaryYearMonthListItemListWordSearchResult")
internal fun DiaryDayList<DiaryDayListItem.WordSearchResult>.toDiaryYearMonthList(
): DiaryYearMonthList<DiaryDayListItem.WordSearchResult>{
    return DiaryYearMonthList(
        createDiaryYearMonthListItemList(this)
                + DiaryYearMonthListItem.ProgressIndicator()
    )
}

private fun <T: DiaryDayListItem> createDiaryYearMonthListItemList(
    diaryDayList: DiaryDayList<T>
): List<DiaryYearMonthListItem<T>> {
    require(diaryDayList.isNotEmpty)

    var sortingDayItemList: MutableList<T> = ArrayList()
    val diaryYearMonthListItemList: MutableList<DiaryYearMonthListItem<T>> = ArrayList()
    var diaryYearMonthListItem: DiaryYearMonthListItem.Diary<*>
    var sortingYearMonth: YearMonth? = null

    val diaryDayListItemList = diaryDayList.itemList
    for (day in diaryDayListItemList) {
        val date = day.date
        val yearMonth = YearMonth.of(date.year, date.month)

        if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
            val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
            diaryYearMonthListItem =
                DiaryYearMonthListItem.Diary(sortingYearMonth, sortedDiaryDayList)
            diaryYearMonthListItemList.add(diaryYearMonthListItem)
            sortingDayItemList = ArrayList()
        }
        sortingDayItemList.add(day)
        sortingYearMonth = yearMonth
    }

    val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
    diaryYearMonthListItem =
        DiaryYearMonthListItem.Diary(checkNotNull(sortingYearMonth) , sortedDiaryDayList)
    diaryYearMonthListItemList.add(diaryYearMonthListItem)
    return diaryYearMonthListItemList
}
