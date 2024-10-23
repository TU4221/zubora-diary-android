package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.WeatherCodeConverter;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.preferences.SettingsRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@HiltViewModel
public class DiaryEditViewModel extends BaseViewModel {
    private final DiaryRepository diaryRepository;
    private final WeatherApiRepository weatherApiRepository;
    private final SettingsRepository settingsRepository;

    // 日記データ関係
    private boolean hasPreparedDiary;
    private final MutableLiveData<LocalDate> loadedDate = new MutableLiveData<>();
    DiaryLiveData diaryLiveData;

    // 天気情報関係
    private Call<WeatherApiResponse> weatherApiResponseCall;
    private boolean isCancelWeatherApiResponseCall;

    @Inject
    public DiaryEditViewModel(
            DiaryRepository diaryRepository,
            WeatherApiRepository weatherApiRepository,
            SettingsRepository settingsRepository) {
        this.diaryRepository = diaryRepository;
        this.weatherApiRepository = weatherApiRepository;
        this.settingsRepository = settingsRepository;
        diaryLiveData = new DiaryLiveData();
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        hasPreparedDiary = false;
        loadedDate.setValue(null);
        diaryLiveData.initialize();
    }

    public void prepareDiary(LocalDate date, boolean isLoadingDiary) {
        if (isLoadingDiary && hasDiary(date)) {
            try {
                loadDiary(date);
            } catch (Exception e) {
                addAppError(AppError.DIARY_LOADING);
                return;
            }
        } else {
            updateDate(date);
        }
        hasPreparedDiary = true;
    }

    private void loadDiary(LocalDate date) throws Exception {
        Diary diary = diaryRepository.selectDiary(date).get();
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
        loadedDate.setValue(LocalDate.parse(diary.getDate()));
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public boolean hasDiary(LocalDate localDate) {
        try {
            return diaryRepository.hasDiary(localDate).get();
        } catch (ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_LOADING);
            return false;
        }
    }

