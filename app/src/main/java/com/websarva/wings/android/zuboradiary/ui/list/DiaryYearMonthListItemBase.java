package com.websarva.wings.android.zuboradiary.ui.list;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.ViewType;

import java.time.YearMonth;
import java.util.Objects;

public abstract class DiaryYearMonthListItemBase {
    private final YearMonth yearMonth;
    private final ViewType viewType;

    public DiaryYearMonthListItemBase(ViewType viewType) {
        Objects.requireNonNull(viewType);

        yearMonth = YearMonth.now();
        this.viewType = viewType;
    }

    public DiaryYearMonthListItemBase(YearMonth yearMonth, ViewType viewType) {
        Objects.requireNonNull(yearMonth);
        Objects.requireNonNull(viewType);

        this.yearMonth = yearMonth;
        this.viewType = viewType;
    }

    public boolean isDiaryViewType() {
        return viewType.equals(ViewType.DIARY);
    }

    public boolean isProgressIndicatorViewType() {
        return viewType.equals(ViewType.PROGRESS_INDICATOR);
    }

    public boolean isNoDiaryMessageViewType() {
        return viewType.equals(ViewType.NO_DIARY_MESSAGE);
    }

    @NonNull
    public YearMonth getYearMonth() {
        return this.yearMonth;
    }

    public ViewType getViewType() {
        return viewType;
    }
}
