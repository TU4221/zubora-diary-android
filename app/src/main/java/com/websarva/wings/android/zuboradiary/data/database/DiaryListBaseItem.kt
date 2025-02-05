package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;

public class DiaryListBaseItem {

    @NonNull
    private String date = "";

    @NonNull
    public String getDate() {
        return this.date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

}
