package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;

import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;

import java.time.DayOfWeek;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SettingsViewModel extends ViewModel {

    private SettingsRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<String> themeColor = new MutableLiveData<>();
    private final MutableLiveData<String> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGettingWeatherInformation = new MutableLiveData<>();

    public SettingsViewModel(SettingsRepository repository) {
        this.repository = repository;

        Flowable<String> themeColorNameFlowable = this.repository.loadThemeColorName();
        this.disposables.add(themeColorNameFlowable
                .subscribe(this.themeColor::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<String> calendarStartDayOfWeekNameFlowable =
                this.repository.loadCalendarStartDayOfWeekName();
        this.disposables.add(calendarStartDayOfWeekNameFlowable
                .subscribe(this.calendarStartDayOfWeek::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isReminderNotificationFlowable =
                this.repository.loadIsReminderNotification();
        this.disposables.add(isReminderNotificationFlowable
                .subscribe(value -> {
                    Log.d("20240708", String.valueOf(value));
                    this.isReminderNotification.postValue(value);
                }, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isPasscodeLockFlowable =
                this.repository.loadIsPasscodeLock();
        this.disposables.add(isPasscodeLockFlowable
                .subscribe(this.isPasscodeLock::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isGettingWeatherInformationFlowable =
                this.repository.loadIsGettingWeatherInformation();
        this.disposables.add(isGettingWeatherInformationFlowable
                .subscribe(this.isGettingWeatherInformation::postValue, throwable -> {
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

    public Single<Preferences> saveIsReminderNotification(boolean value) {
        return this.repository.saveIsReminderNotification(value);
    }

    public Single<Preferences> saveIsPasscodeLock(boolean value) {
        return this.repository.saveIsPasscodeLock(value);
    }

    public Single<Preferences> saveIsGettingWeatherInformation(boolean value) {
        return this.repository.saveIsGettingWeatherInformation(value);
    }

    // Getter/Setter
    public LiveData<String> getLiveDataThemeColor() {
        return this.themeColor;
    }

    public LiveData<String> getLiveDataCalendarStartDayOfWeek() {
        return this.calendarStartDayOfWeek;
    }

    public LiveData<Boolean> getLiveDataIsReminderNotification() {
        return this.isReminderNotification;
    }

    public LiveData<Boolean> getLiveDataIsPasscodeLock() {
        return this.isPasscodeLock;
    }

    public LiveData<Boolean> getLiveDataIsGettingWeatherInformation() {
        return this.isGettingWeatherInformation;
    }


}
