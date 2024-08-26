package com.websarva.wings.android.zuboradiary.data.database;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.websarva.wings.android.zuboradiary.ui.list.diarylist.DiaryDayListItem;

import java.time.LocalDate;
import java.util.UUID;

@Entity(tableName = "diary_item_title_selection_history")
public class DiaryItemTitleSelectionHistoryItem implements Cloneable {
    @Ignore
    private final String id = UUID.randomUUID().toString();

    @PrimaryKey
    @NonNull
    private String title = "";
    @NonNull
    private String log = "";

    @NonNull
    public String getId() {
        return id;
    }

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

    @NonNull
    @Override
    public DiaryItemTitleSelectionHistoryItem clone() {
        DiaryItemTitleSelectionHistoryItem clone;
        try {
            clone = (DiaryItemTitleSelectionHistoryItem) super.clone();
        } catch (CloneNotSupportedException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                throw new InternalError(e);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        return clone;
    }
}
