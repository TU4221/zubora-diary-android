package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.diary.ConditionConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.data.diary.WeatherConverter;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
        initialize();
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

    public void update(Diary diary) {
        Objects.requireNonNull(diary);

        date.setValue(LocalDate.parse(diary.getDate()));
        WeatherConverter weatherConverter = new WeatherConverter();
        Integer intWeather1 = getOrDefault(diary.getWeather1(), 0);
        weather1.setValue(weatherConverter.toWeather(intWeather1));
        Integer intWeather2 = getOrDefault(diary.getWeather2(), 0);
        weather2.setValue(weatherConverter.toWeather(intWeather2));
        ConditionConverter conditionConverter = new ConditionConverter();
        Integer intCondition = getOrDefault(diary.getCondition(), 0);
        condition.setValue(conditionConverter.toCondition(intCondition));
        String title = getOrDefault(diary.getTitle(), "");
        this.title.setValue(title);

        LocalDateTime nullDateTime = null;
        String item1Title = getOrDefault(diary.getItem1Title(), "");
        String item1Comment = getOrDefault(diary.getItem1Comment(), "");
        items[0].update(item1Title, item1Comment, nullDateTime);

        String item2Title = getOrDefault(diary.getItem2Title(), "");
        String item2Comment = getOrDefault(diary.getItem2Comment(), "");
        items[1].update(item2Title, item2Comment, nullDateTime);

        String item3Title = getOrDefault(diary.getItem3Title(), "");
        String item3Comment = getOrDefault(diary.getItem3Comment(), "");
        items[2].update(item3Title, item3Comment, nullDateTime);

        String item4Title = getOrDefault(diary.getItem4Title(), "");
        String item4Comment = getOrDefault(diary.getItem4Comment(), "");
        items[3].update(item4Title, item4Comment, nullDateTime);

        String item5Title = getOrDefault(diary.getItem5Title(), "");
        String item5Comment = getOrDefault(diary.getItem5Comment(), "");
        items[4].update(item5Title, item5Comment, nullDateTime);

        int numVisibleItems = items.length;
        int maxArrayNumber  = numVisibleItems - 1;
        for (int i = maxArrayNumber; i > 0; i--) {
            if (items[i].isEmpty()) {
                numVisibleItems--;
            } else {
                break;
            }
        }
        this.numVisibleItems.setValue(numVisibleItems);

        log.setValue(LocalDateTime.parse(diary.getLog()));
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        if (value == null) return defaultValue;
        return value;
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

    public static class DiaryItemLiveData {
        private final int itemNumber;
        private final MutableLiveData<String> title = new MutableLiveData<>();
        private final MutableLiveData<String> comment = new MutableLiveData<>();
        private final MutableLiveData<LocalDateTime> titleUpdateLog = new MutableLiveData<>();

        private DiaryItemLiveData(int itemNumber) {
            this.itemNumber = itemNumber;
            initialize();
        }

        public void initialize() {
            title.setValue("");
            comment.setValue("");
            titleUpdateLog.setValue(null);
        }

        public void update(String title, String comment, @Nullable LocalDateTime titleUpdateLog) {
            Objects.requireNonNull(title);
            Objects.requireNonNull(comment);

            this.title.setValue(title);
            this.comment.setValue(comment);
            this.titleUpdateLog.setValue(titleUpdateLog);
        }

        public boolean isEmpty() {
            String title = this.title.getValue();
            Objects.requireNonNull(title);
            String comment = this.comment.getValue();
            Objects.requireNonNull(comment);

            return title.isEmpty() && comment.isEmpty();
        }

        public int getItemNumber() {
            return itemNumber;
        }

        public MutableLiveData<String> getTitle() {
            return title;
        }

        public MutableLiveData<String> getComment() {
            return comment;
        }

        public MutableLiveData<LocalDateTime> getTitleUpdateLog() {
            return titleUpdateLog;
        }
    }
}
