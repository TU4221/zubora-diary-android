package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import androidx.lifecycle.LiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryShowViewModel extends BaseViewModel {

    private final DiaryRepository diaryRepository;

    // 日記データ関係
    private final DiaryLiveData diaryLiveData;

    @Inject
    public DiaryShowViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        diaryLiveData = new DiaryLiveData();
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        diaryLiveData.initialize();
    }

    public boolean hasDiary(LocalDate date) {
        Objects.requireNonNull(date);

        try {
            return diaryRepository.hasDiary(date).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_INFORMATION_LOADING);
            return false;
        }
    }

    public void loadDiary(LocalDate date) {
        Objects.requireNonNull(date);

        try {
            Diary diary = diaryRepository.selectDiary(date).get();
            diaryLiveData.update(diary);
        } catch (Exception e) {
            addAppError(AppError.DIARY_LOADING);
        }
    }

    public void deleteDiary() {
        LocalDate deleteDate = diaryLiveData.getDateMutableLiveData().getValue();
        Objects.requireNonNull(deleteDate);

        try {
            diaryRepository.deleteDiary(deleteDate).get();
        } catch (CancellationException | ExecutionException | InterruptedException  e) {
            addAppError(AppError.DIARY_DELETE);
        }
    }

    // LiveDataGetter
    public LiveData<LocalDate> getDateLiveData() {
        return diaryLiveData.getDateMutableLiveData();
    }

    public LiveData<Weathers> getWeather1LiveData() {
        return diaryLiveData.getWeather1MutableLiveData();
    }

    public LiveData<Weathers> getWeather2LiveData() {
        return diaryLiveData.getWeather2MutableLiveData();
    }

    public LiveData<Conditions> getConditionLiveData() {
        return diaryLiveData.getConditionMutableLiveData();
    }

    public LiveData<String> getTitleLiveData() {
        return diaryLiveData.getTitleMutableLiveData();
    }

    public LiveData<Integer> getNumVisibleItemsLiveData() {
        return diaryLiveData.getNumVisibleItemsMutableLiveData();
    }

    public LiveData<String> getItem1TitleLiveData() {
        return diaryLiveData.getItemLiveData(1).getTitleMutableLiveData();
    }

    public LiveData<String> getItem2TitleLiveData() {
        return diaryLiveData.getItemLiveData(2).getTitleMutableLiveData();
    }

    public LiveData<String> getItem3TitleLiveData() {
        return diaryLiveData.getItemLiveData(3).getTitleMutableLiveData();
    }

    public LiveData<String> getItem4TitleLiveData() {
        return diaryLiveData.getItemLiveData(4).getTitleMutableLiveData();
    }

    public LiveData<String> getItem5TitleLiveData() {
        return diaryLiveData.getItemLiveData(5).getTitleMutableLiveData();
    }

    public LiveData<String> getItem1CommentLiveData() {
        return diaryLiveData.getItemLiveData(1).getCommentMutableLiveData();
    }

    public LiveData<String> getItem2CommentLiveData() {
        return diaryLiveData.getItemLiveData(2).getCommentMutableLiveData();
    }

    public LiveData<String> getItem3CommentLiveData() {
        return diaryLiveData.getItemLiveData(3).getCommentMutableLiveData();
    }

    public LiveData<String> getItem4CommentLiveData() {
        return diaryLiveData.getItemLiveData(4).getCommentMutableLiveData();
    }

    public LiveData<String> getItem5CommentLiveData() {
        return diaryLiveData.getItemLiveData(5).getCommentMutableLiveData();
    }

    public LiveData<LocalDateTime> getLogLiveData() {
        return diaryLiveData.getLogMutableLiveData();
    }
}
