package com.websarva.wings.android.zuboradiary.data.database;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class DiaryItemTitleSelectionHistoryRepository {
    private final DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO;

    @Inject
    public DiaryItemTitleSelectionHistoryRepository(DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO) {
        Objects.requireNonNull(diaryItemTitleSelectionHistoryDAO);

        this.diaryItemTitleSelectionHistoryDAO = diaryItemTitleSelectionHistoryDAO;
    }

    @NonNull
    public ListenableFuture<List<DiaryItemTitleSelectionHistoryItemEntity>> loadSelectionHistory(
            int num, int offset) {
        if (num < 1) throw new IllegalArgumentException();
        if (offset < 0) throw new IllegalArgumentException();

        ListenableFuture<List<DiaryItemTitleSelectionHistoryItemEntity>> future =
                diaryItemTitleSelectionHistoryDAO.selectHistoryListOrderByLogDesc(num, offset);
        return Objects.requireNonNull(future);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    @NonNull
    public ListenableFuture<List<Long>> saveSelectionHistoryItems(
                                            List<DiaryItemTitleSelectionHistoryItemEntity> list) {
        Objects.requireNonNull(list);
        list.stream().forEach(Objects::requireNonNull);

        ListenableFuture<List<Long>> future = diaryItemTitleSelectionHistoryDAO.insertHistoryItem(list);
        return Objects.requireNonNull(future);
    }

    @NonNull
    public ListenableFuture<Integer> deleteSelectionHistoryItem(String title) {
        Objects.requireNonNull(title);

        ListenableFuture<Integer> future = diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title);
        return Objects.requireNonNull(future);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    @NonNull
    public ListenableFuture<Integer> deleteOldHistoryItems() {
        ListenableFuture<Integer> future = diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem();
        return Objects.requireNonNull(future);
    }
}
