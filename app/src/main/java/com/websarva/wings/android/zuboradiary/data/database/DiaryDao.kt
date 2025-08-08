package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface DiaryDao {
    // MEMO:@Query使用方法下記参照
    //      https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diaries")
    suspend fun countDiaries(): Int

    @Query("SELECT COUNT(*) FROM diaries WHERE date < :startDate")
    suspend fun countDiaries(startDate: String): Int

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    suspend fun existsDiary(date: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE image_uri = :uriString)")
    suspend fun existsImageUri(uriString: String): Boolean

    @Query("SELECT * FROM diaries WHERE date = :date")
    suspend fun selectDiary(date: String): DiaryEntity?

    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    suspend fun selectNewestDiary(): DiaryEntity?

    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    suspend fun selectOldestDiary(): DiaryEntity?

    @Query("SELECT date, title, image_uri FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int
    ): List<DiaryListItemData>

    @Query("SELECT date, title, image_uri FROM diaries WHERE date <= :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int,
        startDate: String
    ): List<DiaryListItemData>

    @Query(
        ("SELECT COUNT(*) " +
                "FROM diaries " +
                "WHERE title GLOB '*' || :word || '*' " +
                "OR item_1_title GLOB '*' || :word || '*'" +
                "OR item_1_comment GLOB '*' || :word || '*'" +
                "OR item_2_title GLOB '*' || :word || '*'" +
                "OR item_2_comment GLOB '*' || :word || '*'" +
                "OR item_3_title GLOB '*' || :word || '*'" +
                "OR item_3_comment GLOB '*' || :word || '*'" +
                "OR item_4_title GLOB '*' || :word || '*'" +
                "OR item_4_comment GLOB '*' || :word || '*'" +
                "OR item_5_title GLOB '*' || :word || '*'" +
                "OR item_5_comment GLOB '*' || :word || '*'")
    )
    suspend fun countWordSearchResults(word: String): Int

    @Query(
        ("SELECT date, title, item_1_title, item_1_comment, " +
                "item_2_title, item_2_comment, " +
                "item_3_title, item_3_comment, " +
                "item_4_title, item_4_comment, " +
                "item_5_title, item_5_comment " +
                "FROM diaries " +
                "WHERE title GLOB '*' || :word || '*' " +
                "OR item_1_title GLOB '*' || :word || '*'" +
                "OR item_1_comment GLOB '*' || :word || '*'" +
                "OR item_2_title GLOB '*' || :word || '*'" +
                "OR item_2_comment GLOB '*' || :word || '*'" +
                "OR item_3_title GLOB '*' || :word || '*'" +
                "OR item_3_comment GLOB '*' || :word || '*'" +
                "OR item_4_title GLOB '*' || :word || '*'" +
                "OR item_4_comment GLOB '*' || :word || '*'" +
                "OR item_5_title GLOB '*' || :word || '*'" +
                "OR item_5_comment GLOB '*' || :word || '*'" +
                "ORDER BY date DESC LIMIT :num OFFSET :offset")
    )
    suspend fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        word: String
    ): List<WordSearchResultListItemData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diaryEntity: DiaryEntity)

    @Query("DELETE FROM diaries WHERE date = :date")
    suspend fun deleteDiary(date: String)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()
}
