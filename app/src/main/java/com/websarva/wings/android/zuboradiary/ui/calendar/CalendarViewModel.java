package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.websarva.wings.android.zuboradiary.ui.DateConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private long backupCalendarDate = -1;
    private CalendarRepository calendarRepository;
    private Map<String, List<String>> existedDiaryDateLog = new HashMap<>();
    private LocalDate selectedDate;

    public CalendarViewModel(@NonNull Application application) {
        super(application);

        calendarRepository = new CalendarRepository(getApplication());
    }

    //既存日記の日付格納
    public void updateExistedDiaryDateLog(String dateYearMonth) {
        List<String> existedDiaryDateListForOneMonth =
                calendarRepository.selectDiaryDateList(dateYearMonth);
        existedDiaryDateLog.put(dateYearMonth, existedDiaryDateListForOneMonth);

    }

    public void clearExistedDiaryDateLog() {
        existedDiaryDateLog.clear();

    }

    public boolean existsDiary(int year, int month, int dayOfMonth) {
        String targetDateYearMonth = DateConverter.toStringLocalDateYearMonth(year, month);
        String targetDate = DateConverter.toStringLocalDate(year, month, dayOfMonth);

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

    public long getBackupCalendarDate() {
        return this.backupCalendarDate;
    }
    public void setBackupCalendarDate(long backupCalendarDate) {
        this.backupCalendarDate = backupCalendarDate;
    }
}
