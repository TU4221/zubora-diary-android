package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.SettingsRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.LocalDate;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends BaseViewModel {

    private final DiaryRepository diaryRepository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> lastSelectedDate = new MutableLiveData<>();

    // TODO:SettingsRepository不要確認後削除
    @Inject
    public CalendarViewModel(DiaryRepository diaryRepository, SettingsRepository settingsRepository) {
        this.diaryRepository = diaryRepository;
        initialize();
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

    public void hasDiary(LocalDate date, FutureCallback<Boolean> futureCallback) {
        ListenableFuture<Boolean> hasDiaryListenableFuture = diaryRepository.hasDiary(date);
        Futures.addCallback(hasDiaryListenableFuture, futureCallback, new MainThreadExecutor());
        Futures.addCallback(hasDiaryListenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
            }

            @Override
            public void onFailure(Throwable t) {
                // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
                //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
                AppError lastAppError = getAppErrorBufferListLastValue();
                if (lastAppError == AppError.DIARY_INFORMATION_LOADING) {
                    return;
                }

                addAppError(AppError.DIARY_INFORMATION_LOADING);
            }
        }, new MainThreadExecutor()); //TODO:MainThreadじゃなくてもいいかも
    }

    public void updateSelectedDate(LocalDate date) {
        lastSelectedDate.setValue(selectedDate.getValue());
        selectedDate.setValue(date);
    }

    public LiveData<LocalDate> getSelectedDateLiveData() {
        return selectedDate;
    }

    public LiveData<LocalDate> getLastSelectedDateLiveData() {
        return lastSelectedDate;
    }



}
