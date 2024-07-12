package com.websarva.wings.android.zuboradiary.ui;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.websarva.wings.android.zuboradiary.ui.settings.SettingsViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private Context context;
    private Application application;

    public ViewModelFactory(@NonNull Context context, @NonNull Application application) {
        this.context = context;
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(context, application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    ;
}
