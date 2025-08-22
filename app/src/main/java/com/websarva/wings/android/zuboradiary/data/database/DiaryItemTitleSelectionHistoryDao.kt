package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface DiaryItemTitleSelectionHistoryDao {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT * FROM diary_item_title_selection_history ORDER BY log DESC LIMIT :numTitles OFFSET :offset")
    fun selectHistoryListOrderByLogDesc(
        numTitles: Int,
        offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        diaryItemTitleSelectionHistoryEntityList: List<DiaryItemTitleSelectionHistoryEntity>
    )

    @Query("DELETE FROM diary_item_title_selection_history WHERE title = :title")
    suspend fun deleteHistory(title: String)

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    @Query("DELETE FROM diary_item_title_selection_history WHERE title " +
            "NOT IN (SELECT title FROM diary_item_title_selection_history ORDER BY log DESC LIMIT 50 OFFSET 0)")
    suspend fun deleteOldHistory()

    @Query("DELETE FROM diary_item_title_selection_history")
    suspend fun deleteAllHistory()
}
