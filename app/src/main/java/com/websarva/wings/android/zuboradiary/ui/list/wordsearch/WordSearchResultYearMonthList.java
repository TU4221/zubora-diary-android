package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.ViewType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WordSearchResultYearMonthList {

    private final List<WordSearchResultYearMonthListItem> wordSearchResultYearMonthListItemList;

    WordSearchResultYearMonthList(WordSearchResultDayList wordSearchResultDayList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(wordSearchResultDayList);
        if (wordSearchResultDayList.getWordSearchResultDayListItemList().isEmpty()) throw new IllegalArgumentException();

        List<WordSearchResultYearMonthListItem> WordSearchResultYearMonthListItemList = createWordSearchResultYearMonthListItem(wordSearchResultDayList);
        addLastItem(WordSearchResultYearMonthListItemList, needsNoDiaryMessage);
        this.wordSearchResultYearMonthListItemList = Collections.unmodifiableList(WordSearchResultYearMonthListItemList);
    }

    WordSearchResultYearMonthList(List<WordSearchResultYearMonthListItem> itemList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(itemList);
        if (itemList.isEmpty()) throw new IllegalArgumentException();
        itemList.stream().forEach(Objects::requireNonNull);

        List<WordSearchResultYearMonthListItem> _itemList = new ArrayList<>(itemList);
        addLastItem(_itemList, needsNoDiaryMessage);
        this.wordSearchResultYearMonthListItemList = Collections.unmodifiableList(_itemList);
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br>
     * false:ProgressIndicatorのみのリスト作成
     * */
    WordSearchResultYearMonthList(boolean needsNoDiaryMessage) {
        List<WordSearchResultYearMonthListItem> itemList = new ArrayList<>();
        addLastItem(itemList, needsNoDiaryMessage);
        this.wordSearchResultYearMonthListItemList = Collections.unmodifiableList(itemList);
    }

    WordSearchResultYearMonthList() {
        List<WordSearchResultYearMonthListItem> itemList = new ArrayList<>();
        this.wordSearchResultYearMonthListItemList = Collections.unmodifiableList(itemList);
    }

    @NonNull
    private List<WordSearchResultYearMonthListItem> createWordSearchResultYearMonthListItem(WordSearchResultDayList wordSearchResultDayList) {
        Objects.requireNonNull(wordSearchResultDayList);
        if (wordSearchResultDayList.getWordSearchResultDayListItemList().isEmpty()) throw new IllegalArgumentException();

        List<WordSearchResultDayListItem> sortingDayItemList= new ArrayList<>();
        List<WordSearchResultYearMonthListItem> resultYearMonthListItemList = new ArrayList<>();
        WordSearchResultYearMonthListItem resultYearMonthListItem;
        YearMonth sortingYearMonth = null;

        List<WordSearchResultDayListItem> resultDayListItemList = wordSearchResultDayList.getWordSearchResultDayListItemList();
        for (WordSearchResultDayListItem day: resultDayListItemList) {
            LocalDate date = day.getDate();
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());

            if (sortingYearMonth != null && !yearMonth.equals(sortingYearMonth)) {
                WordSearchResultDayList sortedWordSearchResultDayList = new WordSearchResultDayList(sortingDayItemList);
                resultYearMonthListItem =
                        new WordSearchResultYearMonthListItem(sortingYearMonth, sortedWordSearchResultDayList);
                resultYearMonthListItemList.add(resultYearMonthListItem);
                sortingDayItemList= new ArrayList<>();
            }
            sortingDayItemList.add(day);
            sortingYearMonth = yearMonth;
        }

        WordSearchResultDayList sortedWordSearchResultDayList = new WordSearchResultDayList(sortingDayItemList);
        resultYearMonthListItem =
                new WordSearchResultYearMonthListItem(sortingYearMonth, sortedWordSearchResultDayList);
        resultYearMonthListItemList.add(resultYearMonthListItem);
        return resultYearMonthListItemList;
    }

    private void addLastItem(List<WordSearchResultYearMonthListItem> itemList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.removeIf(x -> !x.isDiaryViewType());
        if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(itemList);
        } else {
            addLastItemProgressIndicator(itemList);
        }
    }

    private void addLastItemProgressIndicator(List<WordSearchResultYearMonthListItem> itemList) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.add(new WordSearchResultYearMonthListItem(ViewType.PROGRESS_INDICATOR));
    }

    private void addLastItemNoDiaryMessage(List<WordSearchResultYearMonthListItem> itemList) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.add(new WordSearchResultYearMonthListItem(ViewType.NO_DIARY_MESSAGE));
    }

    public int countDiaries() {
        int count = 0;
        for (WordSearchResultYearMonthListItem item: wordSearchResultYearMonthListItemList) {
            if (item.getViewType().equals(ViewType.DIARY)) {
                count += item.getWordSearchResultDayList().countDiaries();
            }
        }
        return count;
    }

    WordSearchResultYearMonthList combineDiaryLists(
            WordSearchResultYearMonthList additionList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(additionList);
        if (additionList.getWordSearchResultYearMonthListItemList().isEmpty()) throw new IllegalArgumentException();

        List<WordSearchResultYearMonthListItem> originalItemList = new ArrayList<>(this.wordSearchResultYearMonthListItemList);
        List<WordSearchResultYearMonthListItem> additionItemList = new ArrayList<>(additionList.wordSearchResultYearMonthListItemList);

        // List最終アイテム(日記以外
        originalItemList.removeIf(x -> !x.isDiaryViewType());
        additionItemList.removeIf(x -> !x.isDiaryViewType());

        // 元リスト最終アイテムの年月取得
        int originalListLastItemPosition = originalItemList.size() - 1;
        WordSearchResultYearMonthListItem originalListLastItem = originalItemList.get(originalListLastItemPosition);
        YearMonth originalListLastItemYearMonth = originalListLastItem.getYearMonth();

        // 追加リスト先頭アイテムの年月取得
        WordSearchResultYearMonthListItem additionListFirstItem = additionList.getWordSearchResultYearMonthListItemList().get(0);
        YearMonth additionListFirstItemYearMonth = additionListFirstItem.getYearMonth();

        // 元リストに追加リストの年月が含まれていたらアイテムを足し込む
        if (originalListLastItemYearMonth.equals(additionListFirstItemYearMonth)) {
            WordSearchResultDayList originalLastWordSearchResultDayList =
                    originalItemList.get(originalListLastItemPosition).getWordSearchResultDayList();
            WordSearchResultDayList additionWordSearchResultDayList = additionListFirstItem.getWordSearchResultDayList();
            WordSearchResultDayList combinedWordSearchResultDayList =
                    originalLastWordSearchResultDayList.combineDiaryDayLists(additionWordSearchResultDayList);
            WordSearchResultYearMonthListItem combinedResultYearMonthListItem
                    = new WordSearchResultYearMonthListItem(originalListLastItemYearMonth, combinedWordSearchResultDayList);
            originalItemList.remove(originalListLastItemPosition);
            originalItemList.add(combinedResultYearMonthListItem);
            additionItemList.remove(0);
        }

        List<WordSearchResultYearMonthListItem> resultItemList = new ArrayList<>(originalItemList);
        resultItemList.addAll(additionItemList);

        return new WordSearchResultYearMonthList(resultItemList, needsNoDiaryMessage);
    }

    public List<WordSearchResultYearMonthListItem> getWordSearchResultYearMonthListItemList() {
        return wordSearchResultYearMonthListItemList;
    }
}
