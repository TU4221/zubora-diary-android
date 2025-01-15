package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiCallable;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse;
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
    private final MutableLiveData<Uri> loadedPicturePath = new MutableLiveData<>();
    private final DiaryLiveData diaryLiveData;

    // Fragment切替記憶
    private boolean isShowingItemTitleEditFragment;

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
        loadedPicturePath.setValue(null);
        diaryLiveData.initialize();
        isShowingItemTitleEditFragment = false;
    }

    void prepareDiary(LocalDate date, boolean requiresDiaryLoading) {
        Objects.requireNonNull(date);

        if (requiresDiaryLoading) {
            try {
                loadSavedDiary(date);
            } catch (NoSuchElementException e) {
                updateDate(date);
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                addAppMessage(AppMessage.DIARY_LOADING_ERROR);
                return;
            }
        } else {
            updateDate(date);
        }
        hasPreparedDiary = true;
    }

    private void loadSavedDiary(LocalDate date)
            throws CancellationException, ExecutionException, InterruptedException,NoSuchElementException {
        Objects.requireNonNull(date);

        DiaryEntity diaryEntity = diaryRepository.loadDiary(date).get();
        if (diaryEntity == null) throw new NoSuchElementException();

        // HACK:下記はDiaryLiveData#update()処理よりも前に処理すること。
        //      (後で処理するとDiaryLiveDataのDateのObserverがloadedDateの更新よりも先に処理される為)
        loadedDate.setValue(date);

        diaryLiveData.update(diaryEntity);
        loadedPicturePath.setValue(diaryLiveData.getPicturePathMutableLiveData().getValue());
    }

    boolean isNewDiaryDefaultStatus() {
        LocalDate previousDate = this.previousDate.getValue();
        LocalDate loadedDate = this.loadedDate.getValue();

        return hasPreparedDiary && previousDate == null && loadedDate == null;
    }

    boolean existsSavedDiary(LocalDate date) {
        Objects.requireNonNull(date);

        try {
            return diaryRepository.existsDiary(date).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR);
            return false;
        }
    }

    // TODO:TestDiariesSaverクラス削除後、public削除。
    public boolean saveDiary() {
        DiaryEntity diaryEntity = diaryLiveData.createDiaryEntity();
        List<DiaryItemTitleSelectionHistoryItemEntity> diaryItemTitleSelectionHistoryItemEntityList =
                diaryLiveData.createDiaryItemTitleSelectionHistoryItemEntityList();
        try {
            if (shouldDeleteLoadedDateDiary()) {
                diaryRepository
                        .deleteAndSaveDiary(
                                loadedDate.getValue(),
                                diaryEntity,
                                diaryItemTitleSelectionHistoryItemEntityList
                        )
                        .get();
            } else {
                diaryRepository
                        .saveDiary(diaryEntity, diaryItemTitleSelectionHistoryItemEntityList).get();
            }
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_SAVING_ERROR);
            return false;
        }
        return true;
    }

    private boolean isNewDiary() {
        return loadedDate.getValue() == null;
    }

    boolean shouldDeleteLoadedDateDiary() {
        if (isNewDiary()) return false;
        return !isLoadedDateEqualToInputDate();
    }

    boolean shouldShowUpdateConfirmationDialog() {
        if (isLoadedDateEqualToInputDate()) return false;

        LocalDate inputDate = diaryLiveData.getDateMutableLiveData().getValue();
        return existsSavedDiary(inputDate);
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

        Integer result;
        try {
            result = diaryRepository.deleteDiary(deleteDate).get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
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
    // MEMO:Weather、Conditionsから文字列に変換するにはContextが必要なため、
    //      Fragment上のLivedDateObserverにて変換した値を受け取る。
    void updateWeather1(Weather weather) {
        Objects.requireNonNull(weather);

        diaryLiveData.getWeather1MutableLiveData().setValue(weather);
    }

    void updateWeather2(Weather weather) {
        Objects.requireNonNull(weather);

        diaryLiveData.getWeather2MutableLiveData().setValue(weather);
    }

    boolean isEqualWeathers() {
        Weather weather1 = diaryLiveData.getWeather1MutableLiveData().getValue();
        Weather weather2 = diaryLiveData.getWeather2MutableLiveData().getValue();
        Objects.requireNonNull(weather1);
        Objects.requireNonNull(weather2);

        return weather1.equals(weather2);
    }

    void updateCondition(Condition condition) {
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
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR);
        }

    }

    private class CustomWeatherApiCallable extends WeatherApiCallable {

        public CustomWeatherApiCallable(Call<WeatherApiResponse> weatherApiResponseCall) {
            super(weatherApiResponseCall);
        }

        @Override
        public void onResponse(@NonNull Weather weather) {
            diaryLiveData.getWeather1MutableLiveData().postValue(weather);
        }

        @Override
        public void onFailure() {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR);
        }

        @Override
        public void onException(@NonNull Exception e) {
            addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR);
        }
    }

    // 項目関係
    void incrementVisibleItemsCount() {
        diaryLiveData.incrementVisibleItemsCount();
    }

    void deleteItem(ItemNumber itemNumber) {
        Objects.requireNonNull(itemNumber);

        diaryLiveData.deleteItem(itemNumber);
    }

    void updateItemTitle(ItemNumber itemNumber, String title) {
        Objects.requireNonNull(title);

        diaryLiveData.updateItemTitle(itemNumber, title);
    }

    void updatePicturePath(Uri uri) {
        Objects.requireNonNull(uri);

        diaryLiveData.getPicturePathMutableLiveData().setValue(uri);
    }

    void deletePicturePath() {
        diaryLiveData.getPicturePathMutableLiveData().setValue(null);
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

    // Fragment切替記憶
    void updateIsShowingItemTitleEditFragment(boolean isShowing) {
        isShowingItemTitleEditFragment = isShowing;
    }

    void addWeatherInfoFetchErrorMessage() {
        addAppMessage(AppMessage.WEATHER_INFO_LOADING_ERROR);
    }

    // Getter
    boolean getHasPreparedDiary() {
        return hasPreparedDiary;
    }

    boolean getIsShowingItemTitleEditFragment() {
        return isShowingItemTitleEditFragment;
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
    LiveData<Weather> getWeather1LiveData() {
        return diaryLiveData.getWeather1MutableLiveData();
    }

    @NonNull
    LiveData<Weather> getWeather2LiveData() {
        return diaryLiveData.getWeather2MutableLiveData();
    }

    @NonNull
    LiveData<Condition> getConditionLiveData() {
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
    LiveData<String> getItemTitleLiveData(ItemNumber itemNumber) {
        return diaryLiveData.getItemLiveData(itemNumber).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem1TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(1)).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem2TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(2)).getTitleMutableLiveData();
    }


    @NonNull
    public MutableLiveData<String> getItem3TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(3)).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem4TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(4)).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem5TitleMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(5)).getTitleMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem1CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(1)).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem2CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(2)).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem3CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(3)).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem4CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(4)).getCommentMutableLiveData();
    }

    @NonNull
    public MutableLiveData<String> getItem5CommentMutableLiveData() {
        return diaryLiveData.getItemLiveData(new ItemNumber(5)).getCommentMutableLiveData();
    }

    @NonNull
    LiveData<Uri> getPicturePathLiveData() {
        return diaryLiveData.getPicturePathMutableLiveData();
    }

    @NonNull
    LiveData<Uri> getLoadedPicturePathLiveData() {
        return loadedPicturePath;
    }
}
