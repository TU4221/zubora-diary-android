package com.websarva.wings.android.zuboradiary.ui.diary;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.DateConverter;
import com.websarva.wings.android.zuboradiary.data.WeatherCodeConverter;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse;
import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.database.SelectedDiaryItemTitle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
public class DiaryViewModel extends ViewModel {
    private final DiaryRepository diaryRepository;
    private final WeatherApiRepository weatherApiRepository;
    private final SettingsRepository settingsRepository;

    // 日記データ関係
    private boolean hasPreparedDiary;
    private final MutableLiveData<String> loadedDate = new MutableLiveData<>();
    private final MutableLiveData<String> date = new MutableLiveData<>();
    // メモ
    // 下記配列をデータバインディングでスピナーに割り当てたが、スピナーの setSection メソッド(オフセット操作)が機能しなかった。
    // その為、string.xml ファイルに配列を用意し、それを layout.xml に割り当てたら、 setSection メソッドが機能した。
    // 下記配列は str ↔ int 変換で使用するため削除しない。
    // 配列 conditions も同様。
    private final MutableLiveData<Integer> intWeather1 = new MutableLiveData<>();
    private final MutableLiveData<String> strWeather1 = new MutableLiveData<>();
    private final MutableLiveData<Integer> intWeather2 = new MutableLiveData<>();
    private final MutableLiveData<String> strWeather2 = new MutableLiveData<>();
    private final MutableLiveData<Integer> intCondition = new MutableLiveData<>();
    private final MutableLiveData<String> strCondition = new MutableLiveData<>();
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<Integer> numVisibleItems = new MutableLiveData<>();
    public static final int MAX_ITEMS = 5;
    private final ItemLiveData[] items = new ItemLiveData[MAX_ITEMS];
    private final MutableLiveData<String> log = new MutableLiveData<>();

    public static class ItemLiveData {
        private final int itemNumber;
        private final MutableLiveData<String> title = new MutableLiveData<>();
        private final MutableLiveData<String> comment = new MutableLiveData<>();
        private final MutableLiveData<String> titleUpdateLog = new MutableLiveData<>();

        public ItemLiveData(int itemNumber) {
            this.itemNumber = itemNumber;
            initialize();
        }

        public void initialize() {
            title.setValue("");
            comment.setValue("");
            titleUpdateLog.setValue("");
        }

        public void updateTitle(String title) {
            this.title.setValue(title);
            titleUpdateLog.setValue(DateConverter.toStringLocalDateTime(LocalDateTime.now()));
        }

        public void updateComment(String comment) {
            this.comment.setValue(comment);
        }

        public void updateAll(String title, String comment, LocalDateTime titleUpdateLog) {
            this.title.setValue(title);
            this.comment.setValue(comment);
            this.titleUpdateLog.setValue(DateConverter.toStringLocalDateTime(titleUpdateLog));
        }

        public LocalDateTime getTitleUpdateLogLocalDateTime() {
            String titleUpdateLog = this.titleUpdateLog.getValue();
            if (titleUpdateLog == null || !DateConverter.isFormatStringDateTime(titleUpdateLog)) {
                return null;
            }
            return DateConverter.toLocalDateTime(titleUpdateLog);
        }

        public int getItemNumber() {
            return itemNumber;
        }

        public LiveData<String> getTitleLiveData() {
            return title;
        }

        public LiveData<String> getCommentLiveData() {
            return comment;
        }

        public LiveData<String> getTitleUpdateLogLiveData() {
            return titleUpdateLog;
        }
    }

    // 天気情報関係
    private Call<WeatherApiResponse> weatherApiResponseCall;
    private boolean isCancelWeatherApiResponseCall;

    // エラー関係
    private final MutableLiveData<Boolean> isDiarySavingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDiaryLoadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDiaryDeleteError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWeatherLoadingError = new MutableLiveData<>();

    @Inject
    public DiaryViewModel(
            DiaryRepository diaryRepository,
            WeatherApiRepository weatherApiRepository,
            SettingsRepository settingsRepository) {
        this.diaryRepository = diaryRepository;
        for (int i = 0; i < items.length; i++) {
            int itemNumber = i + 1;
            items[i] = new ItemLiveData(itemNumber);
        }
        this.weatherApiRepository = weatherApiRepository;
        this.settingsRepository = settingsRepository;
        initialize();
    }

