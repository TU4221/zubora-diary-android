package com.websarva.wings.android.zuboradiary.ui;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppError;

import java.util.ArrayList;
import java.util.List;

public class BaseViewModel extends ViewModel {
    private final MutableLiveData<List<AppError>> appErrorBufferList = new MutableLiveData<>();

    public BaseViewModel() {
    }

    protected void initialize() {
        appErrorBufferList.setValue(new ArrayList<>());
    }

    protected final void addAppError(AppError appError) {
        if (appError == null) {
            throw new NullPointerException();
        }

        List<AppError> appErrorBufferList = getAppErrorBufferListNonNullValue();
        appErrorBufferList.add(appError);

        boolean isMainThread = (Looper.getMainLooper().getThread() == Thread.currentThread());
        if (isMainThread) {
            this.appErrorBufferList.setValue(new ArrayList<>(appErrorBufferList));
        } else {
            this.appErrorBufferList.postValue(new ArrayList<>(appErrorBufferList));
        }
    }

    public final void triggerObserver() {
        List<AppError> _appErrorBufferList = appErrorBufferList.getValue();
        appErrorBufferList.setValue(new ArrayList<>(_appErrorBufferList));
    }

    public final void removeAppErrorBufferListFirstItem() {
        List<AppError> appErrorBufferList = getAppErrorBufferListNonNullValue();
        if (appErrorBufferList.isEmpty()) {
            this.appErrorBufferList.setValue(new ArrayList<>());
        }

        appErrorBufferList.remove(0);
        this.appErrorBufferList.setValue(new ArrayList<>(appErrorBufferList));
    }

    @NonNull
    protected final List<AppError> getAppErrorBufferListNonNullValue() {
        List<AppError> appErrorBufferList = this.appErrorBufferList.getValue();
        if (appErrorBufferList == null) {
            throw new NullPointerException();
        }
        return appErrorBufferList;
    }

    protected final LiveData<List<AppError>> getAppErrorBufferListLiveData() {
        return appErrorBufferList;
    }
}
