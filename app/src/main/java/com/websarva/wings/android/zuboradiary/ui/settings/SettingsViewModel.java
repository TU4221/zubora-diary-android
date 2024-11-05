package com.websarva.wings.android.zuboradiary.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

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

    private final MutableLiveData<ThemeColor> themeColor = new MutableLiveData<>();
    private final MutableLiveData<DayOfWeek> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<LocalTime> reminderNotificationTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedWeatherInfoAcquisition = new MutableLiveData<>();

    private final MutableLiveData<GeoCoordinates> geoCoordinates = new MutableLiveData<>();
    private Flowable<ThemeColorPreferenceValue> themeColorPreferenceValueFlowable;
    private Flowable<CalendarStartDayOfWeekPreferenceValue> calendarStartDayPreferenceValueFlowable;
    private Flowable<ReminderNotificationPreferenceValue> reminderNotificationPreferenceValueFlowable;
    private Flowable<PassCodeLockPreferenceValue> passCodeLockPreferenceValueFlowable;
    private Flowable<WeatherInfoAcquisitionPreferenceValue> weatherInfoAcquisitionPreferenceValueFlowable;

    @Inject
    public SettingsViewModel(SettingsRepository settingsRepository, WorkerRepository workerRepository) {
        this.settingsRepository = settingsRepository;
        this.workerRepository = workerRepository;

        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();

        setUpThemeColorPreferenceValueLoading();
        setUpCalendarStartDayOfWeekPreferenceValueLoading();
        setUpReminderNotificationPreferenceValueLoading();
        setUpPasscodeLockPreferenceValueLoading();
        setUpWeatherInfoAcquisitionPreferenceValueLoading();
    }

    private void setUpThemeColorPreferenceValueLoading() {
        themeColorPreferenceValueFlowable = settingsRepository.loadThemeColorSettingValue();
        disposables.add(
                themeColorPreferenceValueFlowable.subscribe(
                        value -> {
                            themeColor.postValue(value.getThemeColor());
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
    }

    @NonNull
    public ThemeColor loadThemeColorSettingValue() {
        ThemeColor themeColorValue = themeColor.getValue();
        if (themeColorValue != null) return themeColorValue;
        setUpThemeColorPreferenceValueLoading();
        ThemeColorPreferenceValue defaultValue = new ThemeColorPreferenceValue(ThemeColor.WHITE);
        return themeColorPreferenceValueFlowable.blockingFirst(defaultValue).getThemeColor();
    }

    private void setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        calendarStartDayPreferenceValueFlowable =
                settingsRepository.loadCalendarStartDayOfWeekPreferenceValue();
        disposables.add(
                calendarStartDayPreferenceValueFlowable.subscribe(
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
    }

    @NonNull
    public DayOfWeek loadCalendarStartDaySettingValue() {
        DayOfWeek dayOfWeekValue = calendarStartDayOfWeek.getValue();
        if (dayOfWeekValue != null) return dayOfWeekValue;
        setUpCalendarStartDayOfWeekPreferenceValueLoading();
        CalendarStartDayOfWeekPreferenceValue defaultValue =
                new CalendarStartDayOfWeekPreferenceValue(DayOfWeek.SUNDAY);
        return calendarStartDayPreferenceValueFlowable.blockingFirst(defaultValue).toDayOfWeek();
    }

    private void setUpReminderNotificationPreferenceValueLoading() {
        reminderNotificationPreferenceValueFlowable =
                settingsRepository.loadReminderNotificationPreferenceValue();
        disposables.add(
                reminderNotificationPreferenceValueFlowable.subscribe(
                        value -> {
                            isCheckedReminderNotification.postValue(value.getIsChecked());
                            reminderNotificationTime.postValue(value.getNotificationLocalTime());
                            },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
    }

    public boolean isCheckedReminderNotificationSetting() {
        Boolean value = isCheckedReminderNotification.getValue();
        if (value != null) return value;
        setUpReminderNotificationPreferenceValueLoading();
        ReminderNotificationPreferenceValue defaultValue =
                new ReminderNotificationPreferenceValue(false, "");
        return reminderNotificationPreferenceValueFlowable.blockingFirst(defaultValue).getIsChecked();
    }

    @Nullable
    public LocalTime loadReminderNotificationTimeSettingValue() {
        LocalTime value = reminderNotificationTime.getValue();
        if (value != null) return value;
        setUpReminderNotificationPreferenceValueLoading();
        ReminderNotificationPreferenceValue defaultValue =
                new ReminderNotificationPreferenceValue(false, "");
        return reminderNotificationPreferenceValueFlowable
                .blockingFirst(defaultValue).getNotificationLocalTime();
    }

    private void setUpPasscodeLockPreferenceValueLoading() {
        passCodeLockPreferenceValueFlowable = settingsRepository.loadPasscodeLockPreferenceValue();
        disposables.add(
                passCodeLockPreferenceValueFlowable.subscribe(
                        value -> {
                            isCheckedPasscodeLock.postValue(value.getIsChecked());
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
    }

    private void setUpWeatherInfoAcquisitionPreferenceValueLoading() {
        weatherInfoAcquisitionPreferenceValueFlowable =
                settingsRepository.loadGettingWeatherInformationPreferenceValue();
        disposables.add(
                weatherInfoAcquisitionPreferenceValueFlowable.subscribe(
                        value -> {
                            isCheckedWeatherInfoAcquisition.postValue(value.getIsChecked());
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            addSettingLoadingError();
                        }
                )
        );
    }

    public boolean isCheckedWeatherInfoAcquisitionSetting() {
        Boolean value = isCheckedWeatherInfoAcquisition.getValue();
        if (value != null) return value;
        setUpWeatherInfoAcquisitionPreferenceValueLoading();
        WeatherInfoAcquisitionPreferenceValue defaultValue =
                new WeatherInfoAcquisitionPreferenceValue(false);
        return weatherInfoAcquisitionPreferenceValueFlowable.blockingFirst(defaultValue).getIsChecked();
    }

    private void addSettingLoadingError() {
        if (equalLastAppError(AppError.SETTING_LOADING)) return;  // 設定更新エラー通知の重複防止
        addAppError(AppError.SETTING_LOADING);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    void saveThemeColor(ThemeColor value) {
        Objects.requireNonNull(value);

        ThemeColorPreferenceValue preferenceValue = new ThemeColorPreferenceValue(value);
        Single<Preferences> result = settingsRepository.saveThemeColorPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    void saveCalendarStartDayOfWeek(DayOfWeek value) {
        Objects.requireNonNull(value);

        CalendarStartDayOfWeekPreferenceValue preferenceValue =
                new CalendarStartDayOfWeekPreferenceValue(value);
        Single<Preferences> result =
                settingsRepository.saveCalendarStartDayOfWeekPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    void saveReminderNotificationValid(LocalTime value) {
        Objects.requireNonNull(value);

        ReminderNotificationPreferenceValue preferenceValue = new ReminderNotificationPreferenceValue(true, value);
        Single<Preferences> result = settingsRepository.saveReminderNotificationPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, new OnSettingsSavedCallback() {
            @Override
            public void onSettingsSaved() {
                registerReminderNotificationWorker(value);
            }
        });
    }

    void saveReminderNotificationInvalid() {
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

    void savePasscodeLock(boolean value) {
        PassCodeLockPreferenceValue preferenceValue = new PassCodeLockPreferenceValue(value, 0);
        Single<Preferences> result = settingsRepository.savePasscodeLockPreferenceValue(preferenceValue);
        setUpProcessOnSaved(result, null);
    }

    void saveWeatherInfoAcquisition(boolean value) {
        WeatherInfoAcquisitionPreferenceValue preferenceValue =
                new WeatherInfoAcquisitionPreferenceValue(value);
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
            public void accept(Preferences preferences) {
                Objects.requireNonNull(preferences);

                if (callback == null) return;
                callback.onSettingsSaved();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Objects.requireNonNull(throwable);

                if (equalLastAppError(AppError.SETTING_UPDATE)) return; // 設定更新エラー通知の重複防止
                addAppError(AppError.SETTING_UPDATE);
            }
        }));
    }

    void registerReminderNotificationWorker(LocalTime settingTime) {
        Objects.requireNonNull(settingTime);

        workerRepository.registerReminderNotificationWorker(settingTime);
    }

    void cancelReminderNotificationWorker() {
        workerRepository.cancelReminderNotificationWorker();
    }

    public void updateGeoCoordinates(GeoCoordinates geoCoordinates) {
        Objects.requireNonNull(geoCoordinates);

        this.geoCoordinates.setValue(geoCoordinates);
    }

    public void clearGeoCoordinates() {
        geoCoordinates.setValue(null);
    }

    public boolean hasUpdatedGeoCoordinates() {
        GeoCoordinates geoCoordinates = this.geoCoordinates.getValue();
        return geoCoordinates != null;
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

    public LiveData<LocalTime> getReminderNotificationTimeLiveData() {
        return reminderNotificationTime;
    }

    public LiveData<Boolean> getIsCheckedPasscodeLockLiveData() {
        return isCheckedPasscodeLock;
    }

    public LiveData<Boolean> getIsCheckedWeatherInfoAcquisitionLiveData() {
        return isCheckedWeatherInfoAcquisition;
    }

    public LiveData<GeoCoordinates> getGeoCoordinatesLiveData() {
        return geoCoordinates;
    }
}
