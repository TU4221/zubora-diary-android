package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultListItemDiary;

import java.util.List;

@Dao
public interface DiaryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diaries")
    ListenableFuture<Integer> countDiariesAsync();
    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    ListenableFuture<Boolean> hasDiaryAsync(String date);

    @Query("SELECT * FROM diaries WHERE date = :date")
    ListenableFuture<Diary> selectDiaryAsync(String date);

    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    ListenableFuture<Diary> selectNewestDiaryAsync();

    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    ListenableFuture<Diary> selectOldestDiaryAsync();

    @Query("SELECT date, title, picturePath FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    ListenableFuture<List<DiaryListItem>> selectDiaryListAsync(int num, int offset);

    @Query("SELECT date, title, picturePath FROM diaries WHERE date < :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    ListenableFuture<List<DiaryListItem>> selectDiaryListAsync(int num, int offset , String startDate);

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
    ListenableFuture<Integer> countWordSearchResultsAsync(String word);

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
    ListenableFuture<List<WordSearchResultListItemDiary>> selectWordSearchResultListAsync(int num, int offset, String word);

    @Query("SELECT date FROM diaries WHERE date LIKE :dateYearMonth || '%'") // ||：文字連結
    ListenableFuture<List<String>> selectDiaryDateListAsync(String dateYearMonth);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertDiaryAsync(Diary diary);

    // 他DAO(他テーブルへの書き込み処理)メソッドと同じタイミング(Transaction)で処理する時に使用
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertDiary(Diary diary);

    @Query("DELETE FROM diaries WHERE date = :date")
    ListenableFuture<Integer> deleteDiaryAsync(String date);

    // 他DAO(他テーブルへの書き込み処理)メソッドと同じタイミング(Transaction)で処理する時に使用
    @Query("DELETE FROM diaries WHERE date = :date")
    void deleteDiary(String date);

}
