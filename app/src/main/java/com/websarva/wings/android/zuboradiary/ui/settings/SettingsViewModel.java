package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;

import androidx.annotation.FloatRange;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private SettingsRepository settingsRepository;
    private WorkerRepository workerRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    // TODO:変数名を統一する。selected～、isChecked～。
    private final MutableLiveData<String> themeColor = new MutableLiveData<>();
    private final MutableLiveData<String> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Integer> calendarStartDayOfWeekNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedGettingWeatherInformation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasUpdatedLocation = new MutableLiveData<>(false);
    @FloatRange(from = -90.0, to = 90.0)
    private double latitude = 0;
    @FloatRange(from = -180.0, to = 180.0)
    private double longitude = 0;

    @Inject
    public SettingsViewModel(SettingsRepository settingsRepository, WorkerRepository workerRepository) {
        this.settingsRepository = settingsRepository;
        this.workerRepository = workerRepository;

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

        Flowable<Integer> calendarStartDayOfWeekNumberFlowable =
                this.settingsRepository.loadCalendarStartDayOfWeekNumber();
        this.disposables.add(calendarStartDayOfWeekNumberFlowable
                .subscribe(this.calendarStartDayOfWeekNumber::postValue, throwable -> {
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

    public void updateLocationInformation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        hasUpdatedLocation.setValue(true);
    }

    public void clearLocationInformation() {
        hasUpdatedLocation.setValue(false);
        latitude = 0;
        longitude = 0;
    }

    // Getter/Setter
    public LiveData<String> getThemeColorLiveData() {
        return this.themeColor;
    }

    public LiveData<String> getCalendarStartDayOfWeekLiveData() {
        return this.calendarStartDayOfWeek;
    }

    public LiveData<Integer> getCalendarStartDayOfWeekNumberLiveData() {
        return this.calendarStartDayOfWeekNumber;
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

    public LiveData<Boolean> getHasUpdatedLocationLiveData() {
        return hasUpdatedLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
