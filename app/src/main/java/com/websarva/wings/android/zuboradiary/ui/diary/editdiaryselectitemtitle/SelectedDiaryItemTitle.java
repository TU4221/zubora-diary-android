package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary_item_title_history")
public class SelectedDiaryItemTitle {
    @PrimaryKey
    @NonNull
    private String title = "";
    @NonNull
    private String log = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
