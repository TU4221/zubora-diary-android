package com.websarva.wings.android.zuboradiary.data.database;

import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;


public class DiaryListRepository {
    private DiaryDatabase diaryDatabase;
    private DiaryDAO diaryDAO;

    @Inject
    public DiaryListRepository(DiaryDAO diaryDAO) {
        this.diaryDAO = diaryDAO;
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
