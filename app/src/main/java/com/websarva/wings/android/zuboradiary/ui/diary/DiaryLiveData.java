package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DiaryLiveData {
    private final MutableLiveData<LocalDate> date = new MutableLiveData<>();
    private final MutableLiveData<Weathers> weather1 = new MutableLiveData<>();
    private final MutableLiveData<Weathers> weather2 = new MutableLiveData<>();
    private final MutableLiveData<Conditions> condition = new MutableLiveData<>();
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<Integer> numVisibleItems = new MutableLiveData<>();
    public static final int MAX_ITEMS = 5;
    private final DiaryItemLiveData[] items = new DiaryItemLiveData[MAX_ITEMS];
    private final MutableLiveData<String> picturePath = new MutableLiveData<>();
    private final MutableLiveData<LocalDateTime> log = new MutableLiveData<>();

    public DiaryLiveData() {
        for (int i = 0; i < items.length; i++) {
            int itemNumber = i + 1;
            items[i] = new DiaryItemLiveData(itemNumber);
        }
    }

    public void initialize() {
        date.setValue(null);
        weather1.setValue(Weathers.UNKNOWN);
        weather2.setValue(Weathers.UNKNOWN);
        condition.setValue(Conditions.UNKNOWN);
        title.setValue("");
        numVisibleItems.setValue(1);
        for (DiaryItemLiveData item: items) {
            item.initialize();
        }
        picturePath.setValue("");
        log.setValue(null);
    }

    public MutableLiveData<LocalDate> getDate() {
        return date;
    }

    public MutableLiveData<Weathers> getWeather1() {
        return weather1;
    }

    public MutableLiveData<Weathers> getWeather2() {
        return weather2;
    }

    public MutableLiveData<Conditions> getCondition() {
        return condition;
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public MutableLiveData<Integer> getNumVisibleItems() {
        return numVisibleItems;
    }

    public DiaryItemLiveData getItem(int itemNumber) {
        int arrayNumber = itemNumber - 1;
        return items[arrayNumber];
    }
    public MutableLiveData<String> getPicturePath() {
        return picturePath;
    }

    public MutableLiveData<LocalDateTime> getLog() {
        return log;
    }
}
