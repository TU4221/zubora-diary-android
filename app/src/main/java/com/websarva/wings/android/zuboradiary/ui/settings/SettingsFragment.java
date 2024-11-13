package com.websarva.wings.android.zuboradiary.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment {

    // View関係
    private FragmentSettingsBinding binding;
    private boolean isTouchedReminderNotificationSwitch = false;
    private boolean isTouchedPasscodeLockSwitch = false;
    private boolean isTouchedWeatherInfoAcquisitionSwitch = false;

    // ViewModel関係
    private SettingsViewModel settingsViewModel;

    // ActivityResultLauncher関係
    private ActivityResultLauncher<String> requestPostNotificationsPermissionLauncher;
    private ActivityResultLauncher<String[]> requestAccessLocationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addOnBackPressedCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireMainActivity().popBackStackToStartFragment();
            }
        });

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermissionLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.RequestPermission(),
                            new ActivityResultCallback<Boolean>() {
                                @Override
                                public void onActivityResult(Boolean isGranted) {
                                    if (isGranted && isGrantedPostNotifications()/*再確認*/) {
                                        showReminderNotificationTimePickerDialog();
                                    } else {
                                        binding.includeReminderNotificationSetting
                                                .materialSwitchSettingValue.setChecked(false);
                                    }
                                }
                            }
                    );
        }


        // 位置情報利用権限取得結果処理
        requestAccessLocationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        new ActivityResultCallback<Map<String, Boolean>>() {
                            @Override
                            public void onActivityResult(Map<String, Boolean> o) {
                                Boolean isGrantedAccessFineLocation =
                                        o.get(Manifest.permission.ACCESS_FINE_LOCATION);
                                Boolean isGrantedAccessCoarseLocation =
                                        o.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                                boolean isGrantedAll =
                                        isGrantedAccessFineLocation != null
                                                && isGrantedAccessFineLocation
                                                && isGrantedAccessCoarseLocation != null
                                                && isGrantedAccessCoarseLocation;
                                if (isGrantedAll && isGrantedAccessLocation()/*再確認*/) {
                                    settingsViewModel.saveWeatherInfoAcquisition(true);
                                } else {
                                    binding.includeWeatherInfoAcquisitionSetting
                                            .materialSwitchSettingValue.setChecked(false);
                                }
                            }
                        }
                );
    }

    @Override
    protected void initializeViewModel() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        settingsViewModel = provider.get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected ViewDataBinding initializeDataBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        ThemeColor themeColor = settingsViewModel.loadThemeColorSettingValue();
        LayoutInflater themeColorInflater = createThemeColorInflater(inflater, themeColor);
        binding = FragmentSettingsBinding.inflate(themeColorInflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setSettingsViewModel(settingsViewModel);
        return binding;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpThemeColorSettingItem();
        setUpCalendarStartDaySettingItem();
        setUpReminderNotificationSettingItem();
        setUpPasscodeLockSettingItem();
        setUpWeatherInfoAcquisitionSettingItem();
        setUpAllDiariesDeleteSettingItem();
        setUpAllSettingsInitializationSettingItem();
        setUpAllDataDeleteSettingItem();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveThemeColorPickerDialogResult();
        receiveCalendarStartDayPickerDialogResult();
        receiveReminderNotificationTimePickerDialogResult();
        receivePermissionDialogResult();
        receiveAllDiariesDeleteConfirmationDialogResult();
        receiveAllSettingsInitializationConfirmationDialogResult();
        receiveAllDataDeleteConfirmationDialogResult();
    }

    @Override
    protected void removeDialogResultOnDestroy(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        savedStateHandle.remove(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        savedStateHandle.remove(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME);
        savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllDiariesDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllSettingsInitializationConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        savedStateHandle.remove(AllDataDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        settingsViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(settingsViewModel));
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private void receiveThemeColorPickerDialogResult() {
        ThemeColor selectedThemeColor =
                receiveResulFromDialog(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        if (selectedThemeColor == null) return;

        settingsViewModel.saveThemeColor(selectedThemeColor);
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private void receiveCalendarStartDayPickerDialogResult() {
        DayOfWeek selectedDayOfWeek =
                receiveResulFromDialog(CalendarStartDayPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        if (selectedDayOfWeek == null) return;

        settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek);
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private void receiveReminderNotificationTimePickerDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != DialogInterface.BUTTON_POSITIVE) {
            binding.includeReminderNotificationSetting.materialSwitchSettingValue.setChecked(false);
            return;
        }

        LocalTime selectedTime =
                receiveResulFromDialog(ReminderNotificationTimePickerDialogFragment.KEY_SELECTED_TIME);
        Objects.requireNonNull(selectedTime);
        settingsViewModel.saveReminderNotificationValid(selectedTime);
    }

    // 権限催促ダイアログフラグメントから結果受取
    private void receivePermissionDialogResult() {
        Integer selectedButton = receiveResulFromDialog(PermissionDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        showApplicationDetailsSettings();
    }

    private void receiveAllDiariesDeleteConfirmationDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllDiariesDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllDiaries();
    }

    private void receiveAllSettingsInitializationConfirmationDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllSettingsInitializationConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllSettings();
    }

    private void receiveAllDataDeleteConfirmationDialogResult() {
        Integer selectedButton =
                receiveResulFromDialog(AllDataDeleteConfirmationDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) return;
        if (selectedButton != Dialog.BUTTON_POSITIVE) return;

        settingsViewModel.deleteAllData();
    }

    private void setUpThemeColorSettingItem() {
        binding.includeThemeColorSetting.textSettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemeColor currentThemeColor =
                        settingsViewModel.getThemeColorSettingValueLiveData().getValue();
                showThemeColorPickerDialog(currentThemeColor);
            }
        });

        settingsViewModel.getThemeColorSettingValueLiveData()
                .observe(getViewLifecycleOwner(), new Observer<ThemeColor>() {
                    @Override
                    public void onChanged(ThemeColor themeColor) {
                        Objects.requireNonNull(themeColor);

                        String strThemeColor = themeColor.toSting(requireContext());
                        binding.includeThemeColorSetting.textSettingValue.setText(strThemeColor);
                        switchViewColor(themeColor);
                    }
                });
    }

    private void switchViewColor(ThemeColor themeColor) {
        Objects.requireNonNull(themeColor);

        SettingsThemeColorSwitcher switcher =
                new SettingsThemeColorSwitcher(requireContext(), themeColor);

        switcher.switchBackgroundColor(binding.viewFullScreenBackground);
        switcher.switchToolbarColor(binding.materialToolbarTopAppBar);

        ColorSwitchingViewList<TextView> sectionList =
                new ColorSwitchingViewList<>(
                        binding.textSettingsSectionDesign,
                        binding.textSettingsSectionSetting,
                        binding.textSettingsSectionEnd,
                        binding.textSettingsSectionData
                );
        switcher.switchSettingItemSectionColor(sectionList);

        ColorSwitchingViewList<TextView> iconList =
                new ColorSwitchingViewList<>(
                        binding.includeThemeColorSetting.textSettingTitle,
                        binding.includeCalendarStartDaySetting.textSettingTitle,
                        binding.includeReminderNotificationSetting.textSettingTitle,
                        binding.includePasscodeLockSetting.textSettingTitle,
                        binding.includeWeatherInfoAcquisitionSetting.textSettingTitle,
                        binding.includeAllDiariesDeleteSetting.textSettingTitle,
                        binding.includeAllSettingsInitializationSetting.textSettingTitle,
                        binding.includeAllDataDeleteSetting.textSettingTitle
                );
        switcher.switchSettingItemIconColor(iconList);

        ColorSwitchingViewList<TextView> textList =
                new ColorSwitchingViewList<>(
                        binding.includeThemeColorSetting.textSettingTitle,
                        binding.includeThemeColorSetting.textSettingValue,
                        binding.includeCalendarStartDaySetting.textSettingTitle,
                        binding.includeCalendarStartDaySetting.textSettingValue,
                        binding.includeReminderNotificationSetting.textSettingTitle,
                        binding.includeReminderNotificationSetting.textSettingValue,
                        binding.includePasscodeLockSetting.textSettingTitle,
                        binding.includeWeatherInfoAcquisitionSetting.textSettingTitle
                );
        switcher.switchTextColorOnBackground(textList);

        ColorSwitchingViewList<TextView> redTextList =
                new ColorSwitchingViewList<>(
                        binding.includeAllDiariesDeleteSetting.textSettingTitle,
                        binding.includeAllSettingsInitializationSetting.textSettingTitle,
                        binding.includeAllDataDeleteSetting.textSettingTitle
                );
        switcher.switchRedTextColorOnBackground(redTextList);

        ColorSwitchingViewList<MaterialSwitch> switchList =
                new ColorSwitchingViewList<>(
                        binding.includeReminderNotificationSetting.materialSwitchSettingValue,
                        binding.includePasscodeLockSetting.materialSwitchSettingValue,
                        binding.includeWeatherInfoAcquisitionSetting.materialSwitchSettingValue
                );
        switcher.switchSwitchColor(switchList);

        ColorSwitchingViewList<MaterialDivider> dividerList =
                new ColorSwitchingViewList<>(
                        binding.materialDividerToolbar,
                        binding.materialDividerThemeColorSetting,
                        binding.materialDividerSectionSetting,
                        binding.materialDividerCalendarStartDaySetting,
                        binding.materialDividerReminderNotificationSetting,
                        binding.materialDividerPasscodeLockSetting,
                        binding.materialDividerWeatherInfoAcquisitionSetting,
                        binding.materialDividerSectionData,
                        binding.materialDividerAllDiariesDeleteSetting,
                        binding.materialDividerAllSettingsInitializationSetting,
                        binding.materialDividerAllDataDeleteSetting,
                        binding.materialDividerSectionEnd
                );
        switcher.switchDividerColor(dividerList);
    }

    private void setUpCalendarStartDaySettingItem() {
        binding.includeCalendarStartDaySetting.textSettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                DayOfWeek currentCalendarStartDayOfWeek =
                        settingsViewModel.loadCalendarStartDaySettingValue();
                Objects.requireNonNull(currentCalendarStartDayOfWeek);

                showCalendarStartDayPickerDialog(currentCalendarStartDayOfWeek);
            }
        });

        settingsViewModel.getCalendarStartDayOfWeekLiveData()
                .observe(getViewLifecycleOwner(), new Observer<DayOfWeek>() {
                    @Override
                    public void onChanged(DayOfWeek dayOfWeek) {
                        DayOfWeek settingValue = dayOfWeek;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.loadCalendarStartDaySettingValue();
                        }

                        DayOfWeekStringConverter stringConverter =
                                new DayOfWeekStringConverter(requireContext());
                        String strDayOfWeek =
                                stringConverter.toCalendarStartDayOfWeek(settingValue);
                        binding.includeCalendarStartDaySetting.textSettingValue.setText(strDayOfWeek);
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpReminderNotificationSettingItem() {
        binding.includeReminderNotificationSetting.materialSwitchSettingValue
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedReminderNotificationSwitch = true;
                        }
                        return false;
                    }
                });
        binding.includeReminderNotificationSetting.materialSwitchSettingValue
                .setOnCheckedChangeListener(
                        new ReminderNotificationOnCheckedChangeListener()
                );

        settingsViewModel.getIsCheckedReminderNotificationLiveData()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        Boolean settingValue = aBoolean;
                        if (settingValue == null) {
                            settingValue = settingsViewModel.isCheckedReminderNotificationSetting();
                        }

                        if (settingValue) {
                            binding.includeReminderNotificationSetting
                                    .textSettingValue.setVisibility(View.VISIBLE);
                        } else {
                            binding.includeReminderNotificationSetting
                                    .textSettingValue.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        settingsViewModel.getReminderNotificationTimeLiveData()
                .observe(getViewLifecycleOwner(), new Observer<LocalTime>() {
                    @Override
                    public void onChanged(@Nullable LocalTime time) {
                        // MEMO:未設定の場合nullが代入される。
                        //      その為、nullはエラーではないので下記メソッドの処理は不要(処理するとループする)
                        //      "SettingsViewModel#isCheckedReminderNotificationSetting()"
                        if (time == null) {
                            binding.includeReminderNotificationSetting.textSettingValue.setText("");
                            return;
                        }

                        DateTimeStringConverter converter = new DateTimeStringConverter();
                        String strTime = converter.toHourMinute(time);
                        binding.includeReminderNotificationSetting.textSettingValue.setText(strTime);
                    }
                });
    }

    private class ReminderNotificationOnCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Objects.requireNonNull(buttonView);
            if (!isTouchedReminderNotificationSwitch) return;

            if (isChecked) {
                // MEMO:PostNotificationsはApiLevel33で導入されたPermission。33未満は許可取り不要。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPostNotificationsPermission();
                } else {
                    showReminderNotificationTimePickerDialog();
                }
            } else {
                settingsViewModel.saveReminderNotificationInvalid();
            }
            isTouchedReminderNotificationSwitch = false;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private void requestPostNotificationsPermission() {
            if (isGrantedPostNotifications()) {
                showReminderNotificationTimePickerDialog();
            } else {
                boolean shouldShowRequestPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
                if (shouldShowRequestPermissionRationale) {
                    requestPostNotificationsPermissionLauncher
                            .launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    binding.includeReminderNotificationSetting
                            .materialSwitchSettingValue.setChecked(false);
                    String permissionName = getString(R.string.fragment_settings_permission_name_notification);
                    showPermissionDialog(permissionName);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpPasscodeLockSettingItem() {
        binding.includePasscodeLockSetting.materialSwitchSettingValue
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedPasscodeLockSwitch = true;
                        }
                        return false;
                    }
                });
        binding.includePasscodeLockSetting.materialSwitchSettingValue
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Objects.requireNonNull(buttonView);

                        settingsViewModel.savePasscodeLock(isChecked);
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpWeatherInfoAcquisitionSettingItem() {
        binding.includeWeatherInfoAcquisitionSetting.materialSwitchSettingValue
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Objects.requireNonNull(v);
                        Objects.requireNonNull(event);

                        if (event.getAction() == MotionEvent.ACTION_DOWN){
                            isTouchedWeatherInfoAcquisitionSwitch = true;
                        }
                        return false;
                    }
                });
        binding.includeWeatherInfoAcquisitionSetting.materialSwitchSettingValue
                .setOnCheckedChangeListener(
                        new WeatherInfoAcquisitionOnCheckedChangeListener()
                );
    }

    private class WeatherInfoAcquisitionOnCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Objects.requireNonNull(buttonView);
            if (!isTouchedWeatherInfoAcquisitionSwitch) return;

            if (isChecked) {
                if (isGrantedAccessLocation()) {
                    settingsViewModel.saveWeatherInfoAcquisition(true);
                } else {
                    boolean shouldShowRequestPermissionRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                    && ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (shouldShowRequestPermissionRationale) {
                        String[] requestPermissions =
                                {Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION};
                        requestAccessLocationPermissionLauncher.launch(requestPermissions);
                    } else {
                        binding.includeWeatherInfoAcquisitionSetting
                                .materialSwitchSettingValue.setChecked(false);
                        String permissionName = getString(R.string.fragment_settings_permission_name_location);
                        showPermissionDialog(permissionName);
                    }
                }
            } else {
                settingsViewModel.saveWeatherInfoAcquisition(false);
            }
            isTouchedWeatherInfoAcquisitionSwitch = false;
        }
    }

    private void setUpAllDiariesDeleteSettingItem() {
        binding.includeAllDiariesDeleteSetting.textSettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllDiariesDeleteConfirmationDialog();
            }
        });

        binding.includeAllDiariesDeleteSetting.textSettingValue.setVisibility(View.GONE);
    }

    private void setUpAllSettingsInitializationSettingItem() {
        binding.includeAllSettingsInitializationSetting.textSettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllSettingsInitializationConfirmationDialog();
            }
        });

        binding.includeAllSettingsInitializationSetting.textSettingValue.setVisibility(View.GONE);
    }

    private void setUpAllDataDeleteSettingItem() {
        binding.includeAllDataDeleteSetting.textSettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(v);

                showAllDataDeleteConfirmationDialog();
            }
        });

        binding.includeAllDataDeleteSetting.textSettingValue.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean isGrantedPostNotifications() {
        return ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGrantedAccessLocation() {
        boolean isGrantedAccessFineLocation =
                ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        boolean isGrantedAccessCoarseLocation =
                ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        return isGrantedAccessFineLocation && isGrantedAccessCoarseLocation;
    }

    private void showThemeColorPickerDialog(ThemeColor themeColor) {
        Objects.requireNonNull(themeColor);
        if (!canShowOtherFragment()) return;

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToThemeColorPickerDialog(themeColor);
        navController.navigate(action);
    }

    private void showCalendarStartDayPickerDialog(DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek);
        if (!canShowOtherFragment()) return;

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToCalendarStartDayPickerDialog(dayOfWeek);
        navController.navigate(action);
    }

    private void showReminderNotificationTimePickerDialog() {
        if (!canShowOtherFragment()) return;

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToReminderNotificationTimePickerDialog();
        navController.navigate(action);
    }

    private void showPermissionDialog(String permissionName) {
        Objects.requireNonNull(permissionName);
        if (!canShowOtherFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToPermissionDialog(permissionName);
        navController.navigate(action);
    }

    private void showAllDiariesDeleteConfirmationDialog() {
        if (!canShowOtherFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllDiariesDeleteConfirmationDialog();
        navController.navigate(action);
    }

    private void showAllSettingsInitializationConfirmationDialog() {
        if (!canShowOtherFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllSettingsInitializationConfirmationDialog();
        navController.navigate(action);
    }

    private void showAllDataDeleteConfirmationDialog() {
        if (!canShowOtherFragment()) return;

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToAllDataDeleteConfirmationDialog();
        navController.navigate(action);
    }

    @Override
    protected void showMessageDialog(@NonNull String title, @NonNull String message) {
        NavDirections action =
                SettingsFragmentDirections.actionSettingsFragmentToMessageDialog(title, message);
        navController.navigate(action);
    }

    @Override
    protected void retryErrorDialogShow() {
        settingsViewModel.triggerAppErrorBufferListObserver();
    }

    private void showApplicationDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void destroyBinding() {
        binding = null;
    }
}
