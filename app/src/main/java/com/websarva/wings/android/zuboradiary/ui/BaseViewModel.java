package com.websarva.wings.android.zuboradiary.ui;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.AppErrorList;

import java.util.Objects;

public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<AppErrorList> appErrorBufferList = new MutableLiveData<>();

    protected void initialize() {
        appErrorBufferList.setValue(new AppErrorList());
    }

    protected final void addAppError(AppError appError) {
        Objects.requireNonNull(appError);

        AppErrorList currentList = appErrorBufferList.getValue();
        Objects.requireNonNull(currentList);
        AppErrorList updateList = currentList.addAppError(appError);

        boolean isMainThread = (Looper.getMainLooper().getThread() == Thread.currentThread());
        if (isMainThread) {
            appErrorBufferList.setValue(updateList);
        } else {
            appErrorBufferList.postValue(updateList);
        }
    }

    public final void triggerAppErrorBufferListObserver() {
        AppErrorList currentList = appErrorBufferList.getValue();
        appErrorBufferList.setValue(new AppErrorList());
        appErrorBufferList.setValue(currentList);
    }

    public final void removeAppErrorBufferListFirstItem() {
        AppErrorList currentList = appErrorBufferList.getValue();
        Objects.requireNonNull(currentList);
        AppErrorList updateList = currentList.removeFirstAppError();
        appErrorBufferList.setValue(updateList);
    }

    protected final boolean equalLastAppError(AppError appError) {
        Objects.requireNonNull(appError);

        AppErrorList currentList = appErrorBufferList.getValue();
        Objects.requireNonNull(currentList);
        return currentList.equalLastAppError(appError);
    }

    public final LiveData<AppErrorList> getAppErrorBufferListLiveData() {
        return appErrorBufferList;
    }
}