    public void initialize() {
        hasPreparedDiary = false;
        loadedDate.setValue("");
        date.setValue("");
        intWeather1.setValue(0);
        intWeather2.setValue(0);
        intCondition.setValue(0);
        title.setValue("");
        numVisibleItems.setValue(1);
        for (ItemLiveData item: items) {
            item.title.setValue("");
            item.comment.setValue("");
        }
        log.setValue("");
        isDiarySavingError.setValue(false);
        isDiaryLoadingError.setValue(false);
        isDiaryDeleteError.setValue(false);
        isWeatherLoadingError.setValue(false);
    }

    public void prepareDiary(LocalDate date, boolean isLoadingDiary) {
        if (isLoadingDiary && hasDiary(date)) {
            try {
                loadDiary(date);
            } catch (Exception e) {
                isDiaryLoadingError.setValue(true);
                return;
            }
        } else {
            String stringDate = DateConverter.toStringLocalDate(date);
            this.date.setValue(stringDate);
        }
        hasPreparedDiary = true;
    }

    private void loadDiary(LocalDate date) throws Exception {
        String stringDate = DateConverter.toStringLocalDate(date);
        Diary diary = diaryRepository.selectDiary(stringDate);
        this.date.setValue(diary.getDate());
        log.setValue(diary.getLog());
        strWeather1.setValue(diary.getWeather1()); // Fragmentに記述したObserverよりintWeather1更新
        strWeather2.setValue(diary.getWeather2()); // Fragmentに記述したObserverよりintWeather2更新
        strCondition.setValue(diary.getCondition()); // Fragmentに記述したObserverよりintCondition更新
        title.setValue(diary.getTitle());
        items[0].updateTitle(diary.getItem1Title());
        items[0].updateComment(diary.getItem1Comment());
        items[1].updateTitle(diary.getItem2Title());
        items[1].updateComment(diary.getItem2Comment());
        items[2].updateTitle(diary.getItem3Title());
        items[2].updateComment(diary.getItem3Comment());
        items[3].updateTitle(diary.getItem4Title());
        items[3].updateComment(diary.getItem4Comment());
        items[4].updateTitle(diary.getItem5Title());
        items[4].updateComment(diary.getItem5Comment());

        int numVisibleItems = MAX_ITEMS;
        for (int i = MAX_ITEMS; i > 1; i--) {
            int arrayNumber = i - 1;
            String itemTitle = items[arrayNumber].getTitleLiveData().getValue();
            String itemComment = items[arrayNumber].getCommentLiveData().getValue();
            if (itemTitle == null || itemTitle.isEmpty()) {
                if (itemComment == null || itemComment.isEmpty()) {
                    numVisibleItems--;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        this.numVisibleItems.setValue(numVisibleItems);
        loadedDate.setValue(stringDate);
    }

    public boolean hasDiary(LocalDate localDate) {
        try {
            return diaryRepository.hasDiary(localDate).get();
        } catch (ExecutionException | InterruptedException e) {
            isDiaryLoadingError.setValue(true);
            return false;
        }
    }

    public boolean saveDiary() {
        Diary diary = createDiary();
        List<SelectedDiaryItemTitle> selectedDiaryItemTitleList = createSelectedDiaryItemTitleList();
        try {
            diaryRepository.insertDiary(diary, selectedDiaryItemTitleList);
        } catch (Exception e) {
            isDiarySavingError.setValue(true);
            return false;
        }
        loadedDate.setValue(date.getValue());
        return true;
    }

    public boolean deleteExistingDiaryAndSaveDiary() {
        Diary diary = createDiary();
        List<SelectedDiaryItemTitle> selectedDiaryItemTitleList = createSelectedDiaryItemTitleList();
        try {
            diaryRepository.deleteAndInsertDiary(loadedDate.getValue(), diary, selectedDiaryItemTitleList);
        } catch (Exception e) {
            isDiarySavingError.setValue(true);
            return false;
        }
        loadedDate.setValue(date.getValue());
        return true;
    }

    private Diary createDiary() {
        updateLog();
        Diary diary = new Diary();
        diary.setDate(processTrimmedOrNull(date.getValue()));
        diary.setWeather1(processTrimmedOrNull(strWeather1.getValue()));
        diary.setWeather2(processTrimmedOrNull(strWeather2.getValue()));
        diary.setCondition(processTrimmedOrNull(strCondition.getValue()));
        diary.setTitle(processTrimmedOrNull(title.getValue()));
        diary.setItem1Title(processTrimmedOrNull(items[0].getTitleLiveData().getValue()));
        diary.setItem1Comment(processTrimmedOrNull(items[0].getCommentLiveData().getValue()));
        diary.setItem2Title(processTrimmedOrNull(items[1].getTitleLiveData().getValue()));
        diary.setItem2Comment(processTrimmedOrNull(items[1].getCommentLiveData().getValue()));
        diary.setItem3Title(processTrimmedOrNull(items[2].getTitleLiveData().getValue()));
        diary.setItem3Comment(processTrimmedOrNull(items[2].getCommentLiveData().getValue()));
        diary.setItem4Title(processTrimmedOrNull(items[3].getTitleLiveData().getValue()));
        diary.setItem4Comment(processTrimmedOrNull(items[3].getCommentLiveData().getValue()));
        diary.setItem5Title(processTrimmedOrNull(items[4].getTitleLiveData().getValue()));
        diary.setItem5Comment(processTrimmedOrNull(items[4].getCommentLiveData().getValue()));
        diary.setLog(processTrimmedOrNull(log.getValue()));
        return diary;
    }

    private String processTrimmedOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        } else {
            return s.trim();
        }
    }

    private List<SelectedDiaryItemTitle> createSelectedDiaryItemTitleList() {
        List<SelectedDiaryItemTitle> list = new ArrayList<>();
        SelectedDiaryItemTitle selectedDiaryItemTitle = new SelectedDiaryItemTitle();
        for (int i = 0; i < MAX_ITEMS; i++) {
            String itemTitle = items[i].getTitleLiveData().getValue();
            String itemTitleUpdateLog = items[i].getTitleUpdateLogLiveData().getValue();
            if (itemTitle == null || itemTitleUpdateLog == null) {
                continue;
            }
            if (itemTitle.matches("\\S+.*")
                    && DateConverter.isFormatStringDateTime(itemTitleUpdateLog)) {
                selectedDiaryItemTitle.setTitle(itemTitle);
                selectedDiaryItemTitle.setLog(itemTitleUpdateLog);
                list.add(selectedDiaryItemTitle);
                selectedDiaryItemTitle = new SelectedDiaryItemTitle();
            }
        }
        return list;
    }

    public boolean deleteDiary(String date) {
        try {
            diaryRepository.deleteDiary(date);
        } catch (Exception e) {
            isDiaryDeleteError.setValue(true);
            return false;
        }
        return true;
    }

    // 日付関係
    public LocalDate getDateLocalDate() {
        String stringDate = date.getValue();
        if (stringDate == null || !DateConverter.isFormatStringDate(stringDate)) {
            return null;
        }
        return DateConverter.toLocalDate(stringDate);
    }

    public void updateDate(LocalDate date) {
        String stringDate = DateConverter.toStringLocalDate(date);
        this.date.setValue(stringDate);
    }

    public LocalDate getLoadedDateLocalDate() {
        String stringLoadedDate = loadedDate.getValue();
        if (stringLoadedDate == null || !DateConverter.isFormatStringDate(stringLoadedDate)) {
            return null;
        }
        return DateConverter.toLocalDate(stringLoadedDate);
    }

    private void updateLog() {
        String stringDate = DateConverter.toStringLocalDateTimeNow();
        log.setValue(stringDate);
    }

    // 天気、体調関係
    public void updateIntWeather1(int intWeather) {
        intWeather1.setValue(intWeather);
    }

    public void updateStrWeather1(String strWeather) {
        strWeather1.setValue(strWeather);
    }

    public void updateIntWeather2(int intWeather) {
        intWeather2.setValue(intWeather);
    }

    public void updateStrWeather2(String strWeather) {
        strWeather2.setValue(strWeather);
    }

    public void updateIntCondition(int intCondition) {
        this.intCondition.setValue(intCondition);
    }

    public void updateStrCondition(String strCondition) {
        this.strCondition.setValue(strCondition);
    }

    // 天気情報関係
    public void fetchWeatherInformation(@NonNull LocalDate date, double latitude, double longitude) {
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
            weatherApiResponseCall = weatherApiRepository.getTodayWeather(latitude, longitude);
        } else {
            weatherApiResponseCall =
                    weatherApiRepository.getPastDayWeather(latitude, longitude, (int) betweenDays);
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
                    isWeatherLoadingError.postValue(true);
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
                        intWeather1.postValue(weather.toWeatherNumber());
                    } else {
                        isWeatherLoadingError.postValue(true);
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
            isWeatherLoadingError.setValue(true);
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
        Integer numVisibleItems = this.numVisibleItems.getValue();
        if (numVisibleItems != null) {
            numVisibleItems++;
        }
        this.numVisibleItems.setValue(numVisibleItems);
    }

    public void deleteItem(int itemNumber) {
        int deleteArrayNo = itemNumber - 1;
        items[deleteArrayNo].initialize();

        Integer numVisibleItems = this.numVisibleItems.getValue();
        if (numVisibleItems == null) {
            throw new NullPointerException();
        }
        if (itemNumber < numVisibleItems) {
            int nextArrayNo;
            for (int arrayNo = deleteArrayNo; arrayNo < (numVisibleItems - 1); arrayNo++) {
                nextArrayNo = arrayNo + 1;
                items[arrayNo].updateAll(
                        items[nextArrayNo].getTitleLiveData().getValue(),
                        items[nextArrayNo].getCommentLiveData().getValue(),
                        items[nextArrayNo].getTitleUpdateLogLocalDateTime()
                );
                items[nextArrayNo].initialize();
            }
        }
        if (numVisibleItems > 1) {
            numVisibleItems -= 1;
        }
        this.numVisibleItems.setValue(numVisibleItems);
    }

    public void updateItemTitle(int itemNumber, String title) {
        int arrayNumber = itemNumber - 1;
        items[arrayNumber].updateTitle(title);
    }

    // Error関係
    public void clearDiarySavingError(boolean bool) {
        isDiarySavingError.setValue(bool);
    }

    public void clearDiaryLoadingError(boolean bool) {
        isDiaryLoadingError.setValue(bool);
    }

    public void clearDiaryDeleteError(boolean bool) {
        isDiaryDeleteError.setValue(bool);
    }

    public void clearWeatherLoadingError(boolean bool) {
        isWeatherLoadingError.setValue(bool);
    }

    // Getter
    public Boolean getHasPreparedDiary() {
        return hasPreparedDiary;
    }

    // LiveDataGetter
    public LiveData<String> getLoadedDateLiveData() {
        return loadedDate;
    }

    public LiveData<String> getDateLiveData() {
        return date;
    }

    public LiveData<Integer> getLiveIntWeather1() {
        return intWeather1;
    }

    public LiveData<String> getLiveStrWeather1() {
        return strWeather1;
    }

    public LiveData<Integer> getLiveIntWeather2() {
        return intWeather2;
    }

    public LiveData<String> getLiveStrWeather2() {
        return strWeather2;
    }

    public LiveData<Integer> getLiveIntCondition() {
        return intCondition;
    }

    public LiveData<String> getLiveStrCondition() {
        return strCondition;
    }

    public LiveData<String> getLiveTitle() {
        return title;
    }

    public MutableLiveData<String> getMutableLiveTitle() {
        return title;
    }

    public LiveData<Integer> getNumVisibleItemsLiveData() {
        return numVisibleItems;
    }

    public ItemLiveData getItem(int itemNumber) {
        int arrayNumber = itemNumber - 1;
        return items[arrayNumber];
    }
    // MEMO:getItemメソッドではDataBindingで使用できなかった為、getItem1～5メソッド用意。
    //      (引数？ or return以外の処理？がある為、例外が発生)
    public ItemLiveData getItem1() {
        return items[0];
    }
    public ItemLiveData getItem2() {
        return items[1];
    }
    public ItemLiveData getItem3() {
        return items[2];
    }
    public ItemLiveData getItem4() {
        return items[3];
    }
    public ItemLiveData getItem5() {
        return items[4];
    }

    public LiveData<String> getLiveLog() {
        return log;
    }

    public LiveData<Boolean> getIsDiarySavingErrorLiveData() {
        return isDiarySavingError;
    }

    public LiveData<Boolean> getIsDiaryLoadingErrorLiveData() {
        return isDiaryLoadingError;
    }

    public LiveData<Boolean> getIsDiaryDeleteErrorLiveData() {
        return isDiaryDeleteError;
    }

    public LiveData<Boolean> getIsWeatherLoadingErrorLiveData() {
        return isWeatherLoadingError;
    }
}
