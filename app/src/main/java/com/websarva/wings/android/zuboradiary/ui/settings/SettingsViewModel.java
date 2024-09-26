package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.GettingWeatherInformationPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {
    private final SettingsRepository settingsRepository;
    private final WorkerRepository workerRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    // TODO:変数名を統一する。selected～、isChecked～。
    // TODO:エラー変数を用意。Activityフラグメントで管理？
    private final MutableLiveData<ThemeColor> themeColor = new MutableLiveData<>();
    private final MutableLiveData<DayOfWeek> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<LocalTime> reminderNotificationTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedGettingWeatherInformation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasUpdatedLocation = new MutableLiveData<>(false);
    @FloatRange(from = -90.0, to = 90.0)
    private double latitude = 0;
    @FloatRange(from = -180.0, to = 180.0)
    private double longitude = 0;
    List<Flowable<?>> prefelencesFlowableList = new ArrayList<>();
    Flowable<ThemeColorPreferenceValue> themeColorPreferenceValueFlowable;

    @Inject
    public SettingsViewModel(SettingsRepository settingsRepository, WorkerRepository workerRepository) {
        this.settingsRepository = settingsRepository;
        this.workerRepository = workerRepository;

        initialize();
        setUpLoadingThemeColorPreferenceValue();
        setUpLoadingCalendarStartDayOfWeekPreferenceValue();
        setUpLoadingReminderNotificationPreferenceValue();
        setUpLoadingPasscodeLockPreferenceValue();
        setUpLoadingGettingWeatherInformationPreferenceValue();
    }

    // TODO:PreferenceValue読込エラーが発生した時、再読み込みさせるにはメソッドを再び呼び出す必要がある。現時点で考えれる対策を下記にまとめる。
    //      1.各FragmentからSettingViewModelを参照する時に必要設定値の読み出しエラーフラグ(未作成)を確認して都度初期化する。
    //        (対象Fragmentに不必要な設定がエラーの時でも処理が必要となる)
    //      2.各FragmentがもつViewModelに都度PreferenceValueを読みこませる。(読込までのラグが発生してしまう。)
    private void setUpLoadingThemeColorPreferenceValue() {
        Flowable<ThemeColorPreferenceValue> preferenceValueFlowable =
                settingsRepository.loadThemeColorSettingValue();
        disposables.add(
                preferenceValueFlowable.subscribe(
                        value -> this.themeColor.postValue(value.getThemeColor()),
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
        themeColorPreferenceValueFlowable = preferenceValueFlowable;
        prefelencesFlowableList.add(preferenceValueFlowable);
    }

    @NonNull
    public ThemeColor loadThemeColorSettingValue() {
        ThemeColor themeColorValue = themeColor.getValue();
        if (themeColorValue != null) return themeColorValue;
        return themeColorPreferenceValueFlowable.blockingFirst().getThemeColor();
    }

    private void setUpLoadingCalendarStartDayOfWeekPreferenceValue() {
        Flowable<CalendarStartDayOfWeekPreferenceValue> preferenceValueFlowable =
                settingsRepository.loadCalendarStartDayOfWeekPreferenceValue();
        disposables.add(
                preferenceValueFlowable.subscribe(
                        value -> {
                            DayOfWeek calendarStartDayOfWeek = value.toDayOfWeek();
                            this.calendarStartDayOfWeek.postValue(calendarStartDayOfWeek);
                            },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
        prefelencesFlowableList.add(preferenceValueFlowable);
    }

    private void setUpLoadingReminderNotificationPreferenceValue() {
        Flowable<ReminderNotificationPreferenceValue> preferenceValueFlowable =
                settingsRepository.loadReminderNotificationPreferenceValue();
        disposables.add(
                preferenceValueFlowable.subscribe(
                        value -> {
                            Log.d("20240909", "getIsChecked():" + value.getIsChecked());
                            Log.d("20240909", "getNotificationLocalTime():" + value.getNotificationTimeString());
                            isCheckedReminderNotification.postValue(value.getIsChecked());
                            reminderNotificationTime.postValue(value.getNotificationLocalTime());
                            },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
        prefelencesFlowableList.add(preferenceValueFlowable);
    }

    private void setUpLoadingPasscodeLockPreferenceValue() {
        Flowable<PassCodeLockPreferenceValue> preferenceValueFlowable =
                settingsRepository.loadPasscodeLockPreferenceValue();
        disposables.add(
                preferenceValueFlowable.subscribe(
                        value -> isCheckedPasscodeLock.postValue(value.getIsChecked()),
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
        prefelencesFlowableList.add(preferenceValueFlowable);
    }

    private void setUpLoadingGettingWeatherInformationPreferenceValue() {
        Flowable<GettingWeatherInformationPreferenceValue> preferenceValueFlowable =
                settingsRepository.loadGettingWeatherInformationPreferenceValue();
        disposables.add(
                preferenceValueFlowable.subscribe(
                        value -> isCheckedGettingWeatherInformation.postValue(value.getIsChecked()),
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
        prefelencesFlowableList.add(preferenceValueFlowable);
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

    public void saveThemeColor(ThemeColor value) {
        ThemeColorPreferenceValue preferenceValue = new ThemeColorPreferenceValue(value);
        Single<Preferences> result = settingsRepository.saveThemeColorPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    public void saveCalendarStartDayOfWeek(DayOfWeek value) {
        CalendarStartDayOfWeekPreferenceValue preferenceValue =
                new CalendarStartDayOfWeekPreferenceValue(value);
        Single<Preferences> result =
                settingsRepository.saveCalendarStartDayOfWeekPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    public void saveReminderNotificationValid(LocalTime time) {
        Log.d("20240909", "saveReminderNotificationValid()");
        ReminderNotificationPreferenceValue preferenceValue = new ReminderNotificationPreferenceValue(true, time);
        Single<Preferences> result = settingsRepository.saveReminderNotificationPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, new OnSettingsSavedCallback() {
            @Override
            public void onSettingsSaved() {
                registerReminderNotificationWorker(time);
            }
        });
    }

    public void saveReminderNotificationInvalid() {
        ReminderNotificationPreferenceValue preferenceValue =
                new ReminderNotificationPreferenceValue(false,(LocalTime) null);
        Single<Preferences> result = settingsRepository.saveReminderNotificationPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, new OnSettingsSavedCallback() {
            @Override
            public void onSettingsSaved() {
                cancelReminderNotificationWorker();
            }
        });
    }

    public void savePasscodeLock(boolean bool) {
        PassCodeLockPreferenceValue preferenceValue = new PassCodeLockPreferenceValue(bool, 0);
        Single<Preferences> result = settingsRepository.savePasscodeLockPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    public void saveGettingWeatherInformation(boolean value) {
        GettingWeatherInformationPreferenceValue preferenceValue =
                new GettingWeatherInformationPreferenceValue(value);
        Single<Preferences> result =
                settingsRepository.saveGettingWeatherInformationPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    @FunctionalInterface
    private interface OnSettingsSavedCallback {
        void onSettingsSaved();
    }

    private void setUpProcessOnSaved(Single<Preferences> result,@Nullable OnSettingsSavedCallback callback) {
        disposables.add(result.subscribe(new Consumer<Preferences>() {
            @Override
            public void accept(Preferences preferences) throws Throwable {
                Log.d("20240909", "saveReminderNotificationValid()_success");
                if (callback == null) {
                    return;
                }
                callback.onSettingsSaved();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                Log.d("20240909", "saveReminderNotificationValid()_error");
                AppError lastAppError = getAppErrorBufferListLastValue();
                if (lastAppError == AppError.SETTING_UPDATE) {
                    return;
                }
                addAppError(AppError.SETTING_UPDATE);
            }
        }));
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
    public LiveData<ThemeColor> getThemeColorSettingValueLiveData() {
        return themeColor;
    }

    public LiveData<DayOfWeek> getCalendarStartDayOfWeekLiveData() {
        return calendarStartDayOfWeek;
    }

    public LiveData<Boolean> getIsCheckedReminderNotificationLiveData() {
        return isCheckedReminderNotification;
    }

    public LiveData<LocalTime> getReminderNotificationTime() {
        return reminderNotificationTime;
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

    public double getLongitude() {
        return longitude;
    }
}
