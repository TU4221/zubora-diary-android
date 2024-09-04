package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {
    private SettingsRepository settingsRepository;
    private WorkerRepository workerRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    // TODO:変数名を統一する。selected～、isChecked～。
    // TODO:エラー変数を用意。Activityフラグメントで管理？
    private final MutableLiveData<String> themeColor = new MutableLiveData<>();
    private final MutableLiveData<String> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Integer> calendarStartDayOfWeekNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<String> reminderNotificationTime = new MutableLiveData<>();
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

        initialize();

        Flowable<String> themeColorNameFlowable = settingsRepository.loadThemeColorName();
        disposables.add(themeColorNameFlowable
                .subscribe(themeColor::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<String> calendarStartDayOfWeekNameFlowable =
                settingsRepository.loadCalendarStartDayOfWeekName();
        disposables.add(calendarStartDayOfWeekNameFlowable
                .subscribe(calendarStartDayOfWeek::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<Integer> calendarStartDayOfWeekNumberFlowable =
                settingsRepository.loadCalendarStartDayOfWeekNumber();
        disposables.add(calendarStartDayOfWeekNumberFlowable
                .subscribe(calendarStartDayOfWeekNumber::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<Boolean> isReminderNotificationFlowable =
                settingsRepository.loadIsReminderNotification();
        disposables.add(isReminderNotificationFlowable
                .subscribe(value -> {
                    Log.d("20240708", String.valueOf(value));
                    isCheckedReminderNotification.postValue(value);
                }, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<String> reminderNotificationTimeFlowable =
                settingsRepository.loadReminderNotificationTime();
        disposables.add(reminderNotificationTimeFlowable
                .subscribe(reminderNotificationTime::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<Boolean> isPasscodeLockFlowable =
                settingsRepository.loadIsPasscodeLock();
        disposables.add(isPasscodeLockFlowable
                .subscribe(isCheckedPasscodeLock::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );

        Flowable<Boolean> isGettingWeatherInformationFlowable =
                settingsRepository.loadIsGettingWeatherInformation();
        disposables.add(isGettingWeatherInformationFlowable
                .subscribe(isCheckedGettingWeatherInformation::postValue, throwable -> {
                    addSettingLoadingError();
                })
        );
    }

    private void addSettingLoadingError() {
        AppError lastAppError = getAppErrorBufferListLastValue();
        if (lastAppError == AppError.SETTING_LOADING) {
            return;
        }
        addAppError(AppError.SETTING_LOADING);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public void saveThemeColor(ThemeColors value) {
        Single<Preferences> result = settingsRepository.saveThemeColor(value);
        setUpProcessCompletedUpdate(result);
    }

    public void saveCalendarStartDayOfWeek(DayOfWeek value) {
        Single<Preferences> result = settingsRepository.saveCalendarStartDayOfWeek(value);
        setUpProcessCompletedUpdate(result);
    }

    public void saveIsReminderNotification(boolean value) {
        Single<Preferences> result = settingsRepository.saveIsReminderNotification(value);
        setUpProcessCompletedUpdate(result);
    }

    public void saveReminderNotificationTime(@NonNull LocalTime settingTime) {
        Single<Preferences> result = settingsRepository.saveReminderNotificationTime(settingTime);
        setUpProcessCompletedUpdate(result);
    }

    public void saveIsPasscodeLock(boolean value) {
        Single<Preferences> result = settingsRepository.saveIsPasscodeLock(value);
        setUpProcessCompletedUpdate(result);
    }

    public void saveIsGettingWeatherInformation(boolean value) {
        Single<Preferences> result = settingsRepository.saveIsGettingWeatherInformation(value);
        setUpProcessCompletedUpdate(result);
    }

    private void setUpProcessCompletedUpdate(Single<Preferences> result) {
        disposables.add(result.subscribe(new Consumer<Preferences>() {
            @Override
            public void accept(Preferences preferences) throws Throwable {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                AppError lastAppError = getAppErrorBufferListLastValue();
                if (lastAppError == AppError.SETTING_UPDATE) {
                    return;
                }
                addAppError(AppError.SETTING_UPDATE);
            }
        }));
    }

    public void registerReminderNotificationWorker(@NonNull LocalTime settingTime) {
        workerRepository.registerReminderNotificationWorker(settingTime);
        settingsRepository.saveReminderNotificationTime(settingTime);
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
        return themeColor;
    }

    public LiveData<String> getCalendarStartDayOfWeekLiveData() {
        return calendarStartDayOfWeek;
    }

    public LiveData<Integer> getCalendarStartDayOfWeekNumberLiveData() {
        return calendarStartDayOfWeekNumber;
    }

    public LiveData<Boolean> getIsCheckedReminderNotificationLiveData() {
        return isCheckedReminderNotification;
    }

    public LiveData<Boolean> getIsCheckedPasscodeLockLiveData() {
        return isCheckedPasscodeLock;
    }

    public LiveData<Boolean> getIsCheckedGettingWeatherInformationLiveData() {
        return isCheckedGettingWeatherInformation;
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
