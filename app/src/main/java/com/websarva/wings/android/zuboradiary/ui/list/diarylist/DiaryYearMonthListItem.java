package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseAdapter.ViewType;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListBaseItem;

import java.time.YearMonth;
import java.util.Objects;

public class DiaryYearMonthListItem extends DiaryYearMonthListBaseItem {
    private final DiaryDayList diaryDayList;

    DiaryYearMonthListItem(ViewType viewType) {
        super(viewType);
        diaryDayList = new DiaryDayList();
    }

    DiaryYearMonthListItem(YearMonth yearMonth, DiaryDayList diaryDayList) {
        super(yearMonth, ViewType.DIARY);

        Objects.requireNonNull(diaryDayList);
        if (diaryDayList.getDiaryDayListItemList().isEmpty()) throw new IllegalArgumentException();

        this.diaryDayList = diaryDayList;
    }

    public DiaryDayList getDiaryDayList() {
        return diaryDayList;
    }
}
