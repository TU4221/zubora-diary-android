package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.list.DiaryListItem;
import com.websarva.wings.android.zuboradiary.ui.list.wordsearch.WordSearchResultListItemDiary;

import java.util.List;

@Dao
public interface DiaryDAO {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query
    @Query("SELECT COUNT(*) FROM diaries")
    public ListenableFuture<Integer> countDiaries();
    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    public ListenableFuture<Boolean> hasDiary(String date);

    @Query("SELECT * FROM diaries WHERE date = :date")
    public ListenableFuture<Diary> selectDiary(String date);

    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    public ListenableFuture<Diary> selectNewestDiary();

    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    public ListenableFuture<Diary> selectOldestDiary();

    @Query("SELECT date, title, imagePath FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    public ListenableFuture<List<DiaryListItem>> selectDiaryList(int num, int offset);

    @Query("SELECT date, title, imagePath FROM diaries WHERE date < :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    public ListenableFuture<List<DiaryListItem>> selectDiaryList(int num, int offset , String startDate);

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
    public ListenableFuture<Integer> countWordSearchResults(String word);

    @Query("SELECT date, title, item_1_title, item_1_comment, " +
                "item_2_title, item_2_comment " +
                "item_3_title, item_3_comment " +
                "item_4_title, item_4_comment " +
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
    public ListenableFuture<List<WordSearchResultListItemDiary>> selectWordSearchResultList(int num, int offset, String word);

    @Query("SELECT date FROM diaries WHERE date LIKE :dateYearMonth || '%'") // ||：文字連結
    public ListenableFuture<List<String>> selectDiaryDateList(String dateYearMonth);

    @Insert
    public ListenableFuture<Long> insertDiary(Diary diary);

    @Update
    public ListenableFuture<Integer> updateDiary(Diary diary);

    @Query("DELETE FROM diaries WHERE date = :date")
    public ListenableFuture<Integer> deleteDiary(String date);

}
