package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.WindowDecorActionBar;

import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;

import java.time.LocalTime;

public class ReminderNotificationPreferenceValue {
    private final boolean isChecked;
    private final String notificationTime;

    public ReminderNotificationPreferenceValue(Boolean isChecked,@Nullable LocalTime notificationTime) {
        if (isChecked == null) {
            throw new NullPointerException();
        }
        if (isChecked && notificationTime == null) {
            throw new IllegalArgumentException();
        }
        if (!isChecked && notificationTime != null) {
            throw new IllegalArgumentException();
        }

        this.isChecked = isChecked;
        if (!isChecked) {
            this.notificationTime = "";
            return;
        }
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        this.notificationTime = dateTimeStringConverter.toStringTimeHourMinute(notificationTime);
    }

    public ReminderNotificationPreferenceValue(Boolean isChecked, String notificationTime) {
        if (isChecked == null) {
            throw new NullPointerException();
        }
        if (notificationTime == null) {
            throw new NullPointerException();
        }
        DateTimeStringConverter converter = new DateTimeStringConverter();
        if (isChecked && (notificationTime.isEmpty() || !converter.isFormatTimeHourMinute(notificationTime))) {
            throw new IllegalArgumentException();
        }
        if (!isChecked && !notificationTime.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.isChecked = isChecked;
        this.notificationTime = notificationTime;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public String getNotificationTimeString() {
        return notificationTime;
    }

    @Nullable
    public LocalTime getNotificationLocalTime() {
        if (!isChecked) {
            return null;
        }
        DateTimeStringConverter converter = new DateTimeStringConverter();
        return converter.toLocalTimeTimeHourMinute(notificationTime);
    }
}
