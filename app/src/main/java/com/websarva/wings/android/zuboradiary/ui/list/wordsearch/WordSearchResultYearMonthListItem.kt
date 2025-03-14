package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem
import java.time.YearMonth

internal class WordSearchResultYearMonthListItem : DiaryYearMonthListBaseItem {
    val wordSearchResultDayList: WordSearchResultDayList

    constructor(viewType: DiaryYearMonthListBaseAdapter.ViewType) : super(viewType) {
        this.wordSearchResultDayList = WordSearchResultDayList()
    }

    constructor(
        yearMonth: YearMonth,
        wordSearchResultDayList: WordSearchResultDayList
    ) : super(yearMonth, DiaryYearMonthListBaseAdapter.ViewType.DIARY) {
        require(wordSearchResultDayList.wordSearchResultDayListItemList.isNotEmpty())

        this.wordSearchResultDayList = wordSearchResultDayList
    }
}
