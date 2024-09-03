package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryShowViewModel extends BaseViewModel {
    private final DiaryRepository diaryRepository;
    private final SettingsRepository settingsRepository;

    // 日記データ関係
    DiaryLiveData diaryLiveData;

    @Inject
    public DiaryShowViewModel(DiaryRepository diaryRepository, SettingsRepository settingsRepository) {
        this.diaryRepository = diaryRepository;
        this.settingsRepository = settingsRepository;
        diaryLiveData = new DiaryLiveData();
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        diaryLiveData.initialize();
    }

    public boolean hasDiary(LocalDate localDate) {
        try {
            return diaryRepository.hasDiary(localDate).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_INFORMATION_LOADING);
            return false;
        }
    }

    public void loadDiary(LocalDate date) {
        Diary diary;
        try {
             diary = diaryRepository.selectDiary(date).get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_LOADING);
            return;
        }
        diaryLiveData.getDate().setValue(LocalDate.parse(diary.getDate()));
        WeatherConverter weatherConverter = new WeatherConverter();
        Integer intWeather1 = getOrDefault(diary.getWeather1(), 0);
        diaryLiveData.getWeather1().setValue(weatherConverter.toWeather(intWeather1)); // Fragmentに記述したObserverよりintWeather1更新
        Integer intWeather2 = getOrDefault(diary.getWeather2(), 0);
        diaryLiveData.getWeather2().setValue(weatherConverter.toWeather(intWeather2)); // Fragmentに記述したObserverよりintWeather2更新
        ConditionConverter conditionConverter = new ConditionConverter();
        Integer intCondition = getOrDefault(diary.getCondition(), 0);
        diaryLiveData.getCondition().setValue(conditionConverter.toCondition(intCondition)); // Fragmentに記述したObserverよりintCondition更新
        String title = getOrDefault(diary.getTitle(), "");
        diaryLiveData.getTitle().setValue(title);
        String item1Title = getOrDefault(diary.getItem1Title(), "");
        diaryLiveData.getItem(1).getTitle().setValue(item1Title);
        String item1Comment = getOrDefault(diary.getItem1Comment(), "");
        diaryLiveData.getItem(1).getComment().setValue(item1Comment);
        String item2Title = getOrDefault(diary.getItem2Title(), "");
        diaryLiveData.getItem(2).getTitle().setValue(item2Title);
        String item2Comment = getOrDefault(diary.getItem2Comment(), "");
        diaryLiveData.getItem(2).getComment().setValue(item2Comment);
        String item3Title = getOrDefault(diary.getItem3Title(), "");
        diaryLiveData.getItem(3).getTitle().setValue(item3Title);
        String item3Comment = getOrDefault(diary.getItem3Comment(), "");
        diaryLiveData.getItem(3).getComment().setValue(item3Comment);
        String item4Title = getOrDefault(diary.getItem4Title(), "");
        diaryLiveData.getItem(4).getTitle().setValue(item4Title);
        String item4Comment = getOrDefault(diary.getItem4Comment(), "");
        diaryLiveData.getItem(4).getComment().setValue(item4Comment);
        String item5Title = getOrDefault(diary.getItem5Title(), "");
        diaryLiveData.getItem(5).getTitle().setValue(item5Title);
        String item5Comment = getOrDefault(diary.getItem5Comment(), "");
        diaryLiveData.getItem(5).getComment().setValue(item5Comment);
        int numVisibleItems = DiaryLiveData.MAX_ITEMS;
        for (int i = DiaryLiveData.MAX_ITEMS; i > 1; i--) {
            String itemTitle = diaryLiveData.getItem(i).getTitle().getValue();
            String itemComment = diaryLiveData.getItem(i).getComment().getValue();
            if ((itemTitle == null || itemTitle.isEmpty())
                    && (itemComment == null || itemComment.isEmpty())) {
                numVisibleItems--;
            } else {
                break;
            }
        }
        diaryLiveData.getNumVisibleItems().setValue(numVisibleItems);
        diaryLiveData.getLog().setValue(LocalDateTime.parse(diary.getLog()));
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public boolean deleteDiary() {
        LocalDate deleteDate = diaryLiveData.getDate().getValue();
        if (deleteDate == null) {
            return false;
        }
        try {
            diaryRepository.deleteDiary(deleteDate).get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_DELETE);
            return false;
        }
        return true;
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

    public LiveData<String> getItemTitleLiveData(int itemNumber) {
        return diaryLiveData.getItem(itemNumber).getTitle();
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

    public LiveData<String> getItemCommentLiveData(int itemNumber) {
        return diaryLiveData.getItem(itemNumber).getComment();
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
