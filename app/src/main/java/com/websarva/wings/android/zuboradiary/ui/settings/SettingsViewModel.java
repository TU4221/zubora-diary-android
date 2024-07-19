package com.websarva.wings.android.zuboradiary.ui.settings;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SettingsViewModel extends ViewModel {
    private SettingsRepository settingsRepository;
    private WorkerRepository workerRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    // TODO:変数名を統一する。selected～、isChecked～。
    private final MutableLiveData<String> themeColor = new MutableLiveData<>();
    private final MutableLiveData<String> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedGettingWeatherInformation = new MutableLiveData<>();

    public SettingsViewModel(Context context, Application application) {
        this.settingsRepository = new SettingsRepository(context);
        this.workerRepository = new WorkerRepository(application);

        Flowable<String> themeColorNameFlowable = this.settingsRepository.loadThemeColorName();
        this.disposables.add(themeColorNameFlowable
                .subscribe(this.themeColor::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<String> calendarStartDayOfWeekNameFlowable =
                this.settingsRepository.loadCalendarStartDayOfWeekName();
        this.disposables.add(calendarStartDayOfWeekNameFlowable
                .subscribe(this.calendarStartDayOfWeek::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isReminderNotificationFlowable =
                this.settingsRepository.loadIsReminderNotification();
        this.disposables.add(isReminderNotificationFlowable
                .subscribe(value -> {
                    Log.d("20240708", String.valueOf(value));
                    this.isCheckedReminderNotification.postValue(value);
                }, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isPasscodeLockFlowable =
                this.settingsRepository.loadIsPasscodeLock();
        this.disposables.add(isPasscodeLockFlowable
                .subscribe(this.isCheckedPasscodeLock::postValue, throwable -> {
                    throw throwable;
                })
        );

        Flowable<Boolean> isGettingWeatherInformationFlowable =
                this.settingsRepository.loadIsGettingWeatherInformation();
        this.disposables.add(isGettingWeatherInformationFlowable
                .subscribe(this.isCheckedGettingWeatherInformation::postValue, throwable -> {
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
        return this.settingsRepository.saveThemeColor(value);
    }

    public Single<Preferences> saveCalendarStartDayOfWeek(DayOfWeek value) {
        return this.settingsRepository.saveCalendarStartDayOfWeek(value);
    }

    public Single<Preferences> saveIsReminderNotification(boolean value) {
        return this.settingsRepository.saveIsReminderNotification(value);
    }

    public Single<Preferences> saveIsPasscodeLock(boolean value) {
        return this.settingsRepository.saveIsPasscodeLock(value);
    }

    public Single<Preferences> saveIsGettingWeatherInformation(boolean value) {
        return this.settingsRepository.saveIsGettingWeatherInformation(value);
    }

    public void registerReminderNotificationWorker(LocalTime settingTime) {
        workerRepository.registerReminderNotificationWorker(settingTime);
    }

    public void cancelReminderNotificationWorker() {
        workerRepository.cancelReminderNotificationWorker();
    }

    // Getter/Setter
    public LiveData<String> getThemeColorLiveData() {
        return this.themeColor;
    }

    public LiveData<String> getCalendarStartDayOfWeekLiveData() {
        return this.calendarStartDayOfWeek;
    }

    public LiveData<Boolean> getIsCheckedReminderNotificationLiveData() {
        return this.isCheckedReminderNotification;
    }

    public LiveData<Boolean> getIsCheckedPasscodeLockLiveData() {
        return this.isCheckedPasscodeLock;
    }

    public LiveData<Boolean> getIsCheckedGettingWeatherInformationLiveData() {
        return this.isCheckedGettingWeatherInformation;
    }


}
