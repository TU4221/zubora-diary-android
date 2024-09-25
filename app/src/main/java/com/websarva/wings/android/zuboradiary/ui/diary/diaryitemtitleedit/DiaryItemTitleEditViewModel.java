package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryItemTitleEditViewModel extends BaseViewModel {

    private final DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository;
    private final MutableLiveData<Integer> itemNumber = new MutableLiveData<>();
    private final MutableLiveData<String> itemTitle = new MutableLiveData<>();
    private final MutableLiveData<List<DiaryItemTitleSelectionHistoryItem>> itemTitleSelectionHistory =
            new MutableLiveData<>();
    private static final int MAX_LOADED_ITEM_TITLES = 50;

    @Inject
    public DiaryItemTitleEditViewModel(
            DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository) {
        this.diaryItemTitleSelectionHistoryRepository = diaryItemTitleSelectionHistoryRepository;
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        itemNumber.setValue(0);
        itemTitle.setValue("");
        itemTitleSelectionHistory.setValue(new ArrayList<>());
    }

    public void updateItemTitle(int itemNumber, String itemTitle) {
        this.itemNumber.setValue(itemNumber);
        this.itemTitle.setValue(itemTitle);
    }

    public void loadItemTitleSelectionHistory() {
        List<DiaryItemTitleSelectionHistoryItem> loadedList;
        try {
            loadedList =
                    diaryItemTitleSelectionHistoryRepository
                            .selectHistoryOrderByLogDesc(MAX_LOADED_ITEM_TITLES,0).get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_ITEM_TITLE_HISTORY_LOADING);
            return;
        }
        itemTitleSelectionHistory.setValue(loadedList);
    }

    public void deleteSelectedItemTitleHistoryItem(int deletePosition) {
        List<DiaryItemTitleSelectionHistoryItem> currentList = itemTitleSelectionHistory.getValue();
        if (currentList == null) {
            return;
            // TODO:assert
        }
        DiaryItemTitleSelectionHistoryItem diaryItemTitleSelectionHistoryItem = currentList.get(deletePosition);
        try {
            diaryItemTitleSelectionHistoryRepository.deleteHistoryItem(diaryItemTitleSelectionHistoryItem).get();
        } catch (Exception e) {
            addAppError(AppError.DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE);
            return;
        }

        List<DiaryItemTitleSelectionHistoryItem> cloneList = new ArrayList<>();
        for (DiaryItemTitleSelectionHistoryItem i: currentList) {
            cloneList.add(i.clone());
        }
        cloneList.remove(deletePosition);
        itemTitleSelectionHistory.setValue(cloneList);
    }

    // LiveDataGetter
    public LiveData<Integer> getItemNumberLiveData() {
        return itemNumber;
    }

    public LiveData<String> getItemTitleLiveData() {
        return itemTitle;
    }

    public MutableLiveData<String> getItemTitleMutableLiveData() {
        return itemTitle;
    }

    public LiveData<List<DiaryItemTitleSelectionHistoryItem>> getItemTitleSelectionHistoryLiveData() {
        return itemTitleSelectionHistory;
    }
}
