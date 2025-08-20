package com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult

import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListBaseItem
import java.time.YearMonth

internal class WordSearchResultYearMonthListItem : DiaryYearMonthListBaseItem {

    override val diaryDayList: WordSearchResultDayList

    constructor(viewType: DiaryYearMonthListBaseAdapter.ViewType) : super(viewType) {
        this.diaryDayList = WordSearchResultDayList()
    }

    constructor(
        yearMonth: YearMonth,
        wordSearchResultDayList: WordSearchResultDayList
    ) : super(yearMonth, DiaryYearMonthListBaseAdapter.ViewType.DIARY) {
        require(wordSearchResultDayList.isNotEmpty)

        this.diaryDayList = wordSearchResultDayList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordSearchResultYearMonthListItem) return false
        if (!super.equals(other)) return false

        return diaryDayList == other.diaryDayList
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + diaryDayList.hashCode()
        return result
    }
}
