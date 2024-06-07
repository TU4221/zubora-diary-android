package com.websarva.wings.android.zuboradiary.ui.editdiary;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DiaryViewModel extends AndroidViewModel {

    public class Item {
        private MutableLiveData<Integer> number = new MutableLiveData<>(1);
        private MutableLiveData<String> title = new MutableLiveData<>("");
        private MutableLiveData<String> comment = new MutableLiveData<>("");

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
    }
    

    private DiaryRepository diaryRepository;
    private boolean isNewEditDiary; // TODO:削除保留(Navigationで遷移元からのデータ受取で判断できると思う)
    private boolean requiresPreparationDiary; // TODO:削除保留(Navigationで遷移元からのデータ受取で判断できると思う)
    private MutableLiveData<String> loadingDate = new MutableLiveData<>();
    private MutableLiveData<String> date = new MutableLiveData<>();
    // メモ
    // 下記配列をデータバインディングでスピナーに割り当てたが、スピナーの setSection メソッド(オフセット操作)が機能しなかった。
    // その為、string.xml ファイルに配列を用意し、それを layout.xml に割り当てたら、 setSection メソッドが機能した。
    // 下記配列は str ↔ int 変換で使用するため削除しない。
    // 配列 conditions も同様。
    public String[] weathers = {"--", "晴", "曇", "雨", "雪"};
    private MutableLiveData<Integer> intWeather1 = new MutableLiveData<>();
    private MutableLiveData<String> strWeather1 = new MutableLiveData<>();
    private MutableLiveData<Integer> intWeather2 = new MutableLiveData<>();
    private MutableLiveData<String> strWeather2 = new MutableLiveData<>();
    private String[] conditions = {"--", "HAPPY", "GOOD", "AVERAGE", "POOR", "BAD"};
    private MutableLiveData<Integer> intCondition = new MutableLiveData<>();
    private MutableLiveData<String> strCondition = new MutableLiveData<>();
    private MutableLiveData<String> title = new MutableLiveData<>();
    private int visibleItemsCount;
    public final static int MAX_ITEMS_COUNT = 5;

    private Item[] items = new Item[MAX_ITEMS_COUNT];
    private MutableLiveData<String> log = new MutableLiveData<>();


    public DiaryViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(getApplication());
        for (int i = 0; i < this.items.length; i++) {
            int itemNumber = i + 1;
            this.items[i] = new Item(itemNumber);
        }
        initialize();
    }

    public void initialize() {
        this.isNewEditDiary = false;
        this.requiresPreparationDiary = true;
        this.loadingDate.setValue("");
        this.date.setValue("");
        this.intWeather1.setValue(0);
        this.strWeather1.setValue("--");
        this.intWeather2.setValue(0);
        this.strWeather2.setValue("--");
        this.intCondition.setValue(0);
        this.strCondition.setValue("--");
        this.title.setValue("");
        this.visibleItemsCount = 1;
        for (Item item: this.items) {
            item.title.setValue("");
            item.comment.setValue("");
        }
        this.log.setValue("");
    }

    public void prepareEditDiary() {
        if (this.loadingDate.getValue().isEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDate localDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
                String stringDate = localDate.format(formatter);
                this.date.setValue(stringDate);
            }
        } else {
            String stringLoadingDate = this.loadingDate.getValue();
            if (hasDiary(stringLoadingDate)) {
                loadDiary();
            } else {
                this.date.setValue(stringLoadingDate);
                this.loadingDate.setValue("");
            }
        }
    }

    public void prepareShowDiary() {
        loadDiary();
    }

    private void loadDiary() {
        Diary diary = diaryRepository.selectDiary(this.loadingDate.getValue());
        this.date.setValue(diary.getDate());
        this.log.setValue(diary.getLog());
        this.intWeather1.setValue(toIntegerWeather(diary.getWeather1()));
        this.intWeather2.setValue(toIntegerWeather(diary.getWeather2()));
        this.intCondition.setValue(toIntegerCondition(diary.getCondition()));
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

        this.visibleItemsCount = MAX_ITEMS_COUNT;
        for (int i = MAX_ITEMS_COUNT; i > 1; i--) {
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

        this.isNewEditDiary = false;
    }

    public boolean hasDiary(String date) {
        return diaryRepository.hasDiary(date);
    }
    public boolean hasDiary(int year, int month, int dayOfMonth) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
            String stringDate = localDate.format(formatter);
            return diaryRepository.hasDiary(stringDate);
        }
        return false;
    }

    public void saveNewDiary() {
        Diary diary = createDiary();
        this.diaryRepository.insertDiary(diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
    }

    public void deleteExistingDiaryAndSaveNewDiary() {
        Diary diary = createDiary();
        this.diaryRepository.deleteAndInsertDiary(this.loadingDate.getValue(), diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
    }

    public void updateExistingDiary() {
        Diary diary = createDiary();
        this.diaryRepository.updateDiary(diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
    }

    public void deleteExistingDiaryAndUpdateExistingDiary() {
        Diary diary = createDiary();
        this.diaryRepository.deleteAndUpdateDiary(this.loadingDate.getValue(), diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
    }

    public void deleteDiary(String date) {
        diaryRepository.deleteDiary(date);
    }

    private Diary createDiary() {
        updateLog();
        Diary diary = new Diary();
        diary.setDate(this.date.getValue());
        diary.setWeather1(this.strWeather1.getValue());
        diary.setWeather2(this.strWeather2.getValue());
        diary.setCondition(this.strCondition.getValue());
        diary.setTitle(this.title.getValue().trim());
        diary.setItem1Title(this.items[0].title.getValue().trim());
        diary.setItem1Comment(this.items[0].comment.getValue().trim());
        diary.setItem2Title(this.items[1].title.getValue().trim());
        diary.setItem2Comment(this.items[1].comment.getValue().trim());
        diary.setItem3Title(this.items[2].title.getValue().trim());
        diary.setItem3Comment(this.items[2].comment.getValue().trim());
        diary.setItem4Title(this.items[3].title.getValue().trim());
        diary.setItem4Comment(this.items[3].comment.getValue().trim());
        diary.setItem5Title(this.items[4].title.getValue().trim());
        diary.setItem5Comment(this.items[4].comment.getValue().trim());
        diary.setLog(this.log.getValue());
        return diary;
    }

    public void updateLoadingDate(int year, int month, int dayOfMonth) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
            String stringDate = localDate.format(formatter);
            this.loadingDate.setValue(stringDate);
        }
    }
    public void updateDate(int year, int month, int dayOfMonth) {
        // 日付データ作成。
        // https://qiita.com/hanaaaa/items/8555aaabc6b949ec507d
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
            String stringDate = localDate.format(formatter);
            this.date.setValue(stringDate);
        }
    }

    private void updateLog() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime localDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E) HH:mm:ss");
            String stringDate = localDateTime.format(formatter);
            this.log.setValue(stringDate);
        }
    }


    public void updateStrWeather1() {
        this.strWeather1.setValue(toStringWeather(intWeather1.getValue()));
    }

    public void updateStrWeather2() {
        this.strWeather2.setValue(toStringWeather(intWeather2.getValue()));
    }

    public String toStringWeather(int intWeather) {
        for (int i = 0; i < weathers.length; i++) {
            if (i == intWeather) {
                return weathers[i];
            }
        }
        return weathers[0];
    }

    public int toIntegerWeather(String strWeather) {
        for (int i = 0; i < weathers.length; i++) {
            if (weathers[i].equals(strWeather)) {
                return i;
            }
        }
        return 0;
    }

    public void updateStrCondition() {
        this.strCondition.setValue(toStringCondition(intCondition.getValue()));
    }

    private String toStringCondition(int intCondition) {
        for (int i = 0; i < conditions.length; i++) {
            if (i == intCondition) {
                return conditions[i];
            }
        }
        return conditions[0];
    }

    public int toIntegerCondition(String strCondition) {
        for (int i = 0; i < conditions.length; i++) {
            if (weathers[i].equals(strCondition)) {
                return i;
            }
        }
        return 0;
    }

    public void countUpShowedItem() {
        this.visibleItemsCount++;
    }

    public void deleteItem(int itemNumber) {
        int deleteArrayNo = itemNumber - 1;
        this.items[deleteArrayNo].title.setValue("");
        this.items[deleteArrayNo].comment.setValue("");

        if (itemNumber < visibleItemsCount) {
            int nextArrayNo = -1;
            for (int arrayNo = deleteArrayNo; arrayNo < (visibleItemsCount - 1); arrayNo++) {
                nextArrayNo = arrayNo + 1;
                this.items[arrayNo].title.setValue(this.items[nextArrayNo].title.getValue());
                this.items[arrayNo].comment.setValue(this.items[nextArrayNo].comment.getValue());
                this.items[nextArrayNo].title.setValue("");
                this.items[nextArrayNo].comment.setValue("");
            }
        }
        if (visibleItemsCount > 1) {
            visibleItemsCount -= 1;
        }
    }


    // Getter/Setter
    public Boolean getIsNewEditDiary() {
        return this.isNewEditDiary;
    }
    public void setIsNewEditDiary(Boolean bool) {
        this.isNewEditDiary = bool;
    }

    public Boolean getRequiresPreparationDiary() {
        return this.requiresPreparationDiary;
    }
    public void setRequiresPreparationDiary(Boolean bool) {
        this.requiresPreparationDiary = bool;
    }

    public LiveData<String> getLiveLoadingDate() {
        return this.loadingDate;
    }
    public void setLoadingDate(String loadingDate) {
        this.loadingDate.setValue(loadingDate);
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





}
