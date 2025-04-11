package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
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
