package com.websarva.wings.android.zuboradiary.data.database;



import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.inject.Inject;

public class DiaryRepository {
    private final DiaryDatabase diaryDatabase;
    private final DiaryDAO diaryDAO;
    private final DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO;

    @Inject
    public DiaryRepository(
            DiaryDatabase diaryDatabase, DiaryDAO diaryDAO,
            DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO) {
        this.diaryDatabase = diaryDatabase;
        this.diaryDAO = diaryDAO;
        this.diaryItemTitleSelectionHistoryDAO = diaryItemTitleSelectionHistoryDAO;
    }

    public ListenableFuture<Integer> countDiaries(@Nullable LocalDate date){
        ListenableFuture<Integer> listenableFutureResults;
        if (date == null) {
            listenableFutureResults = this.diaryDAO.countDiariesAsync();
        } else {
            listenableFutureResults = this.diaryDAO.countDiariesAsync(date.toString());
        }
        return listenableFutureResults;
    }

    public ListenableFuture<Boolean> hasDiary(LocalDate date) {
        return diaryDAO.hasDiaryAsync(date.toString());
    }

    public ListenableFuture<Diary> selectDiary(LocalDate date) {
        return  diaryDAO.selectDiaryAsync(date.toString());
    }

    public ListenableFuture<Diary> selectNewestDiary() {
        return diaryDAO.selectNewestDiaryAsync();
    }

    public ListenableFuture<Diary> selectOldestDiary() {
        return diaryDAO.selectOldestDiaryAsync();
    }

    public ListenableFuture<List<DiaryListItem>> loadDiaryList(
            int num, int offset, @Nullable LocalDate date) {
        Log.d("DiaryRepository", "loadDiaryList(num = " + num + ", offset = " + offset + ", date = " + date + ")");
        if (date == null) {
            return diaryDAO.selectDiaryListAsync(num, offset);
        } else {
            return diaryDAO.selectDiaryListAsync(num, offset, date.toString());
        }
    }

    public ListenableFuture<Integer> countWordSearchResults(String searchWord) {
        return diaryDAO.countWordSearchResultsAsync(searchWord);
    }

    public ListenableFuture<List<WordSearchResultListItem>> selectWordSearchResultList(
            int num, int offset, String searchWord) {
        return diaryDAO.selectWordSearchResultListAsync(num, offset, searchWord);
    }

    public Future<Void> insertDiary(Diary diary, List<DiaryItemTitleSelectionHistoryItem> updateTitleList) {
        return DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() {
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() {
                        diaryDAO.insertDiaryAsync(diary);
                        diaryItemTitleSelectionHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        diaryItemTitleSelectionHistoryDAO.deleteOldSelectedDiaryItemTitles();
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> deleteAndInsertDiary(
            LocalDate deleteDiaryDate, Diary createDiary, List<DiaryItemTitleSelectionHistoryItem> updateTitleList)
             {
        return DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() {
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() {
                        diaryDAO.deleteDiaryAsync(deleteDiaryDate.toString());
                        diaryDAO.insertDiaryAsync(createDiary);
                        diaryItemTitleSelectionHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        diaryItemTitleSelectionHistoryDAO.deleteOldSelectedDiaryItemTitles();
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public ListenableFuture<Integer> deleteDiary(LocalDate date) throws Exception {
        return diaryDAO.deleteDiaryAsync(date.toString());
    }
}
