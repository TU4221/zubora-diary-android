package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDateTime;

public class DiaryItemLiveData {
    private final int itemNumber;
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<String> comment = new MutableLiveData<>();
    private final MutableLiveData<LocalDateTime> titleUpdateLog = new MutableLiveData<>();

    public DiaryItemLiveData(int itemNumber) {
        this.itemNumber = itemNumber;
        initialize();
    }

    public void initialize() {
        title.setValue("");
        comment.setValue("");
        titleUpdateLog.setValue(null);
    }

    public void updateAll(String title, String comment, LocalDateTime titleUpdateLog) {
        this.title.setValue(title);
        this.comment.setValue(comment);
        this.titleUpdateLog.setValue(titleUpdateLog);
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public MutableLiveData<String> getComment() {
        return comment;
    }

    public MutableLiveData<LocalDateTime> getTitleUpdateLog() {
        return titleUpdateLog;
    }
}
