package com.websarva.wings.android.zuboradiary;

import android.content.Context;
import android.util.DisplayMetrics;

public class UnitConverter {
    public static float convertPx(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.density;
    }

    public static float convertDp(int px, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
}
