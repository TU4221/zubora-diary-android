package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final DiaryRepository diaryRepository;
    private LocalDate selectedDate;
    private final MutableLiveData<Boolean> isDiaryLoadingError = new MutableLiveData<>();

    @Inject
    public CalendarViewModel(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
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
                isDiaryLoadingError.setValue(true);
            }
        }, new MainThreadExecutor());
    }

    // Getter/Setter
    public LocalDate getSelectedDate() {
        return this.selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public LiveData<Boolean> getIsDiaryLoadingErrorLiveData() {
        return isDiaryLoadingError;
    }

    public void setIsDiaryLoadingErrorLiveData(boolean bool) {
        isDiaryLoadingError.setValue(bool);
    }

}
