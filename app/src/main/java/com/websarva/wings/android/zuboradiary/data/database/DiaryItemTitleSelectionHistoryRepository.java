package com.websarva.wings.android.zuboradiary.data.database;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import javax.inject.Inject;

public class DiaryItemTitleSelectionHistoryRepository {
    private final DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO;

    @Inject
    public DiaryItemTitleSelectionHistoryRepository(DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO) {
        this.diaryItemTitleSelectionHistoryDAO = diaryItemTitleSelectionHistoryDAO;
    }

    public ListenableFuture<List<DiaryItemTitleSelectionHistoryItem>> selectHistoryOrderByLogDesc(
            int numTitles, int offset) {
        return diaryItemTitleSelectionHistoryDAO.selectHistoryOrderByLogDescAsync(numTitles, offset);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    public ListenableFuture<List<Long>> insertHistoryItem(List<DiaryItemTitleSelectionHistoryItem> list) {
        return diaryItemTitleSelectionHistoryDAO.insertHistoryItemAsync(list);
    }

    public ListenableFuture<Integer> deleteHistoryItem(String title) {
        return diaryItemTitleSelectionHistoryDAO.deleteHistoryItemAsync(title);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    public ListenableFuture<Integer> deleteOldHistoryItems() {
        return diaryItemTitleSelectionHistoryDAO.deleteOldSelectedDiaryItemTitlesAsync();
    }
}
