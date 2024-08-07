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

    public ListenableFuture<List<DiaryItemTitleSelectionHistoryItem>> loadSelectedDiaryItemTitles(
            int numTitles, int offset) {
        return diaryItemTitleSelectionHistoryDAO.selectSelectedDiaryItemTitlesAsync(numTitles, offset);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    public ListenableFuture<List<Long>> saveSelectedItemTitles(List<DiaryItemTitleSelectionHistoryItem> list) {
        return diaryItemTitleSelectionHistoryDAO.insertSelectedDiaryItemTitlesAsync(list);
    }

    public ListenableFuture<Integer> deleteSelectedDiaryItemTitle(DiaryItemTitleSelectionHistoryItem title) {
        return diaryItemTitleSelectionHistoryDAO.deleteSelectedDiaryItemTitleAsync(title);
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    public ListenableFuture<Integer> deleteOldSelectedItemTitles() {
        return diaryItemTitleSelectionHistoryDAO.deleteOldSelectedDiaryItemTitlesAsync();
    }
}
