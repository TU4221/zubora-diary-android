package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.websarva.wings.android.zuboradiary.ui.diary.DiaryRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private DiaryRepository diaryRepository;
    private Map<Integer, Map<Integer, List<Integer>>> existedDiaryDateMap = new HashMap<>();
    private LocalDate selectedDate;

    public CalendarViewModel(@NonNull Application application) {
        super(application);

        diaryRepository = new DiaryRepository(getApplication());
    }

    //既存日記の日付格納
    public void updateExistedDiaryDateLog(int year, int month) throws Exception {
        List<Integer> _existedDiaryDateListForOneMonth =
                diaryRepository.selectDiaryDateList(year, month);
        if (this.existedDiaryDateMap.containsKey(year)) {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap =
                    this.existedDiaryDateMap.get(year);
            existedDiaryDateMonthMap.put(month, _existedDiaryDateListForOneMonth);
        } else {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap = new HashMap<>();
            existedDiaryDateMonthMap.put(month, _existedDiaryDateListForOneMonth);
            this.existedDiaryDateMap.put(year, existedDiaryDateMonthMap);
        }
    }

    public void clearExistedDiaryDateMap() {
        this.existedDiaryDateMap.clear();

    }

    public boolean existsDiary(int year, int month, int dayOfMonth) {
        if (this.existedDiaryDateMap.containsKey(year)) {
            Map<Integer, List<Integer>> existedDiaryDateMonthMap =
                    this.existedDiaryDateMap.get(year);
            List<Integer> existedDiaryDateDayOfMonthList = existedDiaryDateMonthMap.get(month);
            for (int number: existedDiaryDateDayOfMonthList) {
                if (number == dayOfMonth) {
                    return true;
                }
            }
        }
        return false;
    }

    public LocalDate getSelectedDate() {
        return this.selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

}
