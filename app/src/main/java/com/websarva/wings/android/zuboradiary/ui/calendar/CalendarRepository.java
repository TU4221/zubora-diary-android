package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalendarRepository {
    private DiaryDatabase diaryDatabase;
    DiaryDAO diaryDAO;

    public CalendarRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = this.diaryDatabase.createDiaryDAO();
    }

    public List<String> selectDiaryDateList(String dateYearMonth) {
        ListenableFuture<List<String>> listenableFutureResults;
        listenableFutureResults = diaryDAO.selectDiaryDateList(dateYearMonth);
        List<String> results = null;
        try {
            results = listenableFutureResults.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        return results;
    }
}
