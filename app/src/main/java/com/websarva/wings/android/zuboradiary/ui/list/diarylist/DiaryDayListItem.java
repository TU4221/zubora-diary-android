package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem;

public class DiaryDayListItem extends DiaryDayListBaseItem {

    private final String title;
    private final Uri picturePath;

    DiaryDayListItem(DiaryListItem listItem) {
        super(listItem);

        this.title = listItem.getTitle();
        String picturePath = listItem.getPicturePath();
        if (picturePath.isEmpty()) {
            this.picturePath = null;
        } else {
            this.picturePath = Uri.parse(picturePath);
        }
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @Nullable
    public Uri getPicturePath() {
        return picturePath;
    }

}
