package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter.ViewType;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListItemBase;

import java.time.YearMonth;
import java.util.Objects;

public class DiaryYearMonthListItem extends DiaryYearMonthListItemBase {
    private final DiaryDayList diaryDayList;

    public DiaryYearMonthListItem(ViewType viewType) {
        super(viewType);
        diaryDayList = new DiaryDayList();
    }

    public DiaryYearMonthListItem(
            YearMonth yearMonth, DiaryDayList diaryDayList) {
        super(yearMonth, ViewType.DIARY);

        Objects.requireNonNull(diaryDayList);
        if (diaryDayList.getDiaryDayListItemList().isEmpty()) throw new IllegalArgumentException();

        this.diaryDayList = diaryDayList;
    }

    public DiaryDayList getDiaryDayList() {
        return diaryDayList;
    }
}
