package com.websarva.wings.android.zuboradiary.data.database;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import javax.inject.Inject;

public class EditDiarySelectItemTitleRepository {
    private SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO;

    @Inject
    public EditDiarySelectItemTitleRepository(SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO) {
        this.selectedItemTitlesHistoryDAO = selectedItemTitlesHistoryDAO;
    }

    public List<SelectedDiaryItemTitle> loadSelectedDiaryItemTitles(int numTitles, int offset) throws Exception {
        ListenableFuture<List<SelectedDiaryItemTitle>> listenableFutureResults
                = this.selectedItemTitlesHistoryDAO.selectSelectedDiaryItemTitlesAsync(numTitles, offset);
        return listenableFutureResults.get();
    }

    public List<Long> saveSelectedItemTitles(List<SelectedDiaryItemTitle> list) throws Exception {
        ListenableFuture<List<Long>> listenableFutureResults =
                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitlesAsync(list);
        return listenableFutureResults.get();
    }

    public Integer deleteSelectedDiaryItemTitle(SelectedDiaryItemTitle title) throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteSelectedDiaryItemTitleAsync(title);
        return listenableFutureResult.get();
    }

    public Integer deleteOldSelectedItemTitles() throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteOldSelectedDiaryItemTitlesAsync();
        return listenableFutureResult.get();
    }
}
