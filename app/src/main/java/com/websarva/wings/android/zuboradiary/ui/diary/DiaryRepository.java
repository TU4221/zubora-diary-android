package com.websarva.wings.android.zuboradiary.ui.diary;

import android.app.Application;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.DateConverter;
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

    public int countDiaries() throws Exception {
        ListenableFuture<Integer> listenableFutureResults = this.diaryDAO.countDiaries();
        return listenableFutureResults.get();
    }

    public boolean hasDiary(int year, int month, int dayOfMonth) throws Exception {
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        ListenableFuture<Boolean> existDiaryListenableFuture = diaryDAO.hasDiary(stringDate);
        return existDiaryListenableFuture.get();
    }

    public Diary selectDiary(String date) throws Exception {
        ListenableFuture<Diary> diaryListenableFuture = diaryDAO.selectDiary(date);
        return  diaryListenableFuture.get();
    }

    public Diary selectNewestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectNewestDiary();
        return listenableFutureResult.get();
    }

    public Diary selectOldestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectOldestDiary();
        return listenableFutureResult.get();
    }

    public List<DiaryListItem> selectDiaryList(int num, int offset, @Nullable String date) throws Exception {
        ListenableFuture<List<DiaryListItem>> listenableFutureResults;
        if (!(date == null)) {
            listenableFutureResults = diaryDAO.selectDiaryList(num, offset, date);
        } else {
            listenableFutureResults = diaryDAO.selectDiaryList(num, offset);
        }
        return listenableFutureResults.get();
    }

    public int countWordSearchResults(String searchWord) throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                diaryDAO.countWordSearchResults(searchWord);
        return listenableFutureResult.get();
    }

    // TODO:下記戻り値である必要があるのか検討
    public ListenableFuture<List<WordSearchResultListItemDiary>> selectWordSearchResultList(
            int num, int offset, String searchWord) {
        return diaryDAO.selectWordSearchResultList(num, offset, searchWord);

    }

    public List<String> selectDiaryDateList(String dateYearMonth) throws Exception {
        ListenableFuture<List<String>> listenableFutureResults =
                diaryDAO.selectDiaryDateList(dateYearMonth);
        return listenableFutureResults.get();
    }

    public void insertDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call(){
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call(){
                        diaryDAO.insertDiary(diary);
                        selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void updateDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call(){
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call(){
                        diaryDAO.updateDiary(diary);
                        selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void deleteAndInsertDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList)
            throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call(){
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call(){
                        diaryDAO.deleteDiary(deleteDiaryDate);
                        diaryDAO.insertDiary(createDiary);
                        selectedItemTitlesHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void deleteAndUpdateDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList)
            throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call(){
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call(){
                        diaryDAO.deleteDiary(deleteDiaryDate);
                        diaryDAO.updateDiary(createDiary);
                        selectedItemTitlesHistoryDAO
                                .insertSelectedDiaryItemTitles(updateTitleList);
                        return null;
                    }
                });
                return null;
            }
        });
        future.get();
    }

    public void deleteDiary(String date) throws Exception {
        ListenableFuture<Integer> diaryListenableFuture = diaryDAO.deleteDiary(date);
        diaryListenableFuture.get();
    }
}
