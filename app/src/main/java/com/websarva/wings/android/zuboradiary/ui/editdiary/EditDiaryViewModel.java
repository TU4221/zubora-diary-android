package com.websarva.wings.android.zuboradiary.ui.editdiary;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditDiaryViewModel extends AndroidViewModel {

    public class Item {
        public MutableLiveData<Boolean> isVisible = new MutableLiveData<>(false);
        public MutableLiveData<String> title = new MutableLiveData<>("");
        public MutableLiveData<String> comment = new MutableLiveData<>("");
    }
    

    private DiaryRepository diaryRepository;
    private boolean isNewEditDiary = false;
    private boolean wasNewEditDiary = false;
    private boolean requiresPreparationDiary = true;
    public MutableLiveData<String> loadingDate = new MutableLiveData<>("");
    public MutableLiveData<String> date = new MutableLiveData<>("");
    public MutableLiveData<String> log = new MutableLiveData<>("");
    // メモ
    // 下記配列をデータバインディングでスピナーに割り当てたが、スピナーの setSection メソッド(オフセット操作)が機能しなかった。
    // その為、string.xml ファイルに配列を用意し、それを layout.xml に割り当てたら、 setSection メソッドが機能した。
    // 下記配列は str ↔ int 変換で使用するため削除しない。
    // 配列 conditions も同様。
    public String[] weathers = {"--", "晴", "曇", "雨", "雪"};
    public MutableLiveData<Integer> intWeather1 = new MutableLiveData<>(0);
    public MutableLiveData<String> strWeather1 = new MutableLiveData<>("--");
    public MutableLiveData<Integer> intWeather2 = new MutableLiveData<>(0);
    public MutableLiveData<String> strWeather2 = new MutableLiveData<>("--");
    public String[] conditions = {"--", "HAPPY", "GOOD", "AVERAGE", "POOR", "BAD"};
    public MutableLiveData<Integer> intCondition = new MutableLiveData<>(0);
    public MutableLiveData<String> strCondition = new MutableLiveData<>("--");
    public MutableLiveData<String> title = new MutableLiveData<>("");
    public int showItemNum = 1;
    private final int MAX_ITEM_NUM = 5;

    public Item[] items = new Item[MAX_ITEM_NUM];


    public EditDiaryViewModel(@NonNull Application application) {
        super(application);
        diaryRepository = new DiaryRepository(getApplication());
        for (int i = 0; i < items.length; i++) {
            items[i] = new Item();
        }
        this.items[0].isVisible.setValue(true);
    }

    public void clear() {
        this.isNewEditDiary = false;
        this.wasNewEditDiary = false;
        this.requiresPreparationDiary = true;
        this.loadingDate.setValue("");
        this.date.setValue("");
        this.log.setValue("");
        this.intWeather1.setValue(0);
        this.intWeather2.setValue(0);
        this.intCondition.setValue(0);
        this.title.setValue("");
        for (Item item: this.items) {
            item.isVisible.setValue(false);
            item.title.setValue("");
            item.comment.setValue("");
        }
        this.items[0].isVisible.setValue(true);
    }

    public void prepareEditDiary() {
        if (this.loadingDate.getValue().equals("")) {
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
        Log.d("20240328", "loadDiary");
        Log.d("20240328", this.loadingDate.getValue());
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

        showItemNum = 5;
        if (((this.items[4].title.getValue() == null) || (this.items[4].title.getValue().equals("")))
                && ((this.items[4].comment.getValue() == null) || (this.items[4].comment.getValue().equals("")))) {
            this.items[4].isVisible.setValue(false);
            showItemNum--;

            if (((this.items[3].title.getValue() == null) || (this.items[3].title.getValue().equals("")))
                    && ((this.items[3].comment.getValue() == null) || (this.items[3].comment.getValue().equals("")))) {
                this.items[3].isVisible.setValue(false);
                showItemNum--;

                if (((this.items[2].title.getValue() == null) || (this.items[2].title.getValue().equals("")))
                        && ((this.items[2].comment.getValue() == null) || (this.items[2].comment.getValue().equals("")))) {
                    this.items[2].isVisible.setValue(false);
                    showItemNum--;

                    if (((this.items[1].title.getValue() == null) || (this.items[1].title.getValue().equals("")))
                            && ((this.items[1].comment.getValue() == null) || (this.items[1].comment.getValue().equals("")))) {
                        this.items[1].isVisible.setValue(false);
                        showItemNum--;
                    } else {
                        this.items[1].isVisible.setValue(true);
                    }
                } else {
                    this.items[1].isVisible.setValue(true);
                    this.items[2].isVisible.setValue(true);
                }
            } else {
                this.items[1].isVisible.setValue(true);
                this.items[2].isVisible.setValue(true);
                this.items[3].isVisible.setValue(true);
            }
        } else {
            this.items[1].isVisible.setValue(true);
            this.items[2].isVisible.setValue(true);
            this.items[3].isVisible.setValue(true);
            this.items[4].isVisible.setValue(true);
        }
        this.items[0].isVisible.setValue(true);

        this.isNewEditDiary = false;
        this.wasNewEditDiary = false;
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
        this.wasNewEditDiary = false;
    }

    public void deleteExistingDiaryAndSaveNewDiary() {
        Diary diary = createDiary();
        this.diaryRepository.deleteAndInsertDiary(this.loadingDate.getValue(), diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
        this.wasNewEditDiary = false;
    }

    public void updateExistingDiary() {
        Diary diary = createDiary();
        this.diaryRepository.updateDiary(diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
        this.wasNewEditDiary = false;
    }

    public void deleteExistingDiaryAndUpdateExistingDiary() {
        Diary diary = createDiary();
        this.diaryRepository.deleteAndUpdateDiary(this.loadingDate.getValue(), diary);
        this.loadingDate.setValue(this.date.getValue());
        this.isNewEditDiary = false;
        this.wasNewEditDiary = false;
    }

    public void deleteDiary(String date) {
        diaryRepository.deleteDiary(date);
    }

    private Diary createDiary() {
        updateLog();
        Diary diary = new Diary();
        diary.setDate(this.date.getValue());
        diary.setLog(this.log.getValue());
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

    public void onItemSelectedWeather2(AdapterView<?> parent, View view, int position, long id) {
        this.intWeather2.setValue(position);
    }

    public void updateStrWeather2() {
        this.strWeather2.setValue(toStringWeather(intWeather2.getValue()));
    }

    public String toStringWeather(int intWeather) {
        String strWeather = "";
        switch (intWeather) {
            case 0:
                strWeather = weathers[0];
                break;
            case 1:
                strWeather = weathers[1];
                break;
            case 2:
                strWeather = weathers[2];
                break;
            case 3:
                strWeather = weathers[3];
                break;
            case 4:
                strWeather = weathers[4];
                break;
            default:
        }
        return strWeather;
    }

    public int toIntegerWeather(String strWeather) {
        int intWeather = 0;
        switch (strWeather) {
            case "晴":
                intWeather = 1;
                break;
            case "曇":
                intWeather = 2;
                break;
            case "雨":
                intWeather = 3;
                break;
            case "雪":
                intWeather = 4;
                break;
            default:
                intWeather = 0;
        }
        return intWeather;
    }

    public void updateStrCondition() {
        this.strCondition.setValue(toStringCondition(intCondition.getValue()));
    }

    private String toStringCondition(int intCondition) {
        String strCondition = "";
        switch (intCondition) {
            case 0:
                strCondition = conditions[0];
                break;
            case 1:
                strCondition = conditions[1];
                break;
            case 2:
                strCondition = conditions[2];
                break;
            case 3:
                strCondition = conditions[3];
                break;
            case 4:
                strCondition = conditions[4];
                break;
            case 5:
                strCondition = conditions[5];
                break;
            default:
        }
        return strCondition;
    }

    public int toIntegerCondition(String strCondition) {
        int intCondition = 0;
        switch (strCondition) {
            case "HAPPY":
                intCondition = 1;
                break;
            case "GOOD":
                intCondition = 2;
                break;
            case "AVERAGE":
                intCondition = 3;
                break;
            case "POOR":
                intCondition = 4;
                break;
            case "BAD":
                intCondition = 5;
                break;
            default:
                intCondition = 0;
        }
        return intCondition;
    }

    public void onClickAddItemButton(View v) {
        showItemNum += 1;
        switch (showItemNum) {
            case 5:
                this.items[4].isVisible.setValue(true);
            case 4:
                this.items[3].isVisible.setValue(true);
            case 3:
                this.items[2].isVisible.setValue(true);
            case 2:
                this.items[1].isVisible.setValue(true);
            default:
                this.items[0].isVisible.setValue(true);
        }
    }

    public void deleteItem(int itemNo) {
        int deleteArrayNo = itemNo - 1;
        this.items[deleteArrayNo].title.setValue("");
        this.items[deleteArrayNo].comment.setValue("");

        if (itemNo < showItemNum) {
            int nextArrayNo = -1;
            for (int arrayNo = deleteArrayNo; arrayNo < (showItemNum - 1); arrayNo++) {
                nextArrayNo = arrayNo + 1;
                this.items[arrayNo].title.setValue(this.items[nextArrayNo].title.getValue());
                this.items[arrayNo].comment.setValue(this.items[nextArrayNo].comment.getValue());
                this.items[nextArrayNo].title.setValue("");
                this.items[nextArrayNo].comment.setValue("");
            }
        }
        if (showItemNum > 1) {
            switch (showItemNum) {
                case 2:
                    this.items[1].isVisible.setValue(false);
                case 3:
                    this.items[2].isVisible.setValue(false);
                case 4:
                    this.items[3].isVisible.setValue(false);
                case 5:
                    this.items[4].isVisible.setValue(false);
                default:
                    this.items[0].isVisible.setValue(true);
            }
            showItemNum -= 1;
        }
    }


    // 注意) データバインディングを使用する時、セッター/ゲッターのメソッド名を「set/get変数名」にするとビルドが上手く行われない。
    //      その為、メソッド名に「Live」を追加して回避。

    public Boolean getIsNewEditDiary() {
        return this.isNewEditDiary;
    }

    public void setIsNewEditDiary(Boolean bool) {
        this.isNewEditDiary = bool;
    }

    public Boolean getWasNewEditDiary() {
        return this.wasNewEditDiary;
    }

    public void setWasNewEditDiary(Boolean bool) {
        this.wasNewEditDiary = bool;
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
    public void setLiveLoadingDate(String loadingDate) {
        this.loadingDate.setValue(loadingDate);
    }
    public LiveData<String> getLiveDate() {
        return this.date;
    }

    public LiveData<Integer> getLiveIntWeather1() {
        return this.intWeather1;
    }

    public void setLiveIntWeather1(int intWeather) {
        this.intWeather1.setValue(intWeather);
    }

    public LiveData<Integer> getLiveIntWeather2() {
        return this.intWeather2;
    }

    public void setLiveIntWeather2(int intWeather) {
        this.intWeather2.setValue(intWeather);
    }

    public LiveData<Integer> getLiveIntCondition() {
        return this.intCondition;
    }

    public void setLiveIntCondition(int intCondition) {
        this.intCondition.setValue(intCondition);
    }

    public LiveData<String> getLiveTitle() {
        return this.title;
    }

    public LiveData<String> getLiveItem1Title() {
        return this.items[0].title;
    }

    public void setLiveItem1Title(String title) {
        this.items[0].title.setValue(title);
    }

    public LiveData<String> getLiveItem1Comment() {
        return this.items[0].comment;
    }

    public LiveData<String> getLiveItem2Title() {
        return this.items[1].title;
    }

    public void setLiveItem2Title(String title) {
        this.items[1].title.setValue(title);
    }

    public LiveData<String> getLiveItem2Comment() {
        return this.items[1].comment;
    }

    public LiveData<String> getLiveItem3Title() {
        return this.items[2].title;
    }

    public void setLiveItem3Title(String title) {
        this.items[2].title.setValue(title);
    }

    public LiveData<String> getLiveItem3Comment() {
        return this.items[2].comment;
    }

    public LiveData<String> getLiveItem4Title() {
        return this.items[3].title;
    }

    public void setLiveItem4Title(String title) {
        this.items[3].title.setValue(title);
    }

    public LiveData<String> getLiveItem4Comment() {
        return this.items[3].comment;
    }

    public LiveData<String> getLiveItem5Title() {
        return this.items[4].title;
    }

    public void setLiveItem5Title(String title) {
        this.items[4].title.setValue(title);
    }

    public LiveData<String> getLiveItem5Comment() {
        return this.items[4].comment;
    }

}
