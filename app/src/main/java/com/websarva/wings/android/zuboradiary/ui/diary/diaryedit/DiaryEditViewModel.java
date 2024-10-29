package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiCallable;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;

@HiltViewModel
public class DiaryEditViewModel extends BaseViewModel {
    private final DiaryRepository diaryRepository;
    private final WeatherApiRepository weatherApiRepository;

    // 日記データ関係
    private boolean hasPreparedDiary;
    private final MutableLiveData<LocalDate> previousDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> loadedDate = new MutableLiveData<>();
    private final DiaryLiveData diaryLiveData;

    @Inject
    public DiaryEditViewModel(DiaryRepository diaryRepository, WeatherApiRepository weatherApiRepository) {
        this.diaryRepository = diaryRepository;
        this.weatherApiRepository = weatherApiRepository;
        diaryLiveData = new DiaryLiveData();
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        hasPreparedDiary = false;
        previousDate.setValue(null);
        loadedDate.setValue(null);
        diaryLiveData.initialize();
    }

    void prepareDiary(LocalDate date, boolean requestsLoadingDiary) {
        Objects.requireNonNull(date);

        if (requestsLoadingDiary) {
            try {
                loadDiary(date);
            } catch (CancellationException | ExecutionException | InterruptedException | NoSuchElementException e) {
                addAppError(AppError.DIARY_LOADING);
                return;
            }
        } else {
            updateDate(date);
        }
        hasPreparedDiary = true;
    }

    private void loadDiary(LocalDate date)
            throws CancellationException, ExecutionException, InterruptedException,NoSuchElementException {
        Objects.requireNonNull(date);

        Diary diary = diaryRepository.selectDiary(date).get();
        if (diary == null) throw new NoSuchElementException(); // TODO:DiaryRepositoryで例外をスローしたい（戻り値にListenableFutureをやめる？）

        // HACK:下記はDiaryLiveData#update()処理よりも前に処理すること。
        //      (後で処理するとDiaryLiveDataのDateのObserverがloadedDateの更新よりも先に処理される為)
        loadedDate.setValue(date);

        diaryLiveData.update(diary);
    }

    boolean isNewDiaryDefaultStatus() {
        LocalDate previousDate = this.previousDate.getValue();
        LocalDate loadedDate = this.loadedDate.getValue();

        return hasPreparedDiary && previousDate == null && loadedDate == null;
    }

