package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalendarRepository {
    private DiaryDatabase diaryDatabase;
    DiaryDAO diaryDAO;

    public CalendarRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = this.diaryDatabase.createDiaryDAO();
    }


}
