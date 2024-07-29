package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.database.EditDiarySelectItemTitleRepository;
import com.websarva.wings.android.zuboradiary.data.database.SelectedDiaryItemTitle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditDiarySelectItemTitleViewModel extends ViewModel {

    private EditDiarySelectItemTitleRepository editDiarySelectItemTitleRepository;
    private MutableLiveData<List<SelectedDiaryItemTitle>> selectedItemTitleHistory =
            new MutableLiveData<>(new ArrayList<>());
    private final int MAX_LOADED_ITEM_TITLES = 50;

    @Inject
    public EditDiarySelectItemTitleViewModel(
            EditDiarySelectItemTitleRepository editDiarySelectItemTitleRepository) {
        this.editDiarySelectItemTitleRepository = editDiarySelectItemTitleRepository;
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
