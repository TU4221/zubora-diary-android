package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.database.DataBaseAccessException
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.error.DiaryItemTitleSelectionHistoryError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DiaryItemTitleSelectionHistoryRepository (
    private val diaryDataSource: DiaryDataSource
) {

    @Throws(DiaryItemTitleSelectionHistoryError.LoadSelectionHistory::class)
    fun loadSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItem>> {
        require(num >= 1)
        require(offset >= 0)

        return try {
            diaryDataSource
                .selectHistoryListOrderByLogDesc(num, offset)
                .map { list ->
                    list.map { it.toDomainModel() }
                }
        } catch (e: DataBaseAccessException) {
            throw DiaryItemTitleSelectionHistoryError.LoadSelectionHistory(e)
        }
    }

    @Throws(DiaryItemTitleSelectionHistoryError.DeleteSelectionHistoryItem::class)
    suspend fun deleteSelectionHistoryItem(title: String) {
        withContext(Dispatchers.IO) {
            try {
                diaryDataSource.deleteHistoryItem(title)
            } catch (e: DataBaseAccessException) {
                throw DiaryItemTitleSelectionHistoryError.DeleteSelectionHistoryItem(e)
            }
        }
    }
}
