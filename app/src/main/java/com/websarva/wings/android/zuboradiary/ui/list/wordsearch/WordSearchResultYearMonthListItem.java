package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.ViewType;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;

import java.time.YearMonth;
import java.util.Objects;

public class WordSearchResultYearMonthListItem extends DiaryYearMonthListBaseItem {
    private final WordSearchResultDayList wordSearchResultDayList;

    WordSearchResultYearMonthListItem(ViewType viewType) {
        super(viewType);
        wordSearchResultDayList = new WordSearchResultDayList();
    }

    WordSearchResultYearMonthListItem(
            YearMonth yearMonth, WordSearchResultDayList wordSearchResultDayList) {
        super(yearMonth, ViewType.DIARY);

        Objects.requireNonNull(wordSearchResultDayList);
        if (wordSearchResultDayList.getWordSearchResultDayListItemList().isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.wordSearchResultDayList = wordSearchResultDayList;
    }

    public WordSearchResultDayList getWordSearchResultDayList() {
        return wordSearchResultDayList;
    }
}
