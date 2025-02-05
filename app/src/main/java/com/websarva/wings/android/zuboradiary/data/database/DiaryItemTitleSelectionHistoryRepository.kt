package com.websarva.wings.android.zuboradiary.data.database

import com.google.common.util.concurrent.ListenableFuture
import javax.inject.Inject

class DiaryItemTitleSelectionHistoryRepository @Inject constructor(
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    fun loadSelectionHistory(
        num: Int, offset: Int
    ): ListenableFuture<List<DiaryItemTitleSelectionHistoryItemEntity>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryItemTitleSelectionHistoryDAO.selectHistoryListOrderByLogDesc(num, offset)
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    fun saveSelectionHistoryItems(
        list: List<DiaryItemTitleSelectionHistoryItemEntity>
    ): ListenableFuture<List<Long>> {
        return diaryItemTitleSelectionHistoryDAO.insertHistoryItem(list)
    }

    fun deleteSelectionHistoryItem(title: String): ListenableFuture<Int> {
        return diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title)
    }

    // MEMO:保存する時は日記保存と同時に処理したいので、DiaryRepositoryにて処理。
    fun deleteOldHistoryItems(): ListenableFuture<Int> {
        return diaryItemTitleSelectionHistoryDAO.deleteOldHistoryItem()
    }
}
