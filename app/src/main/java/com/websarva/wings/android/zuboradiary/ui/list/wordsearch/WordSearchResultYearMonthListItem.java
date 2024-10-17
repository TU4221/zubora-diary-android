package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.ViewType;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;

import java.time.YearMonth;
import java.util.Objects;

public class WordSearchResultYearMonthListItem extends DiaryYearMonthListItemBase {
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
