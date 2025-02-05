package com.websarva.wings.android.zuboradiary.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface DiaryDAO {
    // MEMO:@Query使用方法下記参照
    //      https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diaries")
    ListenableFuture<Integer> countDiaries();

    @Query("SELECT COUNT(*) FROM diaries WHERE date < :startDate")
    ListenableFuture<Integer> countDiaries(String startDate);

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    ListenableFuture<Boolean> existsDiary(String date);

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE picturePath = :uri)")
    ListenableFuture<Boolean> existsPicturePath(String uri);

    @Query("SELECT * FROM diaries WHERE date = :date")
    ListenableFuture<DiaryEntity> selectDiary(String date);

    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    ListenableFuture<DiaryEntity> selectNewestDiary();

    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    ListenableFuture<DiaryEntity> selectOldestDiary();

    @Query("SELECT date, title, picturePath FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    ListenableFuture<List<DiaryListItem>> selectDiaryListOrderByDateDesc(int num, int offset);

    @Query("SELECT date, title, picturePath FROM diaries WHERE date < :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    ListenableFuture<List<DiaryListItem>> selectDiaryListOrderByDateDesc(int num, int offset , String startDate);

    @Query("SELECT COUNT(*) " +
            "FROM diaries " +
            "WHERE title LIKE '%' || :word || '%' " +
            "OR item_1_title LIKE '%' || :word || '%'" +
            "OR item_1_comment LIKE '%' || :word || '%'" +
            "OR item_2_title LIKE '%' || :word || '%'" +
            "OR item_2_comment LIKE '%' || :word || '%'" +
            "OR item_3_title LIKE '%' || :word || '%'" +
            "OR item_3_comment LIKE '%' || :word || '%'" +
            "OR item_4_title LIKE '%' || :word || '%'" +
            "OR item_4_comment LIKE '%' || :word || '%'" +
            "OR item_5_title LIKE '%' || :word || '%'" +
            "OR item_5_comment LIKE '%' || :word || '%'")
    ListenableFuture<Integer> countWordSearchResults(String word);

    @Query("SELECT date, title, item_1_title, item_1_comment, " +
                "item_2_title, item_2_comment, " +
                "item_3_title, item_3_comment, " +
                "item_4_title, item_4_comment, " +
                "item_5_title, item_5_comment " +
            "FROM diaries " +
            "WHERE title LIKE '%' || :word || '%' " +
                "OR item_1_title LIKE '%' || :word || '%'" +
                "OR item_1_comment LIKE '%' || :word || '%'" +
                "OR item_2_title LIKE '%' || :word || '%'" +
                "OR item_2_comment LIKE '%' || :word || '%'" +
                "OR item_3_title LIKE '%' || :word || '%'" +
                "OR item_3_comment LIKE '%' || :word || '%'" +
                "OR item_4_title LIKE '%' || :word || '%'" +
                "OR item_4_comment LIKE '%' || :word || '%'" +
                "OR item_5_title LIKE '%' || :word || '%'" +
                "OR item_5_comment LIKE '%' || :word || '%'" +
            "ORDER BY date DESC LIMIT :num OFFSET :offset")
    ListenableFuture<List<WordSearchResultListItem>> selectWordSearchResultListOrderByDateDesc(int num, int offset, String word);

    /** @noinspection UnusedReturnValue*/
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertDiary(DiaryEntity diaryEntity);

    @Query("DELETE FROM diaries WHERE date = :date")
    ListenableFuture<Integer> deleteDiary(String date);

    @Query("DELETE FROM diaries")
    ListenableFuture<Integer> deleteAllDiaries();
}
