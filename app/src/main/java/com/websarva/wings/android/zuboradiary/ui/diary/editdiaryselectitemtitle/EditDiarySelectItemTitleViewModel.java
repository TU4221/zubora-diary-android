package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class EditDiarySelectItemTitleViewModel extends AndroidViewModel {

    private EditDiarySelectItemTitleRepository editDiarySelectItemTitleRepository;
    private MutableLiveData<List<SelectedDiaryItemTitle>> selectedItemTitleHistory =
            new MutableLiveData<>(new ArrayList<>());
    private final int MAX_LOADED_ITEM_TITLES = 50;

    public EditDiarySelectItemTitleViewModel(@NonNull Application application) {
        super(application);
        editDiarySelectItemTitleRepository =
                new EditDiarySelectItemTitleRepository(getApplication());
    }

    public void loadSelectedItemTitleHistory() throws Exception {
        List<SelectedDiaryItemTitle> loadedList =
                editDiarySelectItemTitleRepository
                        .loadSelectedDiaryItemTitles(MAX_LOADED_ITEM_TITLES,0);
        this.selectedItemTitleHistory.setValue(loadedList);
    }

    public void deleteSelectedItemTitleHistoryItem(int deletePosition) throws Exception {
        List<SelectedDiaryItemTitle> currentList  =this.selectedItemTitleHistory.getValue();
        SelectedDiaryItemTitle selectedDiaryItemTitle = currentList.get(deletePosition);
        this.editDiarySelectItemTitleRepository.deleteSelectedDiaryItemTitle(selectedDiaryItemTitle);
        currentList.remove(deletePosition);
        this.selectedItemTitleHistory.setValue(currentList);
    }

    // Getter/Setter
    public LiveData<List<SelectedDiaryItemTitle>> getLiveSelectedItemTitleHistory() {
        return selectedItemTitleHistory;
    }
}
