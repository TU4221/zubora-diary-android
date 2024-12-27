package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;

public class DiaryListItem extends DiaryListBaseItem {

    @NonNull
    private String title = "";
    @NonNull
    private String picturePath = "";

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(@NonNull String picturePath) {
        this.picturePath = picturePath;
    }
}
