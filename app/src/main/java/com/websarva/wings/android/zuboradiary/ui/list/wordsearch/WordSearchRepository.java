package com.websarva.wings.android.zuboradiary.ui.list.wordsearch;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDAO;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDatabase;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;

public class WordSearchRepository {
    private DiaryDatabase diaryDatabase;
    private DiaryDAO diaryDAO;
    private Application application;

    public WordSearchRepository(Application application) {
        this.diaryDatabase = DiaryDatabase.getDatabase(application);
        this.diaryDAO = diaryDatabase.createDiaryDAO();
        this.application = application;
    }



    /*public List<WordSearchResultListItemDiary> selectWordSearchResultList(
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

    }*/



}
