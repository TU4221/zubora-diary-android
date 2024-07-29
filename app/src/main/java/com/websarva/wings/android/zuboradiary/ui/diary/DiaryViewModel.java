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
import retrofit2.Call;
import retrofit2.Response;

@HiltViewModel
public class DiaryViewModel extends ViewModel {

    public static class Item {
        private MutableLiveData<Integer> number = new MutableLiveData<>(1);
        private MutableLiveData<String> title = new MutableLiveData<>("");
        private MutableLiveData<String> comment = new MutableLiveData<>("");
        private String titleUpdateLog = "";

        public Item(int itemNumber) {
            setNumber(itemNumber);
        }

        public LiveData<Integer> getLiveNumber() {
            return this.number;
        }
        public void setNumber(int number) {
            this.number.setValue(number);
        }

        public LiveData<String> getLiveTitle() {
            return this.title;
        }
        public void setTitle(String title) {
            this.title.setValue(title);
            this.titleUpdateLog = DateConverter.toStringLocalDateTimeNow();
        }

        public LiveData<String> getLiveComment() {
            return this.comment;
        }
        public MutableLiveData<String> getMutableLiveComment() {
            return this.comment;
        }
        public void setComment(String comment) {
            this.comment.setValue(comment);
        }
        public String getTitleUpdateLog() {
            return titleUpdateLog;
        }
        public void setTitleUpdateLog(String titleUpdateLog) {
            this.titleUpdateLog = titleUpdateLog;
        }
    }
    

    private DiaryRepository diaryRepository;
    private WeatherApiRepository weatherApiRepository;
    private SettingsRepository settingsRepository;
    private boolean hasPreparedDiary;
    private MutableLiveData<String> loadedDate = new MutableLiveData<>();
    private MutableLiveData<String> date = new MutableLiveData<>();
    // メモ
    // 下記配列をデータバインディングでスピナーに割り当てたが、スピナーの setSection メソッド(オフセット操作)が機能しなかった。
    // その為、string.xml ファイルに配列を用意し、それを layout.xml に割り当てたら、 setSection メソッドが機能した。
    // 下記配列は str ↔ int 変換で使用するため削除しない。
    // 配列 conditions も同様。
    private MutableLiveData<Integer> intWeather1 = new MutableLiveData<>();
    private MutableLiveData<String> strWeather1 = new MutableLiveData<>();
    private MutableLiveData<Integer> intWeather2 = new MutableLiveData<>();
    private MutableLiveData<String> strWeather2 = new MutableLiveData<>();
    private MutableLiveData<Integer> intCondition = new MutableLiveData<>();
    private MutableLiveData<String> strCondition = new MutableLiveData<>();
    private MutableLiveData<String> title = new MutableLiveData<>();
    private int visibleItemsCount;
    public final static int MAX_ITEMS = 5;

    private Item[] items = new Item[MAX_ITEMS];
    private MutableLiveData<String> log = new MutableLiveData<>();
    private Call<WeatherApiResponse> weatherApiResponseCall;
    private boolean isCancelWeatherApiResponseCall;
    private MutableLiveData<Boolean> isDiarySavingError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isDiaryLoadingError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isDiaryDeleteError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isWeatherLoadingError = new MutableLiveData<>();

    @Inject
    public DiaryViewModel(
            DiaryRepository diaryRepository,
            WeatherApiRepository weatherApiRepository,
            SettingsRepository settingsRepository) {
        this.diaryRepository = diaryRepository;
        for (int i = 0; i < this.items.length; i++) {
            int itemNumber = i + 1;
            this.items[i] = new Item(itemNumber);
        }
        this.weatherApiRepository = weatherApiRepository;
        this.settingsRepository = settingsRepository;
        initialize();
    }

    public void initialize() {
        this.hasPreparedDiary = false;
        this.loadedDate.setValue("");
        this.date.setValue("");
        this.intWeather1.setValue(0);
        this.intWeather2.setValue(0);
        this.intCondition.setValue(0);
        this.title.setValue("");
        this.visibleItemsCount = 1;
        for (Item item: this.items) {
            item.title.setValue("");
            item.comment.setValue("");
        }
        this.log.setValue("");
        isDiarySavingError.setValue(false);
        isDiaryLoadingError.setValue(false);
        isDiaryDeleteError.setValue(false);
        isWeatherLoadingError.setValue(false);
    }

    public void prepareDiary(int year, int month, int dayOfMonth, boolean isLoadingDiary) {
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        if (isLoadingDiary && hasDiary(year, month, dayOfMonth)) {
            try {
                loadDiary(stringDate);
            } catch (Exception e) {
                isDiaryLoadingError.setValue(true);
                return;
            }
        } else {
            this.date.setValue(stringDate);
        }
        this.hasPreparedDiary = true;
    }

    public void prepareDiary(LocalDate localDate, boolean isLoadingDiary) {
        String stringDate = DateConverter.toStringLocalDate(localDate);
        if (isLoadingDiary && hasDiary(localDate)) {
            try {
                loadDiary(stringDate);
            } catch (Exception e) {
                isDiaryLoadingError.setValue(true);
                return;
            }
        } else {
            this.date.setValue(stringDate);
        }
        this.hasPreparedDiary = true;
    }

