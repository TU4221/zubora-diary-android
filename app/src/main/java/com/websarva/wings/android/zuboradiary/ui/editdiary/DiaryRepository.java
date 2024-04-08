package com.websarva.wings.android.zuboradiary.ui.editdiary;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DiaryRepository {
    private DiaryDatabase diaryDatabase;
    DiaryDAO diaryDAO;

    private ListenableFuture<Long> insertDiaryListenableFutureForTransaction;
    private ListenableFuture<Integer> updateDiaryListenableFutureForTransaction;
    private ListenableFuture<Integer> deleteDiaryListenableFutureForTransaction;

    public DiaryRepository(Application application) {
        diaryDatabase = DiaryDatabase.getDatabase(application);
        diaryDAO = diaryDatabase.createDiaryDAO();
    }

    public boolean hasDiary(String date) {
        Log.d("20240328",date);

        ListenableFuture<Boolean> existDiaryListenableFuture = diaryDAO.hasDiary(date);

        Boolean hasDiary = false;
        try {
            hasDiary = existDiaryListenableFuture.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        Log.d("20240328","hasDiary:" + String.valueOf(hasDiary));

        return hasDiary;
    }

    public Diary selectDiary(String date) {
        Log.d("20240328",date);

        ListenableFuture<Diary> diaryListenableFuture = diaryDAO.selectDiary(date);

        Diary diary = null;
        try {
            diary = diaryListenableFuture.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
        if (diary == null){
            Log.d("20240328","Diary = null");
        }

        return diary;
    }

    public void insertDiary(Diary diary) {

        ListenableFuture<Long> diaryListenableFuture = diaryDAO.insertDiary(diary);

        try {
            diaryListenableFuture.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void deleteAndInsertDiary(String deleteDiaryDate, Diary createDiary) {

        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        deleteDiaryListenableFutureForTransaction = diaryDAO.deleteDiary(deleteDiaryDate);
                        insertDiaryListenableFutureForTransaction = diaryDAO.insertDiary(createDiary);
                        return null;
                    }
                });
                return null;
            }
        });

        try {
            future.get();
            deleteDiaryListenableFutureForTransaction.get();
            insertDiaryListenableFutureForTransaction.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }

    }

    public void updateDiary(Diary diary) {

        ListenableFuture<Integer> diaryListenableFuture = diaryDAO.updateDiary(diary);

        try {
            diaryListenableFuture.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void deleteAndUpdateDiary(String deleteDiaryDate, Diary createDiary) {

        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        deleteDiaryListenableFutureForTransaction = diaryDAO.deleteDiary(deleteDiaryDate);
                        updateDiaryListenableFutureForTransaction = diaryDAO.updateDiary(createDiary);
                        return null;
                    }
                });
                return null;
            }
        });

        try {
            future.get();
            deleteDiaryListenableFutureForTransaction.get();
            updateDiaryListenableFutureForTransaction.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void deleteDiary(String date) {

        ListenableFuture<Integer> diaryListenableFuture = diaryDAO.deleteDiary(date);

        try {
            diaryListenableFuture.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }
}
