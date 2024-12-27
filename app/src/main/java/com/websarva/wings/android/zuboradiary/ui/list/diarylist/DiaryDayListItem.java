package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;

public class DiaryDayListItem extends DiaryDayListBaseItem {

    private final String title;
    private final String picturePath;

    DiaryDayListItem(DiaryListItem listItem) {
        super(listItem);

        this.title = listItem.getTitle();
        this.picturePath = listItem.getPicturePath();
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
