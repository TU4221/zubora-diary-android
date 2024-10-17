package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WordSearchResultDayList {

    private final List<WordSearchResultDayListItem> wordSearchResultDayListItemList;

    WordSearchResultDayList(List<WordSearchResultDayListItem> itemList) {
        Objects.requireNonNull(itemList);
        if (itemList.isEmpty()) throw new IllegalArgumentException();
        itemList.stream().forEach(Objects::requireNonNull);

        this.wordSearchResultDayListItemList = Collections.unmodifiableList(itemList);
    }

    WordSearchResultDayList() {
        this.wordSearchResultDayListItemList = new ArrayList<>();
    }

    public int countDiaries() {
        return wordSearchResultDayListItemList.size();
    }

    WordSearchResultDayList combineDiaryDayLists(WordSearchResultDayList additionList) {
        Objects.requireNonNull(additionList);
        if (additionList.wordSearchResultDayListItemList.isEmpty()) throw new IllegalArgumentException();

        List<WordSearchResultDayListItem> resultItemList = new ArrayList<>();
        resultItemList.addAll(this.wordSearchResultDayListItemList);
        resultItemList.addAll(additionList.wordSearchResultDayListItemList);

        return new WordSearchResultDayList(resultItemList);
    }

    public List<WordSearchResultDayListItem> getWordSearchResultDayListItemList() {
        return wordSearchResultDayListItemList;
    }
}
