package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.ViewType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiaryYearMonthList {

    private final List<DiaryYearMonthListItem> diaryYearMonthListItemList;

    DiaryYearMonthList(DiaryDayList diaryDayList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(diaryDayList);
        if (diaryDayList.getDiaryDayListItemList().isEmpty()) throw new IllegalArgumentException();

        List<DiaryYearMonthListItem> diaryYearMonthListItemList = createDiaryYearMonthListItem(diaryDayList);
        addLastItem(diaryYearMonthListItemList, needsNoDiaryMessage);
        this.diaryYearMonthListItemList = Collections.unmodifiableList(diaryYearMonthListItemList);
    }

    DiaryYearMonthList(List<DiaryYearMonthListItem> itemList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(itemList);
        if (itemList.isEmpty()) throw new IllegalArgumentException();
        itemList.stream().forEach(Objects::requireNonNull);

        List<DiaryYearMonthListItem> _itemList = new ArrayList<>(itemList);
        addLastItem(_itemList, needsNoDiaryMessage);
        this.diaryYearMonthListItemList = Collections.unmodifiableList(_itemList);
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br>
     * false:ProgressIndicatorのみのリスト作成
     * */
    DiaryYearMonthList(boolean needsNoDiaryMessage) {
        List<DiaryYearMonthListItem> itemList = new ArrayList<>();
        addLastItem(itemList, needsNoDiaryMessage);
        this.diaryYearMonthListItemList = Collections.unmodifiableList(itemList);
    }

    DiaryYearMonthList() {
        List<DiaryYearMonthListItem> itemList = new ArrayList<>();
        this.diaryYearMonthListItemList = Collections.unmodifiableList(itemList);
    }

    @NonNull
    private List<DiaryYearMonthListItem> createDiaryYearMonthListItem(DiaryDayList diaryDayList) {
        Objects.requireNonNull(diaryDayList);
        if (diaryDayList.getDiaryDayListItemList().isEmpty()) throw new IllegalArgumentException();

        List<DiaryDayListItem> sortingDayItemList= new ArrayList<>();
        List<DiaryYearMonthListItem> diaryYearMonthListItemList = new ArrayList<>();
        DiaryYearMonthListItem diaryYearMonthListItem;
        YearMonth sortingYearMonth = null;

        List<DiaryDayListItem> diaryDayListItemList = diaryDayList.getDiaryDayListItemList();
        for (DiaryDayListItem day: diaryDayListItemList) {
            LocalDate date = day.getDate();
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());

            if (sortingYearMonth != null && !yearMonth.equals(sortingYearMonth)) {
                DiaryDayList sortedDiaryDayList = new DiaryDayList(sortingDayItemList);
                diaryYearMonthListItem =
                        new DiaryYearMonthListItem(sortingYearMonth, sortedDiaryDayList);
                diaryYearMonthListItemList.add(diaryYearMonthListItem);
                sortingDayItemList= new ArrayList<>();
            }
            sortingDayItemList.add(day);
            sortingYearMonth = yearMonth;
        }

        DiaryDayList sortedDiaryDayList = new DiaryDayList(sortingDayItemList);
        diaryYearMonthListItem =
                new DiaryYearMonthListItem(sortingYearMonth, sortedDiaryDayList);
        diaryYearMonthListItemList.add(diaryYearMonthListItem);
        return diaryYearMonthListItemList;
    }

    private void addLastItem(List<DiaryYearMonthListItem> itemList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.removeIf(x -> x.isNotDiaryViewType());
        if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(itemList);
        } else {
            addLastItemProgressIndicator(itemList);
        }
    }

    private void addLastItemProgressIndicator(List<DiaryYearMonthListItem> itemList) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.add(new DiaryYearMonthListItem(ViewType.PROGRESS_INDICATOR));
    }

    private void addLastItemNoDiaryMessage(List<DiaryYearMonthListItem> itemList) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        itemList.add(new DiaryYearMonthListItem(ViewType.NO_DIARY_MESSAGE));
    }

    int countDiaries() {
        int count = 0;
        for (DiaryYearMonthListItem item: diaryYearMonthListItemList) {
            if (item.getViewType().equals(ViewType.DIARY)) {
                count += item.getDiaryDayList().countDiaries();
            }
        }
        return count;
    }

    DiaryYearMonthList combineDiaryLists(
            DiaryYearMonthList additionList, boolean needsNoDiaryMessage) {
        Objects.requireNonNull(additionList);
        if (additionList.getDiaryYearMonthListItemList().isEmpty()) throw new IllegalArgumentException();

        List<DiaryYearMonthListItem> originalItemList = new ArrayList<>(this.diaryYearMonthListItemList);
        List<DiaryYearMonthListItem> additionItemList = new ArrayList<>(additionList.diaryYearMonthListItemList);

        // List最終アイテム(日記以外
        originalItemList.removeIf(x -> x.isNotDiaryViewType());
        additionItemList.removeIf(x -> x.isNotDiaryViewType());

        // 元リスト最終アイテムの年月取得
        int originalListLastItemPosition = originalItemList.size() - 1;
        DiaryYearMonthListItem originalListLastItem = originalItemList.get(originalListLastItemPosition);
        YearMonth originalListLastItemYearMonth = originalListLastItem.getYearMonth();

        // 追加リスト先頭アイテムの年月取得
        DiaryYearMonthListItem additionListFirstItem = additionList.getDiaryYearMonthListItemList().get(0);
        YearMonth additionListFirstItemYearMonth = additionListFirstItem.getYearMonth();

        // 元リストに追加リストの年月が含まれていたらアイテムを足し込む
        if (originalListLastItemYearMonth.equals(additionListFirstItemYearMonth)) {
            DiaryDayList originalLastDiaryDayList =
                    originalItemList.get(originalListLastItemPosition).getDiaryDayList();
            DiaryDayList additionDiaryDayList = additionListFirstItem.getDiaryDayList();
            DiaryDayList combinedDiaryDayList =
                    originalLastDiaryDayList.combineDiaryDayLists(additionDiaryDayList);
            DiaryYearMonthListItem combinedDiaryYearMonthListItem
                    = new DiaryYearMonthListItem(originalListLastItemYearMonth, combinedDiaryDayList);
            originalItemList.remove(originalListLastItemPosition);
            originalItemList.add(combinedDiaryYearMonthListItem);
            additionItemList.remove(0);
        }

        List<DiaryYearMonthListItem> resultItemList = new ArrayList<>(originalItemList);
        resultItemList.addAll(additionItemList);

        return new DiaryYearMonthList(resultItemList, needsNoDiaryMessage);
    }

    public List<DiaryYearMonthListItem> getDiaryYearMonthListItemList() {
        return diaryYearMonthListItemList;
    }
}
