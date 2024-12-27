package com.websarva.wings.android.zuboradiary.ui.list;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryListBaseItem;

import java.time.LocalDate;
import java.util.Objects;

public abstract class DiaryDayListBaseItem {
    private final LocalDate date;

    public DiaryDayListBaseItem(DiaryListBaseItem listItem) {
        Objects.requireNonNull(listItem);

        String dateString = listItem.getDate();
        this.date = LocalDate.parse(dateString);
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

}
