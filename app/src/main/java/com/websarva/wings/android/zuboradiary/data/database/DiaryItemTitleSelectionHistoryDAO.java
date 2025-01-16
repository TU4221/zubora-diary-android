package com.websarva.wings.android.zuboradiary.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface DiaryItemTitleSelectionHistoryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query

    @Query("SELECT * FROM diary_item_title_selection_history ORDER BY log DESC LIMIT :numTitles OFFSET :offset")
    ListenableFuture<List<DiaryItemTitleSelectionHistoryItemEntity>> selectHistoryListOrderByLogDesc(int numTitles, int offset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertHistoryItem(
            List<DiaryItemTitleSelectionHistoryItemEntity> diaryItemTitleSelectionHistoryItemEntityList);

    @Query("DELETE FROM diary_item_title_selection_history WHERE title = :title")
    ListenableFuture<Integer> deleteHistoryItem(String title);

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    String query = "DELETE FROM diary_item_title_selection_history WHERE title " +
            "NOT IN (SELECT title FROM diary_item_title_selection_history ORDER BY log DESC LIMIT 50 OFFSET 0)";
    @Query(query)
    ListenableFuture<Integer> deleteOldHistoryItem();

    /** @noinspection UnusedReturnValue*/
    @Query("DELETE FROM diary_item_title_selection_history")
    ListenableFuture<Void> deleteAllItem();
}
