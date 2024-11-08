package com.websarva.wings.android.zuboradiary.ui.diary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.database.Diary;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.diary.Condition;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiaryLiveData {
    private final MutableLiveData<LocalDate> date = new MutableLiveData<>();
    private final MutableLiveData<Weather> weather1 = new MutableLiveData<>();
    private final MutableLiveData<Weather> weather2 = new MutableLiveData<>();
    private final MutableLiveData<Condition> condition = new MutableLiveData<>();
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<Integer> numVisibleItems = new MutableLiveData<>();
    public static final int MAX_ITEMS = ItemNumber.MAX_NUMBER;
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
        weather1.setValue(Weather.UNKNOWN);
        weather2.setValue(Weather.UNKNOWN);
        condition.setValue(Condition.UNKNOWN);
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
        Integer intWeather1 = getOrDefault(diary.getWeather1(), 0);
        weather1.setValue(Weather.of(intWeather1));
        Integer intWeather2 = getOrDefault(diary.getWeather2(), 0);
        weather2.setValue(Weather.of(intWeather2));
        Integer intCondition = getOrDefault(diary.getCondition(), 0);
        condition.setValue(Condition.of(intCondition));
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

    @NonNull
    public Diary createDiary() {
        Diary diary = new Diary();
        diary.setDate(toDateString(date.getValue()));
        diary.setWeather1(toIntWeather(weather1.getValue()));
        diary.setWeather2(toIntWeather(weather2.getValue()));
        diary.setCondition(toIntCondition(condition.getValue()));
        diary.setTitle(toTrimmedString(title.getValue()));
        diary.setItem1Title(toTrimmedString(items[0].title.getValue()));
        diary.setItem1Comment(toTrimmedString(items[0].comment.getValue()));
        diary.setItem2Title(toTrimmedString(items[1].title.getValue()));
        diary.setItem2Comment(toTrimmedString(items[1].comment.getValue()));
        diary.setItem3Title(toTrimmedString(items[2].title.getValue()));
        diary.setItem3Comment(toTrimmedString(items[2].comment.getValue()));
        diary.setItem4Title(toTrimmedString(items[3].title.getValue()));
        diary.setItem4Comment(toTrimmedString(items[3].comment.getValue()));
        diary.setItem5Title(toTrimmedString(items[4].title.getValue()));
        diary.setItem5Comment(toTrimmedString(items[4].comment.getValue()));
        diary.setPicturePath(toTrimmedString(picturePath.getValue()));
        diary.setLog(LocalDateTime.now().toString());
        return diary;
    }

    @NonNull
    private String toDateString(LocalDate date) {
        Objects.requireNonNull(date);

        return date.toString();
    }

    private int toIntWeather(Weather weather) {
        Objects.requireNonNull(weather);

        return weather.toNumber();
    }

    private int toIntCondition(Condition condition) {
        Objects.requireNonNull(condition);

        return condition.toNumber();
    }

    @NonNull
    private String toTrimmedString(String s) {
        Objects.requireNonNull(s);

        return s.trim();
    }

    @NonNull
    public List<DiaryItemTitleSelectionHistoryItem> createDiaryItemTitleSelectionHistoryItemList() {
        List<DiaryItemTitleSelectionHistoryItem> list = new ArrayList<>();
        for (int i = 0; i < DiaryLiveData.MAX_ITEMS; i++) {
            String itemTitle = items[i].title.getValue();
            LocalDateTime itemTitleUpdateLog = items[i].titleUpdateLog.getValue();
            Objects.requireNonNull(itemTitle);
            if (itemTitleUpdateLog == null) continue;
            if (itemTitle.matches("\\S+.*")) {
                DiaryItemTitleSelectionHistoryItem item = new DiaryItemTitleSelectionHistoryItem();
                item.setTitle(itemTitle);
                item.setLog(itemTitleUpdateLog.toString());
                list.add(item);
            }
        }
        return list;
    }

    public void incrementVisibleItemsCount() {
        Integer numVisibleItems = this.numVisibleItems.getValue();
        Objects.requireNonNull(numVisibleItems);
        Integer incrementedNumVisibleItems = numVisibleItems + 1;
        this.numVisibleItems.setValue(incrementedNumVisibleItems);
    }

    public void deleteItem(ItemNumber itemNumber) {
        getItemLiveData(itemNumber).initialize();
        Integer numVisibleItems = this.numVisibleItems.getValue();
        Objects.requireNonNull(numVisibleItems);

        if (itemNumber.getValue() < numVisibleItems) {
            for (int i = itemNumber.getValue(); i < numVisibleItems; i++) {
                ItemNumber targetItemNumber = new ItemNumber(i);
                ItemNumber nextItemNumber = new ItemNumber(i + 1);
                getItemLiveData(targetItemNumber).update(
                        getItemLiveData(nextItemNumber).title.getValue(),
                        getItemLiveData(nextItemNumber).comment.getValue(),
                        getItemLiveData(nextItemNumber).titleUpdateLog.getValue()
                );
                getItemLiveData(nextItemNumber).initialize();
            }
        }

        if (numVisibleItems > ItemNumber.MIN_NUMBER) {
            Integer decrementedNumVisibleItems = numVisibleItems - 1;
            this.numVisibleItems.setValue(decrementedNumVisibleItems);
        }
    }

    public void updateItemTitle(ItemNumber itemNumber, String title) {
        Objects.requireNonNull(title);

        getItemLiveData(itemNumber).updateItemTitle(title);
    }

    public MutableLiveData<LocalDate> getDateMutableLiveData() {
        return date;
    }

    public MutableLiveData<Weather> getWeather1MutableLiveData() {
        return weather1;
    }

    public MutableLiveData<Weather> getWeather2MutableLiveData() {
        return weather2;
    }

    public MutableLiveData<Condition> getConditionMutableLiveData() {
        return condition;
    }

    public MutableLiveData<String> getTitleMutableLiveData() {
        return title;
    }

    public MutableLiveData<Integer> getNumVisibleItemsMutableLiveData() {
        return numVisibleItems;
    }

    public DiaryItemLiveData getItemLiveData(ItemNumber itemNumber) {
        Objects.requireNonNull(itemNumber);

        int arrayNumber = itemNumber.getValue() - 1;
        return items[arrayNumber];
    }

    public MutableLiveData<String> getPicturePathMutableLiveData() {
        return picturePath;
    }

    public MutableLiveData<LocalDateTime> getLogMutableLiveData() {
        return log;
    }

    public static class DiaryItemLiveData {
        private final int itemNumber;
        private final MutableLiveData<String> title = new MutableLiveData<>();
        private final MutableLiveData<String> comment = new MutableLiveData<>();
        private final MutableLiveData<LocalDateTime> titleUpdateLog = new MutableLiveData<>();
        public static final int MIN_ITEM_NUMBER = 1;
        public static final int MAX_ITEM_NUMBER = 5;

        private DiaryItemLiveData(int itemNumber) {
            if (!isItemNumberInRange(itemNumber)) throw new IllegalArgumentException();

            this.itemNumber = itemNumber;
            initialize();
        }

        private boolean isItemNumberInRange(int itemNumber) {
            return itemNumber >= MIN_ITEM_NUMBER && itemNumber <= MAX_ITEM_NUMBER;
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

        public void updateItemTitle(String title) {
            Objects.requireNonNull(title);

            this.title.setValue(title);
            this.titleUpdateLog.setValue(LocalDateTime.now());
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

        public MutableLiveData<String> getTitleMutableLiveData() {
            return title;
        }

        public MutableLiveData<String> getCommentMutableLiveData() {
            return comment;
        }

        public MutableLiveData<LocalDateTime> getTitleUpdateLogMutableLiveData() {
            return titleUpdateLog;
        }
    }
}
