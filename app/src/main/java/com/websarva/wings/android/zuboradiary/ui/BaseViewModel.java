package com.websarva.wings.android.zuboradiary.ui;

import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.AppMessageList;

import java.util.Objects;

public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<AppMessageList> appMessageBufferList = new MutableLiveData<>();

    public BaseViewModel() {
        initializeAppMessageList();
    }

    protected void initializeAppMessageList() {
        appMessageBufferList.setValue(new AppMessageList());
    }

    /**
     * 本メソッドは継承先のクラス毎に処理するタイミングが異なるので、Override後、継承クラスのコンストラクタに含めること。
     * */
    protected abstract void initialize();

    protected final void addAppMessage(AppMessage appMessage) {
        Objects.requireNonNull(appMessage);

        AppMessageList currentList = appMessageBufferList.getValue();
        Objects.requireNonNull(currentList);
        AppMessageList updateList = currentList.add(appMessage);

        boolean isMainThread = (Looper.getMainLooper().getThread() == Thread.currentThread());
        if (isMainThread) {
            appMessageBufferList.setValue(updateList);
        } else {
            appMessageBufferList.postValue(updateList);
        }
    }

    public final void triggerAppMessageBufferListObserver() {
        AppMessageList currentList = appMessageBufferList.getValue();
        appMessageBufferList.setValue(new AppMessageList());
        appMessageBufferList.setValue(currentList);
    }

    public final void removeAppMessageBufferListFirstItem() {
        AppMessageList currentList = appMessageBufferList.getValue();
        Objects.requireNonNull(currentList);
        AppMessageList updateList = currentList.removeFirstItem();
        appMessageBufferList.setValue(updateList);
    }

    protected final boolean equalLastAppMessage(AppMessage appMessage) {
        Objects.requireNonNull(appMessage);

        AppMessageList currentList = appMessageBufferList.getValue();
        Objects.requireNonNull(currentList);
        return currentList.equalLastItem(appMessage);
    }

    public final LiveData<AppMessageList> getAppMessageBufferListLiveData() {
        return appMessageBufferList;
    }
}
