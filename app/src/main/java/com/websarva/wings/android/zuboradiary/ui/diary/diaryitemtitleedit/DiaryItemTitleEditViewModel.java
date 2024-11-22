package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository;
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DiaryItemTitleEditViewModel extends BaseViewModel {

    private final DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository;
    private final MutableLiveData<ItemNumber> itemNumber = new MutableLiveData<>();
    private final MutableLiveData<String> itemTitle = new MutableLiveData<>();
    private final MutableLiveData<SelectionHistoryList> selectionHistoryList = new MutableLiveData<>();
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
        itemNumber.setValue(null);
        itemTitle.setValue("");
        selectionHistoryList.setValue(new SelectionHistoryList());
    }

    void updateDiaryItemTitle(ItemNumber itemNumber, String itemTitle) {
        Objects.requireNonNull(itemNumber);
        Objects.requireNonNull(itemTitle);

        this.itemNumber.setValue(itemNumber);
        this.itemTitle.setValue(itemTitle);
    }

   void loadDiaryItemTitleSelectionHistory() {
        List<DiaryItemTitleSelectionHistoryItemEntity> loadedList;
        try {
            loadedList =
                    diaryItemTitleSelectionHistoryRepository
                            .loadSelectionHistory(MAX_LOADED_ITEM_TITLES,0).get();
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_ITEM_TITLE_HISTORY_LOADING_ERROR);
            return;
        }
        List<SelectionHistoryListItem> itemList = new ArrayList<>();
        loadedList.stream().forEach(x -> itemList.add(new SelectionHistoryListItem(x)));
        SelectionHistoryList list = new SelectionHistoryList(itemList);
        selectionHistoryList.setValue(list);
    }

    void deleteDiaryItemTitleSelectionHistoryItem(int deletePosition) {
        if (deletePosition < 0) throw new IllegalArgumentException();

        SelectionHistoryList currentList = selectionHistoryList.getValue();
        Objects.requireNonNull(currentList);
        int listSize = currentList.getSelectionHistoryListItemList().size();
        if (deletePosition >= listSize) throw new IllegalArgumentException();

        SelectionHistoryListItem deleteItem =
                currentList.getSelectionHistoryListItemList().get(deletePosition);
        String deleteTitle = deleteItem.getTitle();
        try {
            diaryItemTitleSelectionHistoryRepository.deleteSelectionHistoryItem(deleteTitle).get();
        } catch (Exception e) {
            addAppMessage(AppMessage.DIARY_ITEM_TITLE_HISTORY_ITEM_DELETE_ERROR);
            return;
        }

        selectionHistoryList.setValue(currentList.deleteItem(deletePosition));
    }

    // LiveDataGetter
    @NonNull
    LiveData<ItemNumber> getItemNumberLiveData() {
        return itemNumber;
    }

    @NonNull
    LiveData<String> getItemTitleLiveData() {
        return itemTitle;
    }

    @NonNull
    public MutableLiveData<String> getItemTitleMutableLiveData() {
        return itemTitle;
    }

    @NonNull
    LiveData<SelectionHistoryList> getItemTitleSelectionHistoryLiveData() {
        return selectionHistoryList;
    }
}
