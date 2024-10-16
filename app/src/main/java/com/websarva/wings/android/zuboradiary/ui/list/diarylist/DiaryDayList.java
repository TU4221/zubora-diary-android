package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiaryDayList {

    private final List<DiaryDayListItem> diaryDayListItemList;

    public DiaryDayList(List<DiaryDayListItem> itemList) {
        Objects.requireNonNull(itemList);
        if (itemList.isEmpty()) throw new IllegalArgumentException();
        itemList.stream().forEach(Objects::requireNonNull);

        this.diaryDayListItemList = Collections.unmodifiableList(itemList);
    }

    public DiaryDayList() {
        this.diaryDayListItemList = new ArrayList<>();
    }

    public int countDiaries() {
        return diaryDayListItemList.size();
    }

    public DiaryDayList combineDiaryDayLists(DiaryDayList additionList) {
        Objects.requireNonNull(additionList);
        if (additionList.diaryDayListItemList.isEmpty()) throw new IllegalArgumentException();

        List<DiaryDayListItem> resultItemList = new ArrayList<>();
        resultItemList.addAll(this.diaryDayListItemList);
        resultItemList.addAll(additionList.diaryDayListItemList);

        return new DiaryDayList(resultItemList);
    }

    public List<DiaryDayListItem> getDiaryDayListItemList() {
        return diaryDayListItemList;
    }
}
