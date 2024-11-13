package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;

public class DiaryListItem {

    @NonNull
    private String date = "";
    @NonNull
    private String title = "";
    @NonNull
    private String picturePath = "";

    @NonNull
    public String getDate() {
        return this.date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

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
