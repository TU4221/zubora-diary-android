package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
class CalendarViewModel extends BaseViewModel {

    private final DiaryRepository diaryRepository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> previousSelectedDate = new MutableLiveData<>();

    @Inject
    CalendarViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        selectedDate.setValue(LocalDate.now());
        previousSelectedDate.setValue(null);
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler;

        private MainThreadExecutor() {
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

    void hasDiary(LocalDate date, FutureCallback<Boolean> futureCallback) {
        ListenableFuture<Boolean> hasDiaryListenableFuture = diaryRepository.hasDiary(date);
        Futures.addCallback(hasDiaryListenableFuture, futureCallback, new MainThreadExecutor());
        Futures.addCallback(hasDiaryListenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // 処理なし
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
                //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
                AppError lastAppError = getAppErrorBufferListLastValue();
                if (lastAppError == AppError.DIARY_INFORMATION_LOADING) return;

                addAppError(AppError.DIARY_INFORMATION_LOADING);
            }
        }, new MainThreadExecutor());
    }

    void updateSelectedDate(LocalDate date) {
        Objects.requireNonNull(date);

        previousSelectedDate.setValue(selectedDate.getValue());
        selectedDate.setValue(date);
    }

    @NonNull
    LiveData<LocalDate> getSelectedDateLiveData() {
        return selectedDate;
    }

    @NonNull
    LiveData<LocalDate> getPreviousSelectedDateLiveData() {
        return previousSelectedDate;
    }

}
