package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.editdiary.DiaryDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class WordSearchRepository {
    private DiaryDatabase diaryDatabase;
    private DiaryDAO diaryDAO;

    public WordSearchRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = diaryDatabase.createDiaryDAO();
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

    public List<WordSearchResultListItemDiary> selectWordSearchResultList(
                                                        int num, int offset, String searchWord) {
        ListenableFuture<List<WordSearchResultListItemDiary>> listenableFutureResults;
        listenableFutureResults = diaryDAO.selectWordSearchResultList(num, offset, searchWord);
        List<WordSearchResultListItemDiary> results = null;
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
