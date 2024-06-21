package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.websarva.wings.android.zuboradiary.DateConverter;
import com.websarva.wings.android.zuboradiary.ui.diary.Diary;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private DiaryRepository diaryRepository;
    private Map<String, List<String>> existedDiaryDateLog = new HashMap<>();
    private LocalDate selectedDate;

    public CalendarViewModel(@NonNull Application application) {
        super(application);

        diaryRepository = new DiaryRepository(getApplication());
    }

    //既存日記の日付格納
    public void updateExistedDiaryDateLog(String dateYearMonth) throws Exception {
        List<String> existedDiaryDateListForOneMonth =
                diaryRepository.selectDiaryDateList(dateYearMonth);
        existedDiaryDateLog.put(dateYearMonth, existedDiaryDateListForOneMonth);

    }

    public void clearExistedDiaryDateLog() {
        existedDiaryDateLog.clear();

    }

    public boolean existsDiary(String targetDate) {
        String targetDateYearMonth = DateConverter.toStringLocalDateYearMonth(targetDate);

        if (existedDiaryDateLog.containsKey(targetDateYearMonth)) {
            List<String> list = existedDiaryDateLog.get(targetDateYearMonth);
            for (String existedDiaryDate: list) {
                if (targetDate.equals(existedDiaryDate)) {
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
