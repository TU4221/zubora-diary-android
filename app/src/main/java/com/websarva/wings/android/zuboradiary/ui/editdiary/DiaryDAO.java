package com.websarva.wings.android.zuboradiary.ui.editdiary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.ui.list.ListItemDiary;

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

    @Query("SELECT date, title, imagePath FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    public ListenableFuture<List<ListItemDiary>> selectDiaryList(int num, int offset);

    @Query("SELECT date, title, imagePath FROM diaries WHERE date < :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    public ListenableFuture<List<ListItemDiary>> selectDiaryList(int num, int offset , String startDate);

    @Query("SELECT date FROM diaries WHERE date Like :dateYearMonth || '%'") // ||：文字連結
    public ListenableFuture<List<String>> selectDiaryDateList(String dateYearMonth);

    @Insert
    public ListenableFuture<Long> insertDiary(Diary diary);

    @Update
    public ListenableFuture<Integer> updateDiary(Diary diary);

    @Query("DELETE FROM diaries WHERE date = :date")
    public ListenableFuture<Integer> deleteDiary(String date);

}
