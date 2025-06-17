package com.websarva.wings.android.zuboradiary.data.database

import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DiaryItemTitleSelectionHistoryRepository (
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    fun loadSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItem>> {
        require(num >= 1)
        require(offset >= 0)

        return diaryItemTitleSelectionHistoryDAO
            .selectHistoryListOrderByLogDesc(num, offset)
            .map { list ->
                list.map { it.toDomainModel() }
            }
    }

    suspend fun deleteSelectionHistoryItem(title: String) {
        withContext(Dispatchers.IO) {
            diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title)
        }
    }
}
