package com.websarva.wings.android.zuboradiary.ui.observer;

import android.view.View;

import androidx.lifecycle.Observer;

public class DiaryShowNumVisibleItemsObserver implements Observer<Integer> {
    View[] itemLayouts;

    public DiaryShowNumVisibleItemsObserver(View[] itemLayouts) {
        this.itemLayouts = itemLayouts;
    }

    @Override
    public void onChanged(Integer integer) {
        if (integer == null) {
            return;
        }
        if (integer <= 0 || integer > itemLayouts.length) {
            return;
        }
        for (int i = 0; i < itemLayouts.length; i++) {
            if (i < integer) {
                itemLayouts[i].setVisibility(View.VISIBLE);
            } else {
                itemLayouts[i].setVisibility(View.GONE);
            }
        }
    }
}
