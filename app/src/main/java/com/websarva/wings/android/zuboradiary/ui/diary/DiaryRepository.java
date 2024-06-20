package com.websarva.wings.android.zuboradiary.ui.diary;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedDiaryItemTitle;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedItemTitlesHistoryDAO;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultListItemDiary;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DiaryRepository {
    private DiaryDatabase diaryDatabase;
    DiaryDAO diaryDAO;
    SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO;

    private ListenableFuture<Long> insertDiaryListenableFutureForTransaction;
    private ListenableFuture<Integer> updateDiaryListenableFutureForTransaction;
    private ListenableFuture<Integer> deleteDiaryListenableFutureForTransaction;
    private ListenableFuture<List<Long>> updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction;

    public DiaryRepository(Application application) {
        diaryDatabase = DiaryDatabase.getDatabase(application);
        diaryDAO = diaryDatabase.createDiaryDAO();
        selectedItemTitlesHistoryDAO = diaryDatabase.createSelectedItemTitlesHistoryDAO();
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

    public List<DiaryListItem> selectDiaryList(int num, int offset, @Nullable String date) {
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

    public int countWordSearchResults(String searchWord) {
        ListenableFuture<Integer> listenableFutureResult;
        listenableFutureResult = diaryDAO.countWordSearchResults(searchWord);
        Integer result = 0;
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

    public ListenableFuture<List<WordSearchResultListItemDiary>> selectWordSearchResultList(
            int num, int offset, String searchWord) {

        Log.d("20240611", "selectWordSearchResultList");
        return diaryDAO.selectWordSearchResultList(num, offset, searchWord);

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

    public void insertDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        insertDiaryListenableFutureForTransaction = diaryDAO.insertDiary(diary);
                        updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction =
                                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });

        try {
            future.get();
            insertDiaryListenableFutureForTransaction.get();
            updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void updateDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) {

        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        updateDiaryListenableFutureForTransaction = diaryDAO.updateDiary(diary);
                        updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction =
                                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });

        try {
            future.get();
            updateDiaryListenableFutureForTransaction.get();
            updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void deleteAndInsertDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList) {

        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        deleteDiaryListenableFutureForTransaction = diaryDAO.deleteDiary(deleteDiaryDate);
                        insertDiaryListenableFutureForTransaction = diaryDAO.insertDiary(createDiary);
                        updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction =
                                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
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
            updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }

    }

    public void deleteAndUpdateDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList) {

        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Future<Void> future = diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
                        deleteDiaryListenableFutureForTransaction = diaryDAO.deleteDiary(deleteDiaryDate);
                        updateDiaryListenableFutureForTransaction = diaryDAO.updateDiary(createDiary);
                        updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction =
                                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
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
            updateSelectedDiaryItemTitleHistoryListenableFutureForTransaction.get();
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
