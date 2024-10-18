package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem;

import java.time.LocalDate;
import java.util.Objects;

public class DiaryDayListItem {
    private final LocalDate date;
    private final String title;
    private final String picturePath;

    DiaryDayListItem(DiaryListItem diaryListItem) {
        Objects.requireNonNull(diaryListItem);

        String strDate = diaryListItem.getDate();
        this.date = LocalDate.parse(strDate);
        this.title = diaryListItem.getTitle();
        this.picturePath = diaryListItem.getPicturePath();
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getPicturePath() {
        return picturePath;
    }

}
