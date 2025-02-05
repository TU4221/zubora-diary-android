package com.websarva.wings.android.zuboradiary.data.database;


import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
        Objects.requireNonNull(diaryDatabase);
        Objects.requireNonNull(diaryDAO);
        Objects.requireNonNull(diaryItemTitleSelectionHistoryDAO);

        this.diaryDatabase = diaryDatabase;
        this.diaryDAO = diaryDAO;
        this.diaryItemTitleSelectionHistoryDAO = diaryItemTitleSelectionHistoryDAO;
    }

    @NonNull
    public ListenableFuture<Integer> countDiaries(){
        ListenableFuture<Integer> future = diaryDAO.countDiaries();
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Integer> countDiaries(LocalDate date){
        Objects.requireNonNull(date);

        ListenableFuture<Integer> future = diaryDAO.countDiaries(date.toString());
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Boolean> existsDiary(LocalDate date) {
        Objects.requireNonNull(date);

        ListenableFuture<Boolean> future = diaryDAO.existsDiary(date.toString());
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Boolean> existsPicturePath(Uri uri) {
        Objects.requireNonNull(uri);

        ListenableFuture<Boolean> future = diaryDAO.existsPicturePath(uri.toString());
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<DiaryEntity> loadDiary(LocalDate date) {
        Objects.requireNonNull(date);

        ListenableFuture<DiaryEntity> future = diaryDAO.selectDiary(date.toString());
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<DiaryEntity> loadNewestDiary() {
        ListenableFuture<DiaryEntity> future = diaryDAO.selectNewestDiary();
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<DiaryEntity> loadOldestDiary() {
        ListenableFuture<DiaryEntity> future = diaryDAO.selectOldestDiary();
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<List<DiaryListItem>> loadDiaryList(
            int num, int offset, @Nullable LocalDate date) {
        if (num < 1) throw new IllegalArgumentException();
        if (offset < 0) throw new IllegalArgumentException();

        Log.d("DiaryRepository", "loadDiaryList(num = " + num + ", offset = " + offset + ", date = " + date + ")");
        ListenableFuture<List<DiaryListItem>> future;
        if (date == null) {
            future = diaryDAO.selectDiaryListOrderByDateDesc(num, offset);
        } else {
            future = diaryDAO.selectDiaryListOrderByDateDesc(num, offset, date.toString());
        }
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Integer> countWordSearchResultDiaries(String searchWord) {
        Objects.requireNonNull(searchWord);

        ListenableFuture<Integer> future = diaryDAO.countWordSearchResults(searchWord);
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<List<WordSearchResultListItem>> loadWordSearchResultDiaryList(
            int num, int offset, String searchWord) {
        if (num < 1) throw new IllegalArgumentException();
        if (offset < 0) throw new IllegalArgumentException();
        Objects.requireNonNull(searchWord);

        ListenableFuture<List<WordSearchResultListItem>> future =
                diaryDAO.selectWordSearchResultListOrderByDateDesc(num, offset, searchWord);
        return Objects.requireNonNull(future);
    }

    @NonNull
    public Future<Void> saveDiary(DiaryEntity diaryEntity, List<DiaryItemTitleSelectionHistoryItemEntity> updateTitleList) {
        Objects.requireNonNull(diaryEntity);
        Objects.requireNonNull(updateTitleList);
        updateTitleList.stream().forEach(Objects::requireNonNull);

        Future<Void> future =
                DiaryDatabase.EXECUTOR_SERVICE.submit(() -> diaryDatabase.runInTransaction(() -> {
                    diaryDAO.insertDiary(diaryEntity);
                    diaryItemTitleSelectionHistoryDAO.insertHistoryItem(updateTitleList);
                    diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem();
                    return null;
                }));
        return Objects.requireNonNull(future);
    }

    @NonNull
    public Future<Void> deleteAndSaveDiary(
            LocalDate deleteDiaryDate, DiaryEntity createDiaryEntity,
            List<DiaryItemTitleSelectionHistoryItemEntity> updateTitleList) {
        Objects.requireNonNull(deleteDiaryDate);
        Objects.requireNonNull(createDiaryEntity);
        Objects.requireNonNull(updateTitleList);
        updateTitleList.stream().forEach(Objects::requireNonNull);

        Future<Void> future =
                DiaryDatabase.EXECUTOR_SERVICE.submit(() -> diaryDatabase.runInTransaction(() -> {
                    diaryDAO.deleteDiary(deleteDiaryDate.toString());
                    diaryDAO.insertDiary(createDiaryEntity);
                    diaryItemTitleSelectionHistoryDAO.insertHistoryItem(updateTitleList);
                    diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem();
                    return null;
                }));
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Integer> deleteDiary(LocalDate date) {
        Objects.requireNonNull(date);

        ListenableFuture<Integer> future = diaryDAO.deleteDiary(date.toString());
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Integer> deleteAllDiaries() {
        ListenableFuture<Integer> future = diaryDAO.deleteAllDiaries();
        return Objects.requireNonNull(future);
    }

    @NonNull
    public Future<Void> deleteAllData() {
        Future<Void> future =
                DiaryDatabase.EXECUTOR_SERVICE.submit(() -> diaryDatabase.runInTransaction(() -> {
                    diaryDAO.deleteAllDiaries();
                    diaryItemTitleSelectionHistoryDAO.deleteAllItem();
                    return null;
                }));
        return Objects.requireNonNull(future);
    }
}