    private void loadDiary(String date) throws Exception {
        Diary diary = diaryRepository.selectDiary(date);
        this.date.setValue(diary.getDate());
        this.log.setValue(diary.getLog());
        this.strWeather1.setValue(diary.getWeather1());
        this.strWeather2.setValue(diary.getWeather2());
        this.strCondition.setValue(diary.getCondition());
        this.title.setValue(diary.getTitle());
        this.items[0].title.setValue(diary.getItem1Title());
        this.items[0].comment.setValue(diary.getItem1Comment());
        this.items[1].title.setValue(diary.getItem2Title());
        this.items[1].comment.setValue(diary.getItem2Comment());
        this.items[2].title.setValue(diary.getItem3Title());
        this.items[2].comment.setValue(diary.getItem3Comment());
        this.items[3].title.setValue(diary.getItem4Title());
        this.items[3].comment.setValue(diary.getItem4Comment());
        this.items[4].title.setValue(diary.getItem5Title());
        this.items[4].comment.setValue(diary.getItem5Comment());

        this.visibleItemsCount = MAX_ITEMS;
        for (int i = MAX_ITEMS; i > 1; i--) {
            int arrayNumber = i - 1;
            if (this.items[arrayNumber].title.getValue() == null
                    || this.items[arrayNumber].title.getValue().isEmpty()) {
                if (this.items[arrayNumber].comment.getValue() == null
                        || this.items[arrayNumber].comment.getValue().isEmpty()) {
                    this.visibleItemsCount--;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        loadedDate.setValue(date);
    }

    public boolean hasDiary(int year, int month, int dayOfMonth) {
        try {
            return diaryRepository.hasDiary(year, month, dayOfMonth);
        } catch (Exception e) {
            isDiaryLoadingError.postValue(true); // TODO:postValue?setValue?
            return false;
        }
    }

    public boolean hasDiary(LocalDate localDate) {
        try {
            return diaryRepository.hasDiary(localDate);
        } catch (ExecutionException | InterruptedException e) {
            isDiaryLoadingError.postValue(true); // TODO:postValue?setValue?
            return false;
        }
    }

    public void prepareWeatherSelection(int year, int month, int dayOfMonth, double latitude, double longitude) {
        LocalDate diaryDate = LocalDate.of(year, month, dayOfMonth);
        LocalDate currentDate = LocalDate.now();
        Log.d("20240717", "isAfter:" + String.valueOf(diaryDate.isAfter(currentDate)));
        if (diaryDate.isAfter(currentDate)) {
            return;
        }
        long betweenDays = ChronoUnit.DAYS.between(diaryDate, currentDate);
        Log.d("20240717", "betweenDays:" + String.valueOf(betweenDays));
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
        diary.setDate(date.getValue());
        if (strWeather1.getValue() == null || strWeather1.getValue().isEmpty()) {
            diary.setWeather1(null);
        } else {
            diary.setWeather1(this.strWeather1.getValue());
        }
        if (strWeather2.getValue() == null || strWeather1.getValue().isEmpty()) {
            diary.setWeather2(null);
        } else {
            diary.setWeather2(strWeather2.getValue());
        }
        if (strCondition.getValue() == null || strCondition.getValue().isEmpty()) {
            diary.setCondition(null);
        } else {
            diary.setCondition(strCondition.getValue());
        }
        if (title.getValue() == null || title.getValue().isEmpty()) {
            diary.setTitle(null);
        } else {
            diary.setTitle(title.getValue().trim());
        }
        diary.setItem1Title(items[0].title.getValue().trim());
        diary.setItem1Comment(items[0].comment.getValue().trim());
        diary.setItem2Title(items[1].title.getValue().trim());
        diary.setItem2Comment(items[1].comment.getValue().trim());
        diary.setItem3Title(items[2].title.getValue().trim());
        diary.setItem3Comment(items[2].comment.getValue().trim());
        diary.setItem4Title(items[3].title.getValue().trim());
        diary.setItem4Comment(items[3].comment.getValue().trim());
        diary.setItem5Title(items[4].title.getValue().trim());
        diary.setItem5Comment(items[4].comment.getValue().trim());
        diary.setLog(log.getValue());
        return diary;
    }

    private List<SelectedDiaryItemTitle> createSelectedDiaryItemTitleList() {
        List<SelectedDiaryItemTitle> list = new ArrayList<>();
        SelectedDiaryItemTitle selectedDiaryItemTitle = new SelectedDiaryItemTitle();
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (this.items[i].title.getValue().matches("\\S+.*")
                    && !this.items[i].titleUpdateLog.isEmpty()) {
                selectedDiaryItemTitle.setTitle(this.items[i].title.getValue());
                selectedDiaryItemTitle.setLog(this.items[i].titleUpdateLog);
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

    public void updateDate(int year, int month, int dayOfMonth) {
        String stringDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);
        this.date.setValue(stringDate);
    }

    private void updateLog() {
        String stringDate = DateConverter.toStringLocalDateTimeNow();
        this.log.setValue(stringDate);
    }

    public void incrementVisibleItemsCount() {
        this.visibleItemsCount++;
    }

    public void deleteItem(int itemNumber) {
        int deleteArrayNo = itemNumber - 1;
        this.items[deleteArrayNo].title.setValue("");
        this.items[deleteArrayNo].comment.setValue("");
        this.items[deleteArrayNo].titleUpdateLog = "";

        if (itemNumber < visibleItemsCount) {
            int nextArrayNo = -1;
            for (int arrayNo = deleteArrayNo; arrayNo < (visibleItemsCount - 1); arrayNo++) {
                nextArrayNo = arrayNo + 1;
                this.items[arrayNo].title.setValue(this.items[nextArrayNo].title.getValue());
                this.items[arrayNo].comment.setValue(this.items[nextArrayNo].comment.getValue());
                this.items[arrayNo].titleUpdateLog = this.items[nextArrayNo].titleUpdateLog;
                this.items[nextArrayNo].title.setValue("");
                this.items[nextArrayNo].comment.setValue("");
                this.items[nextArrayNo].titleUpdateLog = "";
            }
        }
        if (visibleItemsCount > 1) {
            visibleItemsCount -= 1;
        }
    }




    // Getter/Setter
    public Boolean getHasPreparedDiary() {
        return this.hasPreparedDiary;
    }
    public void setHasPreparedDiary(Boolean bool) {
        this.hasPreparedDiary = bool;
    }

    public LiveData<String> getLoadedDateLiveData() {
        return loadedDate;
    }

    public void setLoadedDateLiveData(String loadedDate) {
        this.loadedDate.setValue(loadedDate);
    }

    public LiveData<String> getLiveDate() {
        return this.date;
    }
    public void setDate(String date) {
        this.date.setValue(date);
    }

    public LiveData<Integer> getLiveIntWeather1() {
        return this.intWeather1;
    }
    public void setIntWeather1(int intWeather) {
        this.intWeather1.setValue(intWeather);
    }

    public LiveData<String> getLiveStrWeather1() {
        return this.strWeather1;
    }
    public void setStrWeather1(String strWeather) {
        this.strWeather1.setValue(strWeather);
    }

    public LiveData<Integer> getLiveIntWeather2() {
        return this.intWeather2;
    }
    public void setIntWeather2(int intWeather) {
        this.intWeather2.setValue(intWeather);
    }

    public LiveData<String> getLiveStrWeather2() {
        return this.strWeather2;
    }
    public void setStrWeather2(String strWeather) {
        this.strWeather2.setValue(strWeather);
    }

    public LiveData<Integer> getLiveIntCondition() {
        return this.intCondition;
    }
    public void setIntCondition(int intCondition) {
        this.intCondition.setValue(intCondition);
    }

    public LiveData<String> getLiveStrCondition() {
        return this.strCondition;
    }
    public void setStrCondition(String strCondition) {
        this.strCondition.setValue(strCondition);
    }

    public LiveData<String> getLiveTitle() {
        return this.title;
    }
    public MutableLiveData<String> getMutableLiveTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public int getVisibleItemsCount() {
        return this.visibleItemsCount;
    }
    private void setVisibleItemsCount(int itemNumber) {
        this.visibleItemsCount = itemNumber;
    }

    public Item getItem(int itemNumber) {
        int arrayNumber = itemNumber - 1;
        return this.items[arrayNumber];
    }
    // MEMO:getItemメソッドではDataBindingで使用できなかった為、getItem1～5メソッド用意。
    //      (引数？ or return以外の処理？がある為、例外が発生)
    public Item getItem1() {
        return this.items[0];
    }
    public Item getItem2() {
        return this.items[1];
    }
    public Item getItem3() {
        return this.items[2];
    }
    public Item getItem4() {
        return this.items[3];
    }
    public Item getItem5() {
        return this.items[4];
    }

    public LiveData<String> getLiveLog() {
        return this.log;
    }
    public void setLog(String log) {
        this.log.setValue(log);
    }

    public LiveData<Boolean> getIsDiarySavingErrorLiveData() {
        return isDiarySavingError;
    }

    public void setIsDiarySavingErrorLiveData(boolean bool) {
        isDiarySavingError.setValue(bool);
    }

    public LiveData<Boolean> getIsDiaryLoadingErrorLiveData() {
        return isDiaryLoadingError;
    }

    public void setIsDiaryLoadingErrorLiveData(boolean bool) {
        isDiaryLoadingError.setValue(bool);
    }

    public LiveData<Boolean> getIsDiaryDeleteErrorLiveData() {
        return isDiaryDeleteError;
    }

    public void setIsDiaryDeleteErrorLiveData(boolean bool) {
        isDiaryDeleteError.setValue(bool);
    }


    public LiveData<Boolean> getIsWeatherLoadingErrorLiveData() {
        return isWeatherLoadingError;
    }

    public void setIsWeatherLoadingErrorLiveData(boolean bool) {
        isWeatherLoadingError.setValue(bool);
    }
}
