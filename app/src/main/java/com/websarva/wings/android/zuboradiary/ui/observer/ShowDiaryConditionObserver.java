package com.websarva.wings.android.zuboradiary.ui.observer;

import android.content.Context;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.websarva.wings.android.zuboradiary.data.diary.Conditions;
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;

public class ShowDiaryConditionObserver implements Observer<Conditions> {
    Context context;
    TextView textCondition;

    public ShowDiaryConditionObserver(Context context, TextView textCondition) {
        this.context = context;
        this.textCondition = textCondition;
    }

    @Override
    public void onChanged(Conditions condition) {
        if (condition == null) {
            return;
        }
        textCondition.setText(condition.toString(context));
    }
}
