package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;
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

    public boolean existsSavedDiary(LocalDate date) {
        Objects.requireNonNull(date);

        try {
            return diaryRepository.existsDiary(date).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR);
            return false;
        }
    }

    public void loadSavedDiary(LocalDate date) {
        Objects.requireNonNull(date);

        try {
            DiaryEntity diaryEntity = diaryRepository.loadDiary(date).get();
            diaryLiveData.update(diaryEntity);
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR);
        }
    }

    boolean deleteDiary() {
        LocalDate deleteDate = diaryLiveData.getDateMutableLiveData().getValue();
        Objects.requireNonNull(deleteDate);

        Integer result;
        try {
            result = diaryRepository.deleteDiary(deleteDate).get();
        } catch (CancellationException | ExecutionException | InterruptedException  e) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
            return false;
        }

        // 削除件数 = 1が正常
        if (result != 1) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
            return false;
        }

        return true;
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    boolean checkSavedPicturePathDoesNotExist(Uri uri) {
        Objects.requireNonNull(uri);

        try {
            return !diaryRepository.existsPicturePath(uri).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            return false;
        }
    }

    // LiveDataGetter
    public LiveData<LocalDate> getDateLiveData() {
        return diaryLiveData.getDateMutableLiveData();
    }

    public LiveData<Weather> getWeather1LiveData() {
        return diaryLiveData.getWeather1MutableLiveData();
    }

    public LiveData<Weather> getWeather2LiveData() {
        return diaryLiveData.getWeather2MutableLiveData();
    }

    public LiveData<Condition> getConditionLiveData() {
        return diaryLiveData.getConditionMutableLiveData();
    }

    public LiveData<String> getTitleLiveData() {
        return diaryLiveData.getTitleMutableLiveData();
    }

    public LiveData<Integer> getNumVisibleItemsLiveData() {
        return diaryLiveData.getNumVisibleItemsMutableLiveData();
    }

    public LiveData<String> getItem1TitleLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(1)).getTitleMutableLiveData();
    }

    public LiveData<String> getItem2TitleLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(2)).getTitleMutableLiveData();
    }

    public LiveData<String> getItem3TitleLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(3)).getTitleMutableLiveData();
    }

    public LiveData<String> getItem4TitleLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(4)).getTitleMutableLiveData();
    }

    public LiveData<String> getItem5TitleLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(5)).getTitleMutableLiveData();
    }

    public LiveData<String> getItem1CommentLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(1)).getCommentMutableLiveData();
    }

    public LiveData<String> getItem2CommentLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(2)).getCommentMutableLiveData();
    }

    public LiveData<String> getItem3CommentLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(3)).getCommentMutableLiveData();
    }

    public LiveData<String> getItem4CommentLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(4)).getCommentMutableLiveData();
    }

    public LiveData<String> getItem5CommentLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(5)).getCommentMutableLiveData();
    }

    public LiveData<Uri> getPicturePathLiveData() {
        return diaryLiveData.getPicturePathMutableLiveData();
    }

    public LiveData<LocalDateTime> getLogLiveData() {
        return diaryLiveData.getLogMutableLiveData();
    }
}
