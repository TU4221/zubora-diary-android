package com.websarva.wings.android.zuboradiary.ui.editdiaryselectitemtitle;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface SelectedItemTitlesHistoryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diary_item_title_history")
    public ListenableFuture<Integer> countSelectedItemTitles();

    @Query("SELECT * FROM diary_item_title_history ORDER BY log DESC LIMIT :num")
    public ListenableFuture<List<DiaryItemTitle>> selectSelectedItemTitles(int num);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Long> insertSelectedItemTitles(
            DiaryItemTitle diaryItemTitle);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<List<Long>> insertSelectedItemTitles(
                                                            List<DiaryItemTitle> diaryItemTitles);

    @Update
    public ListenableFuture<Integer> updateSelectedItemTitles(
                                                            List<DiaryItemTitle> diaryItemTitles);

    @Delete
    public ListenableFuture<Integer> deleteSelectedItemTitle(DiaryItemTitle diaryItemTitle);

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    @Query("DELETE FROM diary_item_title_history WHERE title " +
        "NOT IN (SELECT title FROM diary_item_title_history ORDER BY log DESC LIMIT 50 OFFSET 0)")
    public ListenableFuture<Integer> deleteOldSelectedItemTitles();

}
