package com.websarva.wings.android.zuboradiary.ui.diary;

import android.app.Application;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedDiaryItemTitle;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedItemTitlesHistoryDAO;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultListItemDiary;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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
        ListenableFuture<Integer> listenableFutureResults = this.diaryDAO.countDiariesAsync();
        return listenableFutureResults.get();
    }

    public boolean hasDiary(int year, int month, int dayOfMonth) throws Exception {
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        ListenableFuture<Boolean> existDiaryListenableFuture = diaryDAO.hasDiaryAsync(stringDate);
        return existDiaryListenableFuture.get();
    }

    public Diary selectDiary(String date) throws Exception {
        ListenableFuture<Diary> diaryListenableFuture = diaryDAO.selectDiaryAsync(date);
        return  diaryListenableFuture.get();
    }

    public Diary selectNewestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectNewestDiaryAsync();
        return listenableFutureResult.get();
    }

    public Diary selectOldestDiary() throws Exception {
        ListenableFuture<Diary> listenableFutureResult = diaryDAO.selectOldestDiaryAsync();
        return listenableFutureResult.get();
    }

    public List<DiaryListItem> selectDiaryList(int num, int offset, @Nullable String date) throws Exception {
        ListenableFuture<List<DiaryListItem>> listenableFutureResults;
        if (!(date == null)) {
            listenableFutureResults = diaryDAO.selectDiaryListAsync(num, offset, date);
        } else {
            listenableFutureResults = diaryDAO.selectDiaryListAsync(num, offset);
        }
        return listenableFutureResults.get();
    }

    public int countWordSearchResults(String searchWord) throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                diaryDAO.countWordSearchResultsAsync(searchWord);
        return listenableFutureResult.get();
    }

    // TODO:下記戻り値である必要があるのか検討
    public ListenableFuture<List<WordSearchResultListItemDiary>> selectWordSearchResultList(
            int num, int offset, String searchWord) {
        return diaryDAO.selectWordSearchResultListAsync(num, offset, searchWord);

    }

    public List<Integer> selectDiaryDateList(int year, int month) throws Exception {
        String stringDateYearMonth = DateConverter.toStringLocalDateYearMonth(year, month);
        ListenableFuture<List<String>> listenableFutureResults =
                diaryDAO.selectDiaryDateListAsync(stringDateYearMonth);
        List<String> beforeList = listenableFutureResults.get();
        List<Integer> afterList = new ArrayList<>();
        beforeList.stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                int startIndex = s.indexOf("月") + 1;
                int endIndex = s.indexOf("日");
                String dayOfMonth = s.substring(startIndex, endIndex);
                afterList.add(Integer.parseInt(dayOfMonth));
            }
        });
        return afterList;
    }

    public void insertDiary(Diary diary, List<SelectedDiaryItemTitle> updateTitleList) throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception{
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception{
                        diaryDAO.insertDiary(diary);
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

    public void deleteAndInsertDiary(
            String deleteDiaryDate, Diary createDiary, List<SelectedDiaryItemTitle> updateTitleList)
            throws Exception {
        Future<Void> future = DiaryDatabase.EXECUTOR_SERVICE.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                diaryDatabase.runInTransaction(new Callable<Future<Void>>() {
                    @Override
                    public Future<Void> call() throws Exception {
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

    public void deleteDiary(String date) throws Exception {
        ListenableFuture<Integer> diaryListenableFuture = diaryDAO.deleteDiaryAsync(date);
        diaryListenableFuture.get();
    }
}
