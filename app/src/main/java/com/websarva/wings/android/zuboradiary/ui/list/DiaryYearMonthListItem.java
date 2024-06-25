package com.websarva.wings.android.zuboradiary.ui.list;

import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiaryYearMonthListItem {
    private final String id = UUID.randomUUID().toString();
    private int year;
    private int month;
    private List<DiaryDayListItem> diaryDayListItemList = new ArrayList<>();
    private int viewType;

    public DiaryYearMonthListItem() {
    }

    public DiaryYearMonthListItem(
            int year, int month, List<DiaryDayListItem> diaryDayListItemList, int viewType) {
        this.year = year;
        this.month = month;
        this.diaryDayListItemList = diaryDayListItemList;
        this.viewType = viewType;
    }

    public DiaryYearMonthListItem(DiaryYearMonthListItem diaryYearMonthListItem) {
        this.year = diaryYearMonthListItem.year;
        this.month = diaryYearMonthListItem.month;
        this.diaryDayListItemList = diaryYearMonthListItem.diaryDayListItemList;
        this.viewType = diaryYearMonthListItem.viewType;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryYearMonthListItem clone() {
        DiaryYearMonthListItem clone = null;
        try {
            clone = (DiaryYearMonthListItem) super.clone();
        } catch (CloneNotSupportedException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                throw new InternalError(e);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        return clone;
    }

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
