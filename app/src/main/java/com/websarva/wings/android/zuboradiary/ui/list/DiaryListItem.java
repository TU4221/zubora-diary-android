package com.websarva.wings.android.zuboradiary.ui.list;

import androidx.annotation.NonNull;

public class DiaryListItem {
    @NonNull
    private String date;
    @NonNull
    private String title;
    private String picturePath;

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
