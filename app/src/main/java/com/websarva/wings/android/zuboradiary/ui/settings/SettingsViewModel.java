package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;

import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.SettingsRepository;

import java.time.DayOfWeek;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SettingsViewModel extends ViewModel {

    private SettingsRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<String> themeColor = new MutableLiveData<>();
    private final MutableLiveData<String> calendarStartDayOfWeek = new MutableLiveData<>();

    public SettingsViewModel(SettingsRepository repository) {
        this.repository = repository;

        Flowable<String> themeColorNameFlowable = this.repository.loadThemeColorName();
        this.disposables.add(themeColorNameFlowable
                .subscribe(themeColor::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<String> calendarStartDayOfWeekNameFlowable =
                this.repository.loadCalendarStartDayOfWeekName();
        this.disposables.add(calendarStartDayOfWeekNameFlowable
                .subscribe(calendarStartDayOfWeek::postValue, throwable -> {
                    throw throwable;
                })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public Single<Preferences> saveThemeColor(ThemeColors value) {
        return this.repository.saveThemeColor(value);
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.repository.saveCalendarStartDayOfWeek(value);
    }

    // Getter/Setter
    public LiveData<String> getLiveDataThemeColor() {
        return this.themeColor;
    }

    public LiveData<String> getLiveDataCalendarStartDayOfWeek() {
        return this.calendarStartDayOfWeek;
    }


}
