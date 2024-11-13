package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary_item_title_selection_history")
public class DiaryItemTitleSelectionHistoryItemEntity {

    @PrimaryKey
    @NonNull
    private String title = "";
    @NonNull
    private String log = "";


    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getLog() {
        return log;
    }

    public void setLog(@NonNull String log) {
        this.log = log;
    }
}
