package com.websarva.wings.android.zuboradiary.ui;

import android.os.Build;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;
import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryYearMonthListItem;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DiaryYearMonthListItemBase implements Cloneable {
    private final String id = UUID.randomUUID().toString();
    private YearMonth yearMonth;
    private int viewType;

    public DiaryYearMonthListItemBase(int viewType) {
        yearMonth = null;
        this.viewType = viewType;
    }

    public DiaryYearMonthListItemBase(@NonNull YearMonth yearMonth, int viewType) {
        this.yearMonth = yearMonth;
        this.viewType = viewType;
    }

    // Object#clone例外処理:https://yujisoftware.hatenablog.com/entry/CloneNotSupportedException
    @NonNull
    @Override
    public DiaryYearMonthListItemBase clone() {
        DiaryYearMonthListItemBase clone = null;
        try {
            clone = (DiaryYearMonthListItemBase) super.clone();
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

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
