package com.websarva.wings.android.zuboradiary.ui.list;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.Diary;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDatabase;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DiaryListRepository {
    private DiaryDatabase diaryDatabase;
    private DiaryDAO diaryDAO;

    public DiaryListRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = diaryDatabase.createDiaryDAO();
    }

    public int countDiaries() {
        ListenableFuture<Integer> listenableFutureResults = this.diaryDAO.countDiaries();
        Integer result = 0;
        try {
            result = listenableFutureResults.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        return result;
    }

    public List<DiaryListItem> getListItemDiaries(int num, int offset, @Nullable String date) {
        ListenableFuture<List<DiaryListItem>> listenableFutureResults;
        if (!(date == null)) {
            listenableFutureResults = diaryDAO.selectDiaryList(num, offset, date);
        } else {
            listenableFutureResults = diaryDAO.selectDiaryList(num, offset);
        }
        List<DiaryListItem> results = null;
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

    public void deleteDiary(String date) {
        ListenableFuture<Integer> listenableFutureResult = diaryDAO.deleteDiary(date);

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

    public Diary selectNewestDiary() {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectNewestDiary();

        Diary result = null;
        try {
            result = listenableFutureResult.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        return result;
    }

    public Diary selectOldestDiary() {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectOldestDiary();

        Diary result = null;
        try {
            result = listenableFutureResult.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        return result;
    }
}
