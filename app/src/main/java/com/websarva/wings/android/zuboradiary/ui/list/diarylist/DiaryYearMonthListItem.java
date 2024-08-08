package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.os.Build;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.DiaryYearMonthListItemBase;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiaryYearMonthListItem extends DiaryYearMonthListItemBase {
    private List<DiaryDayListItem> diaryDayListItemList;

    public DiaryYearMonthListItem(int viewType) {
        super(viewType);
        diaryDayListItemList = new ArrayList<>();
    }

    public DiaryYearMonthListItem(
            @NonNull YearMonth yearMonth,
            @NonNull List<DiaryDayListItem> diaryDayListItemList, int viewType) {
        super(yearMonth, viewType);
        this.diaryDayListItemList = diaryDayListItemList;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryYearMonthListItem clone() {
        DiaryYearMonthListItem clone = (DiaryYearMonthListItem) super.clone();
        List<DiaryDayListItem> cloneDiaryDayList = new ArrayList<>();
        for (DiaryDayListItem item : diaryDayListItemList) {
            DiaryDayListItem cloneItem = item.clone();
            cloneDiaryDayList.add(cloneItem);
        }
        clone.diaryDayListItemList = cloneDiaryDayList;
        return clone;
    }

    public List<DiaryDayListItem> getDiaryDayListItemList() {
        return diaryDayListItemList;
    }

    public void setDiaryDayListItemList(List<DiaryDayListItem> diaryDayListItemList) {
        this.diaryDayListItemList = diaryDayListItemList;
    }
}
