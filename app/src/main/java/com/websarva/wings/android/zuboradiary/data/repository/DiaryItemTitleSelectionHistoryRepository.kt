package com.websarva.wings.android.zuboradiary.data.repository

import android.database.sqlite.SQLiteException
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
import com.websarva.wings.android.zuboradiary.data.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryItem
import com.websarva.wings.android.zuboradiary.domain.model.error.DiaryItemTitleSelectionHistoryError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DiaryItemTitleSelectionHistoryRepository (
    private val diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
) {

    @Throws(DiaryItemTitleSelectionHistoryError.LoadSelectionHistory::class)
    fun loadSelectionHistory(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryItem>> {
        require(num >= 1)
        require(offset >= 0)

        return try {
            diaryItemTitleSelectionHistoryDAO
                .selectHistoryListOrderByLogDesc(num, offset)
                .map { list ->
                    list.map { it.toDomainModel() }
                }
        } catch (e: SQLiteException) {
            throw DiaryItemTitleSelectionHistoryError.LoadSelectionHistory(e)
        } catch (e: IllegalStateException) {
            throw DiaryItemTitleSelectionHistoryError.LoadSelectionHistory(e)
        }
    }

    @Throws(DiaryItemTitleSelectionHistoryError.DeleteSelectionHistoryItem::class)
    suspend fun deleteSelectionHistoryItem(title: String) {
        withContext(Dispatchers.IO) {
            try {
                diaryItemTitleSelectionHistoryDAO.deleteHistoryItem(title)
            } catch (e: SQLiteException) {
                throw DiaryItemTitleSelectionHistoryError.DeleteSelectionHistoryItem(e)
            } catch (e: IllegalStateException) {
                throw DiaryItemTitleSelectionHistoryError.DeleteSelectionHistoryItem(e)
            }
        }
    }
}
