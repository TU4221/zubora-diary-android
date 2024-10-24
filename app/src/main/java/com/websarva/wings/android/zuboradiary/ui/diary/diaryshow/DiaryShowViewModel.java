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
        LocalDate deleteDate = diaryLiveData.getDate().getValue();
        Objects.requireNonNull(deleteDate);

        try {
            diaryRepository.deleteDiary(deleteDate).get();
        } catch (CancellationException | ExecutionException | InterruptedException  e) {
            addAppError(AppError.DIARY_DELETE);
        }
    }

    // LiveDataGetter
    public LiveData<LocalDate> getDateLiveData() {
        return diaryLiveData.getDate();
    }

    public LiveData<Weathers> getWeather1LiveData() {
        return diaryLiveData.getWeather1();
    }

    public LiveData<Weathers> getWeather2LiveData() {
        return diaryLiveData.getWeather2();
    }

    public LiveData<Conditions> getConditionLiveData() {
        return diaryLiveData.getCondition();
    }

    public LiveData<String> getTitleLiveData() {
        return diaryLiveData.getTitle();
    }

    public LiveData<Integer> getNumVisibleItemsLiveData() {
        return diaryLiveData.getNumVisibleItems();
    }

    public LiveData<String> getItem1TitleLiveData() {
        return diaryLiveData.getItem(1).getTitle();
    }

    public LiveData<String> getItem2TitleLiveData() {
        return diaryLiveData.getItem(2).getTitle();
    }

    public LiveData<String> getItem3TitleLiveData() {
        return diaryLiveData.getItem(3).getTitle();
    }

    public LiveData<String> getItem4TitleLiveData() {
        return diaryLiveData.getItem(4).getTitle();
    }

    public LiveData<String> getItem5TitleLiveData() {
        return diaryLiveData.getItem(5).getTitle();
    }

    public LiveData<String> getItem1CommentLiveData() {
        return diaryLiveData.getItem(1).getComment();
    }

    public LiveData<String> getItem2CommentLiveData() {
        return diaryLiveData.getItem(2).getComment();
    }

    public LiveData<String> getItem3CommentLiveData() {
        return diaryLiveData.getItem(3).getComment();
    }

    public LiveData<String> getItem4CommentLiveData() {
        return diaryLiveData.getItem(4).getComment();
    }

    public LiveData<String> getItem5CommentLiveData() {
        return diaryLiveData.getItem(5).getComment();
    }

    public LiveData<LocalDateTime> getLogLiveData() {
        return diaryLiveData.getLog();
    }
}
