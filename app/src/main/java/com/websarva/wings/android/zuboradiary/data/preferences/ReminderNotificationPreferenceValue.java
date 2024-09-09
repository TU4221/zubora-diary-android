package com.websarva.wings.android.zuboradiary.data.preferences;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.WindowDecorActionBar;

import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

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
        this.notificationTime = notificationTime.toString();
    }

    public ReminderNotificationPreferenceValue(Boolean isChecked, String notificationTime) {
        if (isChecked == null) {
            throw new NullPointerException();
        }
        if (notificationTime == null) {
            throw new NullPointerException();
        }
        boolean isFormat;
        try {
            LocalTime.parse(notificationTime);
            isFormat = true;
        } catch (DateTimeParseException e) {
            isFormat = false;
        }
        if (isChecked && (notificationTime.isEmpty() || !isFormat)) {
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

        return LocalTime.parse(notificationTime);
    }
}
