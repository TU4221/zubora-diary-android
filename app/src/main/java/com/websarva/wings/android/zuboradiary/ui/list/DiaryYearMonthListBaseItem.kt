package com.websarva.wings.android.zuboradiary.ui.list;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.ViewType;

import java.time.YearMonth;
import java.util.Objects;

public abstract class DiaryYearMonthListBaseItem {
    private final YearMonth yearMonth;
    private final ViewType viewType;

    public DiaryYearMonthListBaseItem(ViewType viewType) {
        Objects.requireNonNull(viewType);

        yearMonth = YearMonth.now();
        this.viewType = viewType;
    }

    public DiaryYearMonthListBaseItem(YearMonth yearMonth, ViewType viewType) {
        Objects.requireNonNull(yearMonth);
        Objects.requireNonNull(viewType);

        this.yearMonth = yearMonth;
        this.viewType = viewType;
    }

    public final boolean isNotDiaryViewType() {
        return !viewType.equals(ViewType.DIARY);
    }

    @NonNull
    public final YearMonth getYearMonth() {
        return this.yearMonth;
    }

    public final ViewType getViewType() {
        return viewType;
    }
}
