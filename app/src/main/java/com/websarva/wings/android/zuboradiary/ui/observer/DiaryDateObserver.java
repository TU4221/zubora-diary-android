package com.websarva.wings.android.zuboradiary.ui.observer;

import android.content.Context;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

public class DiaryDateObserver implements Observer<Weathers> {
    Context context;
    TextView textWeather;

    public DiaryDateObserver(Context context, TextView textWeather) {
        this.context = context;
        this.textWeather = textWeather;
    }

    @Override
    public void onChanged(Weathers weather) {
        if (weather == null) {
            return;
        }
        textWeather.setText(weather.toString(context));
    }
}
