package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.common.util.concurrent.ListenableFuture

@Dao
interface DiaryDAO {
    // MEMO:@Query使用方法下記参照
    //      https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diaries")
    fun countDiaries(): ListenableFuture<Int>

    @Query("SELECT COUNT(*) FROM diaries WHERE date < :startDate")
    fun countDiaries(startDate: String): ListenableFuture<Int>

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    fun existsDiary(date: String): ListenableFuture<Boolean>

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE picturePath = :uri)")
    fun existsPicturePath(uri: String): ListenableFuture<Boolean>

    @Query("SELECT * FROM diaries WHERE date = :date")
    fun selectDiary(date: String): ListenableFuture<DiaryEntity>

    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    fun selectNewestDiary(): ListenableFuture<DiaryEntity>

    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    fun selectOldestDiary(): ListenableFuture<DiaryEntity>

    @Query("SELECT date, title, picturePath FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int
    ): ListenableFuture<List<DiaryListItem>>

    @Query("SELECT date, title, picturePath FROM diaries WHERE date < :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int,
        startDate: String
    ): ListenableFuture<List<DiaryListItem>>

    @Query(
        ("SELECT COUNT(*) " +
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
    )
    fun countWordSearchResults(word: String): ListenableFuture<Int>

    @Query(
        ("SELECT date, title, item_1_title, item_1_comment, " +
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
    )
    fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        word: String
    ): ListenableFuture<List<WordSearchResultListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDiary(diaryEntity: DiaryEntity): ListenableFuture<Long>

    @Query("DELETE FROM diaries WHERE date = :date")
    fun deleteDiary(date: String): ListenableFuture<Int>

    @Query("DELETE FROM diaries")
    fun deleteAllDiaries(): ListenableFuture<Int>
}