    boolean hasDiary(LocalDate localDate) {
        try {
            return diaryRepository.hasDiary(localDate).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_LOADING);
            return false;
        }
    }

    // TODO:TestDiariesSaverクラス削除後、public削除。
    public boolean saveDiary() {
        Diary diary = diaryLiveData.createDiary();
        List<DiaryItemTitleSelectionHistoryItem> diaryItemTitleSelectionHistoryItemList =
                diaryLiveData.createDiaryItemTitleSelectionHistoryItemList();
        try {
            if (shouldDeleteLoadedDateDiary()) {
                diaryRepository
                        .deleteAndInsertDiary(loadedDate.getValue(), diary, diaryItemTitleSelectionHistoryItemList)
                        .get();
            } else {
                diaryRepository.insertDiary(diary, diaryItemTitleSelectionHistoryItemList).get();
            }
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_SAVING);
            return false;
        }
        return true;
    }

    // TODO:下記いらないかも？
    boolean isNewDiary() {
        return loadedDate.getValue() == null;
    }

    boolean shouldDeleteLoadedDateDiary() {
        if (isNewDiary()) return false;
        return !isLoadedDateEqualToInputDate();
    }

    boolean shouldShowUpdateConfirmationDialog() {
        if (isLoadedDateEqualToInputDate()) return false;

        LocalDate inputDate = diaryLiveData.getDateMutableLiveData().getValue();
        return hasDiary(inputDate);
    }

    boolean isLoadedDateEqualToInputDate() {
        LocalDate loadedDate = this.loadedDate.getValue();
        if (loadedDate == null) return false;
        LocalDate inputDate = diaryLiveData.getDateMutableLiveData().getValue();
        Objects.requireNonNull(inputDate);
        return loadedDate.equals(inputDate);
    }

    boolean deleteDiary() {
        LocalDate deleteDate = loadedDate.getValue();
        Objects.requireNonNull(deleteDate);

        try {
            diaryRepository.deleteDiary(deleteDate).get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_DELETE);
            return false;
        }
        return true;
    }

    // 日付関係
    // TODO:TestDiariesSaverクラス削除後、public削除。
    public void updateDate(LocalDate date) {
        Objects.requireNonNull(date);

        LocalDate previousDate = diaryLiveData.getDateMutableLiveData().getValue();

        // HACK:下記はDiaryLiveDataのDateのsetValue()処理よりも前に処理すること。
        //      (後で処理するとDateのObserverがpreviousDateの更新よりも先に処理される為)
        this.previousDate.setValue(previousDate);

        diaryLiveData.getDateMutableLiveData().setValue(date);
    }

    // 天気、体調関係
    // MEMO:Weathers、Conditionsから文字列に変換するにはContextが必要なため、
    //      Fragment上のLivedDateObserverにて変換した値を受け取る。
    void updateWeather1(Weathers weather) {
        Objects.requireNonNull(weather);

        diaryLiveData.getWeather1MutableLiveData().setValue(weather);
    }

    void updateWeather2(Weathers weather) {
        Objects.requireNonNull(weather);

        diaryLiveData.getWeather2MutableLiveData().setValue(weather);
    }

    boolean isEqualWeathers() {
        Weathers weather1 = diaryLiveData.getWeather1MutableLiveData().getValue();
        Weathers weather2 = diaryLiveData.getWeather2MutableLiveData().getValue();
        Objects.requireNonNull(weather1);
        Objects.requireNonNull(weather2);

        return weather1.equals(weather2);
    }

    void updateCondition(Conditions condition) {
        Objects.requireNonNull(condition);

        diaryLiveData.getConditionMutableLiveData().setValue(condition);
    }

    boolean canFetchWeatherInformation(LocalDate date) {
        Objects.requireNonNull(date);

        return weatherApiRepository.canFetchWeatherInfo(date);
    }

    // 天気情報関係
    void fetchWeatherInformation(LocalDate date, GeoCoordinates geoCoordinates) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(geoCoordinates);
        if (!canFetchWeatherInformation(date)) return;

        LocalDate currentDate = LocalDate.now();
        long betweenDays = ChronoUnit.DAYS.between(date, currentDate);
        Call<WeatherApiResponse> weatherApiResponseCall;
        if (betweenDays == 0) {
            weatherApiResponseCall = weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates);
        } else {
            weatherApiResponseCall =
                    weatherApiRepository.fetchPastDayWeatherInfo(geoCoordinates, (int) betweenDays);
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(new CustomWeatherApiCallable(weatherApiResponseCall)).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppError(AppError.WEATHER_INFORMATION_LOADING);
        }

    }

    private class CustomWeatherApiCallable extends WeatherApiCallable {

        public CustomWeatherApiCallable(Call<WeatherApiResponse> weatherApiResponseCall) {
            super(weatherApiResponseCall);
        }

        @Override
        public void onResponse(Weathers weather) {
            diaryLiveData.getWeather1MutableLiveData().postValue(weather);
        }

        @Override
        public void onFailure() {
            addAppError(AppError.WEATHER_INFORMATION_LOADING);
        }

        @Override
        public void onException(Exception e) {
            addAppError(AppError.WEATHER_INFORMATION_LOADING);
        }
    }

    // 項目関係
    void incrementVisibleItemsCount() {
        diaryLiveData.incrementVisibleItemsCount();
    }

    void deleteItem(int itemNumber) {
        diaryLiveData.deleteItem(itemNumber);
    }

    void updateItemTitle(int itemNumber, String title) {
        Objects.requireNonNull(title);

        diaryLiveData.updateItemTitle(itemNumber, title);
    }

    // Getter
    boolean getHasPreparedDiary() {
        return hasPreparedDiary;
    }

    // LiveDataGetter
    @NonNull
    LiveData<LocalDate> getPreviousDateLiveData() {
        return previousDate;
    }

    @NonNull
    LiveData<LocalDate> getLoadedDateLiveData() {
        return loadedDate;
    }

    @NonNull
    public LiveData<LocalDate> getDateLiveData() {
        return diaryLiveData.getDateMutableLiveData();
    }

    @NonNull
    LiveData<Weathers> getWeather1LiveData() {
        return diaryLiveData.getWeather1MutableLiveData();
    }

    @NonNull
    LiveData<Weathers> getWeather2LiveData() {
        return diaryLiveData.getWeather2MutableLiveData();
    }

    @NonNull
    LiveData<Conditions> getConditionLiveData() {
        return diaryLiveData.getConditionMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getTitleMutableLiveData() {
        return diaryLiveData.getTitleMutableLiveData();
    }

    @NonNull
    LiveData<Integer> getNumVisibleItemsLiveData() {
        return diaryLiveData.getNumVisibleItemsMutableLiveData();
    }

    @NonNull
    LiveData<String> getItemTitleLiveData(int itemNumber) {
        return diaryLiveData.getItemLiveData(itemNumber).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem1TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(1).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem2TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(2).getTitleMutableLiveData();
    }


    @NonNull
    public MutableLiveData<String> getItem3TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(3).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem4TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(4).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem5TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(5).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem1CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(1).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem2CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(2).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem3CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(3).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem4CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(4).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem5CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(5).getCommentMutableLiveData();
    }

}
