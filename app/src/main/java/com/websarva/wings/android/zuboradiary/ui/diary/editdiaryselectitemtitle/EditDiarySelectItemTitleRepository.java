package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.app.Application;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryDatabase;

import java.util.List;

public class EditDiarySelectItemTitleRepository {
    private DiaryDatabase selectedItemTitlesHistoryDatabase;
    private SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO;

    public EditDiarySelectItemTitleRepository(Application application) {
        this.selectedItemTitlesHistoryDatabase = DiaryDatabase.getDatabase(application);
        this.selectedItemTitlesHistoryDAO =
                selectedItemTitlesHistoryDatabase.createSelectedItemTitlesHistoryDAO();
    }

    public List<SelectedDiaryItemTitle> loadSelectedDiaryItemTitles(int numTitles, int offset) throws Exception {
        ListenableFuture<List<SelectedDiaryItemTitle>> listenableFutureResults
                = this.selectedItemTitlesHistoryDAO.selectSelectedDiaryItemTitles(numTitles, offset);
        return listenableFutureResults.get();
    }

    public List<Long> saveSelectedItemTitles(List<SelectedDiaryItemTitle> list) throws Exception {
        ListenableFuture<List<Long>> listenableFutureResults =
                selectedItemTitlesHistoryDAO.insertSelectedDiaryItemTitles(list);
        return listenableFutureResults.get();
    }

    public Integer deleteSelectedDiaryItemTitle(SelectedDiaryItemTitle title) throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteSelectedDiaryItemTitle(title);
        return listenableFutureResult.get();
    }

    public Integer deleteOldSelectedItemTitles() throws Exception {
        ListenableFuture<Integer> listenableFutureResult =
                selectedItemTitlesHistoryDAO.deleteOldSelectedDiaryItemTitles();
        return listenableFutureResult.get();
    }
}
