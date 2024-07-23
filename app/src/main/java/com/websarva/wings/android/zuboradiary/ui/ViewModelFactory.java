package com.websarva.wings.android.zuboradiary.ui;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.websarva.wings.android.zuboradiary.ui.diary.DiaryViewModel;
import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private Context context;

    public ViewModelFactory(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DiaryViewModel.class)) {
            return (T) new DiaryViewModel(context);
        }
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    ;
}
