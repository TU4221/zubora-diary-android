package com.websarva.wings.android.zuboradiary.ui.observer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

public class ShowDiaryWeather2Observer implements Observer<Weathers> {
    Context context;
    TextView slush;
    TextView textWeather;

    public ShowDiaryWeather2Observer(Context context, TextView slush, TextView textWeather) {
        this.context = context;
        this.slush = slush;
        this.textWeather = textWeather;
    }

    @Override
    public void onChanged(Weathers weather) {
        if (weather == null || weather == Weathers.UNKNOWN) {
            slush.setVisibility(View.GONE);
            textWeather.setVisibility(View.GONE);
            textWeather.setText(Weathers.UNKNOWN.toString(context));
        } else {
            slush.setVisibility(View.VISIBLE);
            textWeather.setVisibility(View.VISIBLE);
            textWeather.setText(weather.toString(context));
        }
    }
}
