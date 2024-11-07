package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ReminderNotificationPreferenceValue {

    private final boolean isChecked;
    private final String notificationTime;

    public ReminderNotificationPreferenceValue(boolean isChecked,@Nullable LocalTime notificationTime) {
        if (isChecked) Objects.requireNonNull(notificationTime);

        this.isChecked = isChecked;
        if (isChecked) {
            this.notificationTime = notificationTime.toString();
        } else {
            this.notificationTime = "";
        }
    }

    public ReminderNotificationPreferenceValue(boolean isChecked,@Nullable String notificationTime) {
        String _notificationTime;
        if (isChecked) {
            Objects.requireNonNull(notificationTime);
            boolean isFormat;
            try {
                LocalTime.parse(notificationTime);
                isFormat = true;
            } catch (DateTimeParseException e) {
                isFormat = false;
            }
            if (notificationTime.isEmpty() || !isFormat) throw new IllegalArgumentException();

            _notificationTime = notificationTime;
        } else {
            _notificationTime = "";
        }

        this.isChecked = isChecked;
        this.notificationTime = _notificationTime;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    @NonNull
    public String getNotificationTimeString() {
        return notificationTime;
    }

    @Nullable
    public LocalTime getNotificationLocalTime() {
        if (!isChecked) return null;

        return LocalTime.parse(notificationTime);
    }
}
