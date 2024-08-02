package com.websarva.wings.android.zuboradiary.ui.observer;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

public class ShowDiaryWeather2Observer implements Observer<Integer> {
    TextView slush;
    TextView weather2;

    public ShowDiaryWeather2Observer(TextView slush, TextView weather2) {
        this.slush = slush;
        this.weather2 = weather2;
    }

    @Override
    public void onChanged(Integer integer) {
        if (integer == null) {
            return;
        }
        if (integer > 0 && integer <= Weathers.values().length - 1) {
            slush.setVisibility(View.VISIBLE);
            weather2.setVisibility(View.VISIBLE);
        } else {
            slush.setVisibility(View.GONE);
            weather2.setVisibility(View.GONE);
        }
    }
}
