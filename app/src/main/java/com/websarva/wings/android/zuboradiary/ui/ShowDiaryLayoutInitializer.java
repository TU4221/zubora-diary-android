package com.websarva.wings.android.zuboradiary.ui;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;

// TODO:このクラスが最終的に必要か判断すること。
public class ShowDiaryLayoutInitializer {
    public void setUpVisibleWeather2Observer(
            DiaryViewModel diaryViewModel, LifecycleOwner lifecycleOwner, TextView slush, TextView weather2) {
        diaryViewModel.getLiveIntWeather2()
                .observe(lifecycleOwner, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if (integer != 0) {
                            slush.setVisibility(View.VISIBLE);
                            weather2.setVisibility(View.VISIBLE);
                        } else {
                            slush.setVisibility(View.GONE);
                            weather2.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
