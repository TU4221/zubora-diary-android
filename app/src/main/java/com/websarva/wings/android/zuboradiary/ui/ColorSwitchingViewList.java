package com.websarva.wings.android.zuboradiary.ui;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorSwitchingViewList<T extends View> {
    List<T> viewList;

    // TODO:アノテーションを付けなくても良い方法を検討
    @SafeVarargs
    public ColorSwitchingViewList(T... viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }
        if (viewList.length == 0) {
            throw new IllegalArgumentException();
        }

        this.viewList = Arrays.asList(viewList);
    }

    public List<T> getViewList() {
        return new ArrayList<>(viewList);
    }
}
