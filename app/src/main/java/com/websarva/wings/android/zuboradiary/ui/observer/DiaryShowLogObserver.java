package com.websarva.wings.android.zuboradiary.ui.observer;

import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;

import java.time.LocalDateTime;

public class DiaryShowLogObserver implements Observer<LocalDateTime> {
    TextView textLog;

    public DiaryShowLogObserver(TextView textLog) {
        this.textLog = textLog;
    }

    @Override
    public void onChanged(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return;
        }
        DateTimeStringConverter dateTimeStringConverter = new DateTimeStringConverter();
        String strDate = dateTimeStringConverter.toStringDateTime(localDateTime);
        textLog.setText(strDate);
    }
}
