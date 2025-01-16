package com.websarva.wings.android.zuboradiary.ui.settings;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.websarva.wings.android.zuboradiary.data.AppMessage;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.network.GeoCoordinates;
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference;
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference;
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference;
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference;
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
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<ThemeColor> themeColor = new MutableLiveData<>();
    private final MutableLiveData<DayOfWeek> calendarStartDayOfWeek = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedReminderNotification = new MutableLiveData<>();
    private final MutableLiveData<LocalTime> reminderNotificationTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedPasscodeLock = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCheckedWeatherInfoAcquisition = new MutableLiveData<>();

    private final MutableLiveData<GeoCoordinates> geoCoordinates = new MutableLiveData<>();
    private Flowable<ThemeColorPreference> themeColorPreferenceFlowable;
    private Flowable<CalendarStartDayOfWeekPreference> calendarStartDayPreferenceFlowable;
    private Flowable<ReminderNotificationPreference> reminderNotificationPreferenceFlowable;
    private Flowable<PassCodeLockPreference> passCodeLockPreferenceFlowable;
    private Flowable<WeatherInfoAcquisitionPreference> weatherInfoAcquisitionPreferenceFlowable;

    @Inject
    public SettingsViewModel(
            UserPreferencesRepository userPreferencesRepository,
            WorkerRepository workerRepository,
            DiaryRepository diaryRepository) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.workerRepository = workerRepository;
        this.diaryRepository = diaryRepository;

        initialize();
    }

    @Override
    protected void initialize() {
        initializeAppMessageList();
        setUpThemeColorPreferenceValueLoading();
        setUpCalendarStartDayOfWeekPreferenceValueLoading();
        setUpReminderNotificationPreferenceValueLoading();
        setUpPasscodeLockPreferenceValueLoading();
        setUpWeatherInfoAcquisitionPreferenceValueLoading();
    }

    private void setUpThemeColorPreferenceValueLoading() {
        themeColorPreferenceFlowable = userPreferencesRepository.loadThemeColorPreference();
        disposables.add(
                themeColorPreferenceFlowable.subscribe(
                        value -> {
                            // HACK:一つのDataStore(UserPreferencesクラス)からFlowableを生成している為、
                            //      一つのPreferenceを更新すると他のPreferenceのFlowableにも通知される。
                            //      結果的にObserverにも通知が行き、不必要な処理が発生してしまう。
                            //      対策として下記コードを記述。(他PreferenceFlowableも同様)
                            Objects.requireNonNull(value);
                            ThemeColor themeColor = this.themeColor.getValue();
                            if (themeColor != null && themeColor.equals(value.toThemeColor())) return;

                            this.themeColor.postValue(value.toThemeColor());
                        },
                        throwable -> {
                            Log.d("Exception", "テーマカラー設定値読込失敗", throwable);
                            addSettingLoadingErrorMessage();
                        }
                )
        );
    }

    @NonNull
    public ThemeColor loadThemeColorSettingValue() {
        ThemeColor themeColorValue = themeColor.getValue();
        if (themeColorValue != null) return themeColorValue;
        ThemeColorPreference defaultValue = new ThemeColorPreference();
        return themeColorPreferenceFlowable.blockingFirst(defaultValue).toThemeColor();
    }

    private void setUpCalendarStartDayOfWeekPreferenceValueLoading() {
        calendarStartDayPreferenceFlowable =
                userPreferencesRepository.loadCalendarStartDayOfWeekPreference();
        disposables.add(
                calendarStartDayPreferenceFlowable.subscribe(
                        value -> {
                            Objects.requireNonNull(value);
                            DayOfWeek dayOfWeek = calendarStartDayOfWeek.getValue();
                            if (dayOfWeek != null && dayOfWeek.equals(value.toDayOfWeek())) return;

                            DayOfWeek calendarStartDayOfWeek = value.toDayOfWeek();
                            this.calendarStartDayOfWeek.postValue(calendarStartDayOfWeek);
                            },
                        throwable -> {
                            Log.d("Exception", "カレンダー開始曜日設定値読込失敗", throwable);
                            addSettingLoadingErrorMessage();
                        }
                )
        );
    }

    @NonNull
    public DayOfWeek loadCalendarStartDaySettingValue() {
        DayOfWeek dayOfWeekValue = calendarStartDayOfWeek.getValue();
        if (dayOfWeekValue != null) return dayOfWeekValue;
        CalendarStartDayOfWeekPreference defaultValue = new CalendarStartDayOfWeekPreference();
        return calendarStartDayPreferenceFlowable.blockingFirst(defaultValue).toDayOfWeek();
    }

    private void setUpReminderNotificationPreferenceValueLoading() {
        reminderNotificationPreferenceFlowable =
                userPreferencesRepository.loadReminderNotificationPreference();
        disposables.add(
                reminderNotificationPreferenceFlowable.subscribe(
                        value -> {
                            Objects.requireNonNull(value);
                            Boolean isChecked = isCheckedReminderNotification.getValue();
                            if (isChecked != null && isChecked == value.getIsChecked()) return;

                            isCheckedReminderNotification.postValue(value.getIsChecked());
                            reminderNotificationTime.postValue(value.getNotificationLocalTime());
                            },
                        throwable -> {
                            Log.d("Exception", "リマインダー通知設定値読込失敗", throwable);
                            addSettingLoadingErrorMessage();
                        }
                )
        );
    }

    public boolean isCheckedReminderNotificationSetting() {
        Boolean value = isCheckedReminderNotification.getValue();
        if (value != null) return value;
        ReminderNotificationPreference defaultValue =
                new ReminderNotificationPreference();
        return reminderNotificationPreferenceFlowable.blockingFirst(defaultValue).getIsChecked();
    }

    @Nullable
    public LocalTime loadReminderNotificationTimeSettingValue() {
        LocalTime value = reminderNotificationTime.getValue();
        if (value != null) return value;
        ReminderNotificationPreference defaultValue =
                new ReminderNotificationPreference();
        return reminderNotificationPreferenceFlowable
                .blockingFirst(defaultValue).getNotificationLocalTime();
    }

    private void setUpPasscodeLockPreferenceValueLoading() {
        passCodeLockPreferenceFlowable = userPreferencesRepository.loadPasscodeLockPreference();
        disposables.add(
                passCodeLockPreferenceFlowable.subscribe(
                        value -> {
                            Objects.requireNonNull(value);
                            Boolean isChecked = isCheckedPasscodeLock.getValue();
                            if (isChecked != null && isChecked == value.getIsChecked()) return;

                            isCheckedPasscodeLock.postValue(value.getIsChecked());
                        },
                        throwable -> {
                            Log.d("Exception", "パスコード設定値読込失敗", throwable);
                            addSettingLoadingErrorMessage();
                        }
                )
        );
    }

    private void setUpWeatherInfoAcquisitionPreferenceValueLoading() {
        weatherInfoAcquisitionPreferenceFlowable =
                userPreferencesRepository.loadWeatherInfoAcquisitionPreference();
        disposables.add(
                weatherInfoAcquisitionPreferenceFlowable.subscribe(
                        value -> {
                            Objects.requireNonNull(value);
                            Boolean isChecked = isCheckedWeatherInfoAcquisition.getValue();
                            if (isChecked != null && isChecked == value.getIsChecked()) return;

                            isCheckedWeatherInfoAcquisition.postValue(value.getIsChecked());
                        },
                        throwable -> {
                            Log.d("Exception", "天気情報取得設定値読込失敗", throwable);
                            addSettingLoadingErrorMessage();
                        }
                )
        );
    }

    public boolean isCheckedWeatherInfoAcquisitionSetting() {
        Boolean value = isCheckedWeatherInfoAcquisition.getValue();
        if (value != null) return value;
        WeatherInfoAcquisitionPreference defaultValue =
                new WeatherInfoAcquisitionPreference();
        return weatherInfoAcquisitionPreferenceFlowable.blockingFirst(defaultValue).getIsChecked();
    }

    private void addSettingLoadingErrorMessage() {
        if (equalLastAppMessage(AppMessage.SETTING_LOADING_ERROR)) return;  // 設定更新エラー通知の重複防止
        addAppMessage(AppMessage.SETTING_LOADING_ERROR);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    void saveThemeColor(ThemeColor value) {
        Objects.requireNonNull(value);

        ThemeColorPreference preferenceValue = new ThemeColorPreference(value);
        Single<Preferences> result = userPreferencesRepository.saveThemeColorPreference(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    void saveCalendarStartDayOfWeek(DayOfWeek value) {
        Objects.requireNonNull(value);

        CalendarStartDayOfWeekPreference preferenceValue =
                new CalendarStartDayOfWeekPreference(value);
        Single<Preferences> result =
                userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    void saveReminderNotificationValid(LocalTime value) {
        Objects.requireNonNull(value);

        ReminderNotificationPreference preferenceValue =
                new ReminderNotificationPreference(true, value);
        Single<Preferences> result = userPreferencesRepository.saveReminderNotificationPreference(preferenceValue);
        setUpProcessOnUpdate(result, new OnSettingsUpdateCallback() {
            @Override
            public void onUpdateSettings() {
                workerRepository.registerReminderNotificationWorker(value);
            }
        });
    }

    public void saveReminderNotificationInvalid() {
        ReminderNotificationPreference preferenceValue =
                new ReminderNotificationPreference(false,(LocalTime) null);
        Single<Preferences> result = userPreferencesRepository.saveReminderNotificationPreference(preferenceValue);
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

        PassCodeLockPreference preferenceValue = new PassCodeLockPreference(value, passcode);
        Single<Preferences> result = userPreferencesRepository.savePasscodeLockPreference(preferenceValue);
        setUpProcessOnUpdate(result, null);
    }

    public void saveWeatherInfoAcquisition(boolean value) {
        WeatherInfoAcquisitionPreference preferenceValue =
                new WeatherInfoAcquisitionPreference(value);
        Single<Preferences> result =
                userPreferencesRepository.saveWeatherInfoAcquisitionPreference(preferenceValue);
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

                AppMessage appMessage = AppMessage.SETTING_UPDATE_ERROR;
                if (equalLastAppMessage(appMessage)) return; // 設定更新エラー通知の重複防止
                addAppMessage(appMessage);
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
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
        }
    }

    void deleteAllSettings() {
        Single<Preferences> result = userPreferencesRepository.initializeAllPreferences();
        setUpProcessOnUpdate(result, null);
    }

    void deleteAllData() {
        try {
            diaryRepository.deleteAllData().get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR);
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
