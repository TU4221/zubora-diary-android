package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditDiarySelectItemTitleRepository {
    private SelectedItemTitlesHistoryDatabase selectedItemTitlesHistoryDatabase;
    private SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO;

    public EditDiarySelectItemTitleRepository(Application application) {
        this.selectedItemTitlesHistoryDatabase = SelectedItemTitlesHistoryDatabase.getDatabase(application);
        this.selectedItemTitlesHistoryDAO = selectedItemTitlesHistoryDatabase.createSelectedItemTitlesHistoryDAO();
    }

    public int countSelectedItemTitles() {
        ListenableFuture<Integer> listenableFutureResult = this.selectedItemTitlesHistoryDAO.countSelectedItemTitles();
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

    public List<DiaryItemTitle> selectSelectedItemTitles(int num) {
        ListenableFuture<List<DiaryItemTitle>> listenableFutureResults
                = this.selectedItemTitlesHistoryDAO.selectSelectedItemTitles(num);
        List<DiaryItemTitle> results = new ArrayList<>();
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

    public void insertSelectedItemTitles(List<DiaryItemTitle> list) {
        ListenableFuture<List<Long>> listenableFutureResults =
                selectedItemTitlesHistoryDAO.insertSelectedItemTitles(list);

        try {
            listenableFutureResults.get();
        }
        catch (ExecutionException ex) {
            Log.d("ROOM通信エラー", "ExecutionException");
        }
        catch (InterruptedException ex) {
            Log.d("ROOM通信エラー", "InterruptedException");
        }
    }

    public void deleteSelectedItemTitle(DiaryItemTitle title) {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteSelectedItemTitle(title);

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

    public void deleteOldSelectedItemTitles() {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteOldSelectedItemTitles();

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
