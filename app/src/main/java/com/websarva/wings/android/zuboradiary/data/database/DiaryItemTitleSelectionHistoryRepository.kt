package com.websarva.wings.android.zuboradiary.data.database

import kotlinx.coroutines.flow.Flow

internal class DiaryItemTitleSelectionHistoryRepository (
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    fun loadSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItemEntity>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryItemTitleSelectionHistoryDAO.selectHistoryListOrderByLogDesc(num, offset)
    }

    suspend fun deleteSelectionHistoryItem(title: String) {
        return diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title)
    }
}
