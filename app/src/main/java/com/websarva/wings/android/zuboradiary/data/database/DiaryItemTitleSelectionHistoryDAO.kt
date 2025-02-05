package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface DiaryItemTitleSelectionHistoryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT * FROM diary_item_title_selection_history ORDER BY log DESC LIMIT :numTitles OFFSET :offset")
    fun selectHistoryListOrderByLogDesc(
        numTitles: Int,
        offset: Int
    ): ListenableFuture<List<DiaryItemTitleSelectionHistoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoryItem(
        diaryItemTitleSelectionHistoryItemEntityList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ): ListenableFuture<List<Long>>

    @Query("DELETE FROM diary_item_title_selection_history WHERE title = :title")
    fun deleteHistoryItem(title: String): ListenableFuture<Int>

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    @Query("DELETE FROM diary_item_title_selection_history WHERE title " +
            "NOT IN (SELECT title FROM diary_item_title_selection_history ORDER BY log DESC LIMIT 50 OFFSET 0)")
    fun deleteOldHistoryItem(): ListenableFuture<Int>

    @Query("DELETE FROM diary_item_title_selection_history")
    fun deleteAllItem(): ListenableFuture<Void?>
}
