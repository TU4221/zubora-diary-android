package com.websarva.wings.android.zuboradiary.ui.list;

import android.os.Build;

import androidx.annotation.NonNull;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiaryYearMonthListItem implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private YearMonth yearMonth;
    private List<DiaryDayListItem> diaryDayListItemList;
    private int viewType;

    public DiaryYearMonthListItem(int viewType) {
        yearMonth = null;
        diaryDayListItemList = new ArrayList<>();
        this.viewType = viewType;
    }

    public DiaryYearMonthListItem(
            @NonNull YearMonth yearMonth,
            @NonNull List<DiaryDayListItem> diaryDayListItemList, int viewType) {
        this.yearMonth = yearMonth;
        this.diaryDayListItemList = diaryDayListItemList;
        this.viewType = viewType;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryYearMonthListItem clone() {
        DiaryYearMonthListItem clone = null;
        try {
            clone = (DiaryYearMonthListItem) super.clone();
            List<DiaryDayListItem> cloneDiaryDayList = new ArrayList<>();
            for (DiaryDayListItem item : this.diaryDayListItemList) {
                DiaryDayListItem cloneItem = item.clone();
                cloneDiaryDayList.add(cloneItem);
            }
            clone.diaryDayListItemList = cloneDiaryDayList;
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

    public YearMonth getYearMonth() {
        return this.yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
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
