package com.websarva.wings.android.zuboradiary.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreferenceValue;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;

@HiltViewModel
public class SettingsViewModel extends BaseViewModel {
    private final UserPreferencesRepository userPreferencesRepository;
    private final WorkerRepository workerRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository;
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
    public SettingsViewModel(
            UserPreferencesRepository userPreferencesRepository,
            WorkerRepository workerRepository,
            DiaryRepository diaryRepository,
            DiaryItemTitleSelectionHistoryRepository diaryItemTitleSelectionHistoryRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.workerRepository = workerRepository;
        this.diaryRepository = diaryRepository;
        this.diaryItemTitleSelectionHistoryRepository = diaryItemTitleSelectionHistoryRepository;

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
        themeColorPreferenceValueFlowable = userPreferencesRepository.loadThemeColorSettingValue();
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
        ThemeColorPreferenceValue defaultValue = new ThemeColorPreferenceValue();
        return themeColorPreferenceValueFlowable.blockingFirst(defaultValue).getThemeColor();
    }

    private void setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        calendarStartDayPreferenceValueFlowable =
                userPreferencesRepository.loadCalendarStartDayOfWeekPreferenceValue();
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
        CalendarStartDayOfWeekPreferenceValue defaultValue = new CalendarStartDayOfWeekPreferenceValue();
        return calendarStartDayPreferenceValueFlowable.blockingFirst(defaultValue).toDayOfWeek();
    }

    private void setUpReminderNotificationPreferenceValueLoading() {
        reminderNotificationPreferenceValueFlowable =
                userPreferencesRepository.loadReminderNotificationPreferenceValue();
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
                new ReminderNotificationPreferenceValue();
        return reminderNotificationPreferenceValueFlowable.blockingFirst(defaultValue).getIsChecked();
    }

    @Nullable
    public LocalTime loadReminderNotificationTimeSettingValue() {
        LocalTime value = reminderNotificationTime.getValue();
        if (value != null) return value;
        setUpReminderNotificationPreferenceValueLoading();
        ReminderNotificationPreferenceValue defaultValue =
                new ReminderNotificationPreferenceValue();
        return reminderNotificationPreferenceValueFlowable
                .blockingFirst(defaultValue).getNotificationLocalTime();
    }

    private void setUpPasscodeLockPreferenceValueLoading() {
        passCodeLockPreferenceValueFlowable = userPreferencesRepository.loadPasscodeLockPreferenceValue();
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
                userPreferencesRepository.loadWeatherInfoAcquisitionPreferenceValue();
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
                new WeatherInfoAcquisitionPreferenceValue();
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
        Single<Preferences> result = userPreferencesRepository.saveThemeColorPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    void saveCalendarStartDayOfWeek(DayOfWeek value) {
        Objects.requireNonNull(value);

        CalendarStartDayOfWeekPreferenceValue preferenceValue =
                new CalendarStartDayOfWeekPreferenceValue(value);
        Single<Preferences> result =
                userPreferencesRepository.saveCalendarStartDayOfWeekPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    void saveReminderNotificationValid(LocalTime value) {
        Objects.requireNonNull(value);

        ReminderNotificationPreferenceValue preferenceValue =
                new ReminderNotificationPreferenceValue(true, value);
        Single<Preferences> result = userPreferencesRepository.saveReminderNotificationPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, new OnSettingsUpdateCallback() {
            @Override
            public void onUpdateSettings() {
                workerRepository.registerReminderNotificationWorker(value);
            }
        });
    }

    void saveReminderNotificationInvalid() {
        ReminderNotificationPreferenceValue preferenceValue =
                new ReminderNotificationPreferenceValue(false,(LocalTime) null);
        Single<Preferences> result = userPreferencesRepository.saveReminderNotificationPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, new OnSettingsUpdateCallback() {
            @Override
            public void onUpdateSettings() {
                workerRepository.cancelReminderNotificationWorker();
            }
        });
    }

    void savePasscodeLock(boolean value) {
        String passcode;
        if (value) {
            passcode = "0000"; // TODO:仮
        } else {
            passcode = "";
        }

        PassCodeLockPreferenceValue preferenceValue = new PassCodeLockPreferenceValue(value, passcode);
        Single<Preferences> result = userPreferencesRepository.savePasscodeLockPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    void saveWeatherInfoAcquisition(boolean value) {
        WeatherInfoAcquisitionPreferenceValue preferenceValue =
                new WeatherInfoAcquisitionPreferenceValue(value);
        Single<Preferences> result =
                userPreferencesRepository.saveWeatherInfoAcquisitionPreferenceValue(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    @FunctionalInterface
    private interface OnSettingsUpdateCallback {
        void onUpdateSettings();
    }

    private void setUpProcessOnUpdate(Single<Preferences> result, @Nullable OnSettingsUpdateCallback callback) {
        Objects.requireNonNull(result);

        disposables.add(result.subscribe(new Consumer<Preferences>() {
            @Override
            public void accept(Preferences preferences) {
                Objects.requireNonNull(preferences);

                if (callback == null) return;
                callback.onUpdateSettings();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Objects.requireNonNull(throwable);

                AppError appError = AppError.SETTING_UPDATE;
                if (equalLastAppError(appError)) return; // 設定更新エラー通知の重複防止
                addAppError(appError);
            }
        }));
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

    void deleteAllDiaries() {
        try {
            diaryRepository.deleteAllDiaries().get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_DELETE);
        }
    }

    void deleteAllSettings() {
        Single<Preferences> result = userPreferencesRepository.initializePreferences();
        setUpProcessOnUpdate(result, null);
    }

    void deleteAllData() {
        try {
            diaryRepository.deleteAllData().get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppError(AppError.DIARY_DELETE);
        }
        deleteAllSettings();
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