    public boolean saveDiary() {
        Diary diary = createDiary();
        List<DiaryItemTitleSelectionHistoryItem> diaryItemTitleSelectionHistoryItemList = createSelectedDiaryItemTitleList();
        try {
            diaryRepository.insertDiary(diary, diaryItemTitleSelectionHistoryItemList).get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_SAVING);
            return false;
        }
        return true;
    }

    public boolean deleteExistingDiaryAndSaveDiary() {
        Diary diary = createDiary();
        List<DiaryItemTitleSelectionHistoryItem> diaryItemTitleSelectionHistoryItemList = createSelectedDiaryItemTitleList();
        try {
            diaryRepository
                    .deleteAndInsertDiary(loadedDate.getValue(), diary, diaryItemTitleSelectionHistoryItemList)
                    .get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_SAVING);
            return false;
        }
        return true;
    }

    private Diary createDiary() {
        Diary diary = new Diary();
        diary.setDate(toStringOrNull(diaryLiveData.getDate().getValue())); // MEMO:日付カラムはNonNull
        diary.setWeather1(toIntWeather(diaryLiveData.getWeather1().getValue()));
        diary.setWeather2(toIntWeather(diaryLiveData.getWeather2().getValue()));
        diary.setCondition(toIntCondition(diaryLiveData.getCondition().getValue()));
        diary.setTitle(processTrimmed(diaryLiveData.getTitle().getValue()));
        diary.setItem1Title(processTrimmed(diaryLiveData.getItem(1).getTitle().getValue()));
        diary.setItem1Comment(processTrimmed(diaryLiveData.getItem(1).getComment().getValue()));
        diary.setItem2Title(processTrimmed(diaryLiveData.getItem(2).getTitle().getValue()));
        diary.setItem2Comment(processTrimmed(diaryLiveData.getItem(2).getComment().getValue()));
        diary.setItem3Title(processTrimmed(diaryLiveData.getItem(3).getTitle().getValue()));
        diary.setItem3Comment(processTrimmed(diaryLiveData.getItem(3).getComment().getValue()));
        diary.setItem4Title(processTrimmed(diaryLiveData.getItem(4).getTitle().getValue()));
        diary.setItem4Comment(processTrimmed(diaryLiveData.getItem(4).getComment().getValue()));
        diary.setItem5Title(processTrimmed(diaryLiveData.getItem(5).getTitle().getValue()));
        diary.setItem5Comment(processTrimmed(diaryLiveData.getItem(5).getComment().getValue()));
        diary.setPicturePath(processTrimmed(diaryLiveData.getPicturePath().getValue()));
        diary.setLog(LocalDateTime.now().toString());
        return diary;
    }

    private String toStringOrNull(LocalDate localDate) {
        if (localDate == null) {
            return null;
        } else {
            return localDate.toString();
        }
    }

    private Integer toIntWeather(Weathers weather) {
        if (weather == null) {
            return Weathers.UNKNOWN.toWeatherNumber();
        } else {
            return weather.toWeatherNumber();
        }
    }

    private Integer toIntCondition(Conditions condition) {
        if (condition == null) {
            return Conditions.UNKNOWN.toConditionNumber();
        } else {
            return condition.toConditionNumber();
        }
    }

    private String processTrimmed(String s) {
        if (s == null) {
            return "";
        } else {
            return s.trim();
        }
    }

    private List<DiaryItemTitleSelectionHistoryItem> createSelectedDiaryItemTitleList() {
        List<DiaryItemTitleSelectionHistoryItem> list = new ArrayList<>();
        DiaryItemTitleSelectionHistoryItem diaryItemTitleSelectionHistoryItem = new DiaryItemTitleSelectionHistoryItem();
        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            int itemNumber = i + 1;
            String itemTitle = diaryLiveData.getItem(itemNumber).getTitle().getValue();
            LocalDateTime itemTitleUpdateLog = diaryLiveData.getItem(itemNumber).getTitleUpdateLog().getValue();
            if (itemTitle == null || itemTitleUpdateLog == null) {
                continue;
            }
            if (itemTitle.matches("\\S+.*")) {
                diaryItemTitleSelectionHistoryItem.setTitle(itemTitle);
                diaryItemTitleSelectionHistoryItem.setLog(itemTitleUpdateLog.toString());
                list.add(diaryItemTitleSelectionHistoryItem);
                diaryItemTitleSelectionHistoryItem = new DiaryItemTitleSelectionHistoryItem();
            }
        }
        return list;
    }

    public boolean deleteDiary() {
        LocalDate deleteDate = loadedDate.getValue();
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

    // 日付関係
    public void updateDate(LocalDate date) {
        diaryLiveData.getDate().setValue(date);
    }

    // 天気、体調関係
    // MEMO:Weathers、Conditionsから文字列に変換するにはContextが必要なため、
    //      Fragment上のLivedDateObserverにて変換した値を受け取る。
    public void updateWeather1(Weathers weather) {
        diaryLiveData.getWeather1().setValue(weather);
    }

    public void updateWeather2(Weathers weather) {
        diaryLiveData.getWeather2().setValue(weather);
    }

    boolean isEqualWeathers() {
        Weathers weather1 = diaryLiveData.getWeather1().getValue();
        Weathers weather2 = diaryLiveData.getWeather2().getValue();
        Objects.requireNonNull(weather1);
        Objects.requireNonNull(weather2);

        return weather1.equals(weather2);
    }

    public void updateCondition(Conditions condition) {
        diaryLiveData.getCondition().setValue(condition);
    }

    // 天気情報関係
    // TODO:latitude、longitudeをGeoCoordinatesに置換
    public void fetchWeatherInformation(@NonNull LocalDate date, GeoCoordinates geoCoordinates) {
        LocalDate currentDate = LocalDate.now();
        Log.d("fetchWeatherInformation", "isAfter:" + date.isAfter(currentDate));
        if (date.isAfter(currentDate)) {
            return;
        }
        long betweenDays = ChronoUnit.DAYS.between(date, currentDate);
        Log.d("fetchWeatherInformation", "betweenDays:" + betweenDays);
        if (betweenDays > 92) { //過去天気情報取得可能
            return;
        }
        if (betweenDays == 0) {
            weatherApiResponseCall = weatherApiRepository.getTodayWeather(geoCoordinates);
        } else {
            weatherApiResponseCall =
                    weatherApiRepository.getPastDayWeather(geoCoordinates, (int) betweenDays);
        }

        // TODO:非同期処理不要(最終的に削除)
        /*weatherApiResponseCall.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherApiResponse> call, @NonNull Response<WeatherApiResponse> response) {
                Log.d("WeatherApi", "response.code():" + String.valueOf(response.code()));
                Log.d("WeatherApi", "response.message():" + String.valueOf(response.message()));
                Log.d("WeatherApi", "response.body():" + String.valueOf(response.body()));
                WeatherApiResponse weatherApiResponse = response.body();
                if (response.isSuccessful() && weatherApiResponse != null) {
                    Weathers weather =
                            findWeatherInformation(weatherApiResponse);
                    intWeather1.postValue(weather.toWeatherNumber());
                } else {
                    isWeatherLoadingError.postValue(true);
                    Log.d("WeatherApi", "response.code():" + String.valueOf(response.code()));
                    Log.d("WeatherApi", "response.message():" + String.valueOf(response.message()));
                    try {
                        Log.d("WeatherApi", "response.errorBody():" + String.valueOf(response.errorBody().string()));
                    } catch (IOException e) {
                        Log.d("WeatherApi", "Exception:" + String.valueOf(e));
                    }
                }
                resetAfterWeatherApiCompletion();
            }

            @Override
            public void onFailure(@NonNull Call<WeatherApiResponse> call, @NonNull Throwable t) {
                Log.d("WeatherApi", "onFailure()");
                if (!isCancelWeatherApiResponseCall) {
                    isWeatherLoadingError.postValue(true);
                }
                resetAfterWeatherApiCompletion();
            }
        });
        weatherApiResponseCall.cancel(); // TODO:調整後削除
                                             */

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Response<WeatherApiResponse> response;
                try {
                    response = weatherApiResponseCall.execute();
                } catch (IOException e) {
                    addAppError(AppError.WEATHER_INFORMATION_LOADING);
                    return false;
                }
                if (response.isSuccessful()) {
                    Log.d("WeatherApi", "response.code():" + response.code());
                    Log.d("WeatherApi", "response.message():" + response.message());
                    Log.d("WeatherApi", "response.body():" + response.body());
                    WeatherApiResponse weatherApiResponse = response.body();
                    if (response.isSuccessful() && weatherApiResponse != null) {
                        Weathers weather =
                                findWeatherInformation(weatherApiResponse);
                        diaryLiveData.getWeather1().postValue(weather);
                    } else {
                        addAppError(AppError.WEATHER_INFORMATION_LOADING);
                        Log.d("WeatherApi", "response.code():" + response.code());
                        Log.d("WeatherApi", "response.message():" + response.message());
                        try(ResponseBody errorBody = response.errorBody()) {
                            if (errorBody != null) {
                                Log.d("WeatherApi", "response.errorBody():" + errorBody.string());
                            }
                        } catch (IOException e) {
                            Log.d("WeatherApi", "Exception:" + e);
                        }
                    }
                }
                return true;
            }
        });
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e1) {
            addAppError(AppError.WEATHER_INFORMATION_LOADING);
        }
    }

    private Weathers findWeatherInformation(
            @NonNull WeatherApiResponse weatherApiResponse) {
        Log.d("WeatherApi", String.valueOf(weatherApiResponse.getLatitude()));
        Log.d("WeatherApi", String.valueOf(weatherApiResponse.getLongitude()));
        for (String s: weatherApiResponse.getDaily().getTimes()) {
            Log.d("WeatherApi", s);
        }
        for (int i: weatherApiResponse.getDaily().getWeatherCodes()) {
            Log.d("WeatherApi", String.valueOf(i));
        }
        int[] weatherCodes =
                weatherApiResponse.getDaily().getWeatherCodes();
        int weatherCode = weatherCodes[0];
        Log.d("WeatherApi", String.valueOf(weatherCode));
        WeatherCodeConverter converter = new WeatherCodeConverter();
        return converter.convertWeathers(weatherCode);
    }

    private void resetAfterWeatherApiCompletion() {
        isCancelWeatherApiResponseCall = false;
        weatherApiResponseCall = null;
    }

    public void cancelWeatherSelectionPreparation() {
        if (weatherApiResponseCall != null) {
            isCancelWeatherApiResponseCall = true;
            weatherApiResponseCall.cancel();
        }
    }

    // 項目関係
    public void incrementVisibleItemsCount() {
        Integer numVisibleItems = diaryLiveData.getNumVisibleItems().getValue();
        if (numVisibleItems != null) {
            numVisibleItems++;
        }
        diaryLiveData.getNumVisibleItems().setValue(numVisibleItems);
    }

    public void deleteItem(int itemNumber) {
        diaryLiveData.getItem(itemNumber).initialize();
        Integer numVisibleItems = diaryLiveData.getNumVisibleItems().getValue();
        if (numVisibleItems == null) {
            throw new NullPointerException();
        }
        if (itemNumber < numVisibleItems) {
            int nextItemNumber;
            for (int i = itemNumber; i < numVisibleItems; i++) {
                nextItemNumber = i + 1;
                diaryLiveData.getItem(i).update(
                        diaryLiveData.getItem(nextItemNumber).getTitle().getValue(),
                        diaryLiveData.getItem(nextItemNumber).getComment().getValue(),
                        diaryLiveData.getItem(nextItemNumber).getTitleUpdateLog().getValue()
                );
                diaryLiveData.getItem(nextItemNumber).initialize();
            }
        }
        if (numVisibleItems > 1) {
            numVisibleItems -= 1;
        }
        diaryLiveData.getNumVisibleItems().setValue(numVisibleItems);
    }

    public void updateItemTitle(int itemNumber, String title) {
        diaryLiveData.getItem(itemNumber).getTitle().setValue(title);
        diaryLiveData.getItem(itemNumber).getTitleUpdateLog().setValue(LocalDateTime.now());
    }

    // Getter
    public Boolean getHasPreparedDiary() {
        return hasPreparedDiary;
    }

    // LiveDataGetter
    public LiveData<LocalDate> getLoadedDateLiveData() {
        return loadedDate;
    }

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

    public MutableLiveData<String> getTitleMutableLiveData() {
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

    public MutableLiveData<String> getItem1TitleMutableLiveData() {
        return diaryLiveData.getItem(1).getTitle();
    }

    public LiveData<String> getItem2TitleLiveData() {
        return diaryLiveData.getItem(2).getTitle();
    }

    public MutableLiveData<String> getItem2TitleMutableLiveData() {
        return diaryLiveData.getItem(2).getTitle();
    }

    public LiveData<String> getItem3TitleLiveData() {
        return diaryLiveData.getItem(3).getTitle();
    }

    public MutableLiveData<String> getItem3TitleMutableLiveData() {
        return diaryLiveData.getItem(3).getTitle();
    }

    public LiveData<String> getItem4TitleLiveData() {
        return diaryLiveData.getItem(4).getTitle();
    }

    public MutableLiveData<String> getItem4TitleMutableLiveData() {
        return diaryLiveData.getItem(4).getTitle();
    }

    public LiveData<String> getItem5TitleLiveData() {
        return diaryLiveData.getItem(5).getTitle();
    }

    public MutableLiveData<String> getItem5TitleMutableLiveData() {
        return diaryLiveData.getItem(5).getTitle();
    }

    public LiveData<String> getItemCommentLiveData(int itemNumber) {
        return diaryLiveData.getItem(itemNumber).getComment();
    }

    public LiveData<String> getItem1CommentLiveData() {
        return diaryLiveData.getItem(1).getComment();
    }

    public MutableLiveData<String> getItem1CommentMutableLiveData() {
        return diaryLiveData.getItem(1).getComment();
    }

    public LiveData<String> getItem2CommentLiveData() {
        return diaryLiveData.getItem(2).getComment();
    }

    public MutableLiveData<String> getItem2CommentMutableLiveData() {
        return diaryLiveData.getItem(2).getComment();
    }

    public LiveData<String> getItem3CommentLiveData() {
        return diaryLiveData.getItem(3).getComment();
    }

    public MutableLiveData<String> getItem3CommentMutableLiveData() {
        return diaryLiveData.getItem(3).getComment();
    }

    public LiveData<String> getItem4CommentLiveData() {
        return diaryLiveData.getItem(4).getComment();
    }

    public MutableLiveData<String> getItem4CommentMutableLiveData() {
        return diaryLiveData.getItem(4).getComment();
    }

    public LiveData<String> getItem5CommentLiveData() {
        return diaryLiveData.getItem(5).getComment();
    }

    public MutableLiveData<String> getItem5CommentMutableLiveData() {
        return diaryLiveData.getItem(5).getComment();
    }

    public LiveData<LocalDateTime> getLogLiveData() {
        return diaryLiveData.getLog();
    }
}
