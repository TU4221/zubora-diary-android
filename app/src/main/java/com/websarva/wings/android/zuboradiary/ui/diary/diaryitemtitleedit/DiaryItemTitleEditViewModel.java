package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryItemTitleEditViewModel extends ViewModel {

    private final DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository;
    private final MutableLiveData<List<DiaryItemTitleSelectionHistoryItem>> itemTitleSelectionHistory =
            new MutableLiveData<>();
    private final int MAX_LOADED_ITEM_TITLES = 50;

    // エラー関係
    private final MutableLiveData<Boolean> isItemTitleSelectionHistoryLoadingError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isItemTitleSelectionHistoryItemDeleteError = new MutableLiveData<>();

    @Inject
    public DiaryItemTitleEditViewModel(
            DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository) {
        this.diaryItemTitleSelectionHistoryRepository = diaryItemTitleSelectionHistoryRepository;
        itemTitleSelectionHistory.setValue(new ArrayList<>());
        isItemTitleSelectionHistoryLoadingError.setValue(false);
        isItemTitleSelectionHistoryItemDeleteError.setValue(false);
    }

    public void loadSelectedItemTitleHistory() {
        List<DiaryItemTitleSelectionHistoryItem> loadedList;
        try {
            loadedList =
                    diaryItemTitleSelectionHistoryRepository
                            .loadSelectedDiaryItemTitles(MAX_LOADED_ITEM_TITLES,0).get();
        } catch (Exception e) {
            isItemTitleSelectionHistoryLoadingError.setValue(true);
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
            diaryItemTitleSelectionHistoryRepository.deleteSelectedDiaryItemTitle(diaryItemTitleSelectionHistoryItem).get();
        } catch (Exception e) {
            isItemTitleSelectionHistoryItemDeleteError.setValue(true);
            return;
        }
        currentList.remove(deletePosition);
        itemTitleSelectionHistory.setValue(currentList);
    }

    // Error関係
    public void clearSelectedItemTitleListLoadingError() {
        isItemTitleSelectionHistoryLoadingError.setValue(false);
    }

    public void clearSelectedItemTitleDeleteError() {
        isItemTitleSelectionHistoryItemDeleteError.setValue(false);
    }

    // LiveDataGetter
    public LiveData<List<DiaryItemTitleSelectionHistoryItem>> getItemTitleSelectionHistoryLiveData() {
        return itemTitleSelectionHistory;
    }

    public LiveData<Boolean> getIsItemTitleSelectionHistoryLoadingErrorLiveData() {
        return isItemTitleSelectionHistoryLoadingError;
    }

    public LiveData<Boolean> getIsItemTitleSelectionHistoryItemDeleteErrorLiveData() {
        return isItemTitleSelectionHistoryItemDeleteError;
    }
}
