package com.websarva.wings.android.zuboradiary.ui.calendar;

import androidx.lifecycle.ViewModel;

public class CalendarViewModel extends ViewModel {

    private long backupCalendarDate;

    public CalendarViewModel() {
        backupCalendarDate = -1;
    }

    public long getBackupCalendarDate() {
        return this.backupCalendarDate;
    }
    public void setBackupCalendarDate(long backupCalendarDate) {
        this.backupCalendarDate = backupCalendarDate;
    }
}
