package com.websarva.wings.android.zuboradiary.ui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiaryYearMonthListItem {
    private final String id = UUID.randomUUID().toString();
    private int year;
    private int month;
    private List<DiaryDayListItem> diaryDayListItemList = new ArrayList<>();
    private int viewType;

    public String getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public List<DiaryDayListItem> getDiaryDayListItemList() {
        return diaryDayListItemList;
    }

    public void setDiaryDayListItemList(List<DiaryDayListItem> diaryDayListItemList) {
        this.diaryDayListItemList = diaryDayListItemList;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
