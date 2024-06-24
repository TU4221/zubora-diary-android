package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDatabase;

import java.util.concurrent.ExecutionException;

public class DiaryListRepository {
    private DiaryDatabase diaryDatabase;
    private DiaryDAO diaryDAO;

    public DiaryListRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = diaryDatabase.createDiaryDAO();
    }





    public void deleteDiary(String date) {
        ListenableFuture<Integer> listenableFutureResult = diaryDAO.deleteDiaryAsync(date);

        try {
            listenableFutureResult.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }


}
