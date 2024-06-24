package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface SelectedItemTitlesHistoryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query

    @Query("SELECT * FROM diary_item_title_history ORDER BY log DESC LIMIT :numTitles OFFSET :offset")
    ListenableFuture<List<SelectedDiaryItemTitle>> selectSelectedDiaryItemTitlesAsync(int numTitles, int offset);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertSelectedDiaryItemTitlesAsync(
            List<SelectedDiaryItemTitle> selectedDiaryItemTitles);

    // 他DAO(他テーブルへの書き込み処理)メソッドと同じタイミング(Transaction)で処理する時に使用
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSelectedDiaryItemTitles(
            List<SelectedDiaryItemTitle> selectedDiaryItemTitles);

    @Delete
    ListenableFuture<Integer> deleteSelectedDiaryItemTitleAsync(SelectedDiaryItemTitle selectedDiaryItemTitle);

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    @Query("DELETE FROM diary_item_title_history WHERE title " +
        "NOT IN (SELECT title FROM diary_item_title_history ORDER BY log DESC LIMIT 50 OFFSET 0)")
    ListenableFuture<Integer> deleteOldSelectedDiaryItemTitlesAsync();

}
