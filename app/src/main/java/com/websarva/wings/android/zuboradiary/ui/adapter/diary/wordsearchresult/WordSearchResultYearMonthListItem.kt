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
        require(wordSearchResultDayList.itemList.isNotEmpty())

        this.wordSearchResultDayList = wordSearchResultDayList
    }

    override fun areContentsTheSame(item: DiaryYearMonthListBaseItem): Boolean {
        if (this === item) return true
        if (item !is WordSearchResultYearMonthListItem) return false

        return wordSearchResultDayList == item.wordSearchResultDayList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordSearchResultYearMonthListItem) return false
        if (!super.equals(other)) return false

        return wordSearchResultDayList == other.wordSearchResultDayList
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + wordSearchResultDayList.hashCode()
        return result
    }
}
