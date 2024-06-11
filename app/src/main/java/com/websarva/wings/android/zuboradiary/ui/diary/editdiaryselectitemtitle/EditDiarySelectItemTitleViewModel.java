package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EditDiarySelectItemTitleViewModel extends AndroidViewModel {

    private EditDiarySelectItemTitleRepository editDiarySelectItemTitleRepository;
    private MutableLiveData<List<DiaryItemTitle>> selectedItemTitleHistory =
            new MutableLiveData<>(new ArrayList<>());
    private final int MAX_DIARY_ITEMS_NUM = 5;
    private DiaryItemTitle[] savingDiaryItemTitles = new DiaryItemTitle[MAX_DIARY_ITEMS_NUM];

    public EditDiarySelectItemTitleViewModel(@NonNull Application application) {
        super(application);
        editDiarySelectItemTitleRepository =
                new EditDiarySelectItemTitleRepository(getApplication());

        for (int i = 0; i < savingDiaryItemTitles.length; i++) {
            savingDiaryItemTitles[i] = new DiaryItemTitle();
        }
    }

    public boolean hasSelectedItemTitleHistory() {
        int selectedItemTitlesNum = editDiarySelectItemTitleRepository.countSelectedItemTitles();
        return selectedItemTitlesNum >= 1;
    }

    public void loadSelectedItemTitleHistory() {
        List<DiaryItemTitle> loadedList = editDiarySelectItemTitleRepository.selectSelectedItemTitles(50);
        this.selectedItemTitleHistory.setValue(loadedList);
    }

    public void updateSelectedItemTitleHistory() {
        List<DiaryItemTitle> updateList = new ArrayList<>();

        for (DiaryItemTitle diaryItemTitle: this.savingDiaryItemTitles) {
            if (diaryItemTitle.getTitle().matches("\\S+.*")) {
                updateList.add(diaryItemTitle);
            }
        }

        if (!updateList.isEmpty()) {
            this.editDiarySelectItemTitleRepository.insertSelectedItemTitles(updateList);
        }

    }

    public void deleteSelectedItemTitle(int deletePos) {
        List<DiaryItemTitle> list  =this.selectedItemTitleHistory.getValue();
        DiaryItemTitle diaryItemTitle = list.get(deletePos);
        this.editDiarySelectItemTitleRepository.deleteSelectedItemTitle(diaryItemTitle);
        list.remove(deletePos);
        this.selectedItemTitleHistory.setValue(list);
    }

    public void updateSavingDiaryItemTitle(int itemNo, String selectedTitle) {
        String stringCurrentDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime localDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
            stringCurrentDate = localDateTime.format(formatter);
        }

        int arrayNo = itemNo - 1;
        savingDiaryItemTitles[arrayNo].setTitle(selectedTitle);
        savingDiaryItemTitles[arrayNo].setLog(stringCurrentDate);
    }

    public void deleteSavingDiaryItemTitle(int itemNo) {
        int deleteArrayNo = itemNo - 1;
        this.savingDiaryItemTitles[deleteArrayNo].setTitle("");
        this.savingDiaryItemTitles[deleteArrayNo].setLog("");

        int nextArrayNo = -1;
        for (int arrayNo = deleteArrayNo; arrayNo < (MAX_DIARY_ITEMS_NUM - 1); arrayNo++) {
            nextArrayNo = arrayNo + 1;
            this.savingDiaryItemTitles[arrayNo].setTitle(savingDiaryItemTitles[nextArrayNo].getTitle());
            this.savingDiaryItemTitles[arrayNo].setLog(savingDiaryItemTitles[nextArrayNo].getLog());
            this.savingDiaryItemTitles[nextArrayNo].setTitle("");
            this.savingDiaryItemTitles[nextArrayNo].setLog("");
        }
    }

    public LiveData<List<DiaryItemTitle>> getLiveSelectedItemTitleHistory() {
        return selectedItemTitleHistory;
    }

    public void test() {
        DiaryItemTitle diaryItemTitle = new DiaryItemTitle();
        List<DiaryItemTitle> list = new ArrayList<>();
        for (int i = 10; i < 30; i++) {
            diaryItemTitle.setTitle(String.valueOf(i));
            diaryItemTitle.setLog("2024年03月25日 16:30:" + String.valueOf(i));
            list.add(diaryItemTitle);
            diaryItemTitle = new DiaryItemTitle();
        }
        this.editDiarySelectItemTitleRepository.insertSelectedItemTitles(list);
    }
}
