package com.websarva.wings.android.zuboradiary.ui.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final DiaryRepository diaryRepository;
    private final Map<Integer, Map<Integer, List<Integer>>> existedDiaryDateMap = new HashMap<>();
    private LocalDate selectedDate;
    private final MutableLiveData<Boolean> isDiaryLoadingError = new MutableLiveData<>();

    @Inject
    public CalendarViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    //既存日記の日付格納
    public boolean updateExistedDiaryDateLog(YearMonth yearMonth) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        List<Integer> existedDiaryDateDayList;
        try {
            existedDiaryDateDayList =
                    diaryRepository.selectDiaryDateDayList(yearMonth);
        } catch (ExecutionException | InterruptedException e) {
            isDiaryLoadingError.setValue(true);
            return false;
        }
        if (this.existedDiaryDateMap.containsKey(year)) {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap =
                    this.existedDiaryDateMap.get(year);
            existedDiaryDateMonthMap.put(month, existedDiaryDateDayList);
        } else {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap = new HashMap<>();
            existedDiaryDateMonthMap.put(month, existedDiaryDateDayList);
            this.existedDiaryDateMap.put(year, existedDiaryDateMonthMap);
        }
        return true;
    }

    public void clearExistedDiaryDateMap() {
        existedDiaryDateMap.clear();
    }

    public boolean existsDiary(LocalDate localDate) {
        if (existedDiaryDateMap.containsKey(localDate.getYear())) {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap =
                                                    existedDiaryDateMap.get(localDate.getYear());
            List<Integer> existedDiaryDateDayOfMonthList =
                    existedDiaryDateMonthMap.get(localDate.getMonthValue());
            for (int number: existedDiaryDateDayOfMonthList) {
                if (number == localDate.getDayOfMonth()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Getter/Setter
    public LocalDate getSelectedDate() {
        return this.selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public LiveData<Boolean> getIsDiaryLoadingErrorLiveData() {
        return isDiaryLoadingError;
    }

    public void setIsDiaryLoadingErrorLiveData(boolean bool) {
        isDiaryLoadingError.setValue(bool);
    }

}
