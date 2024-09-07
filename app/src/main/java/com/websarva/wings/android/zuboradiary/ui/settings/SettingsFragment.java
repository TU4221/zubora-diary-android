package com.websarva.wings.android.zuboradiary.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.DateTimeStringConverter;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekStringConverter;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends BaseFragment {

    // View関係
    private FragmentSettingsBinding binding;
    private boolean isTouchedReminderNotificationSwitch = false;
    private boolean isTouchedPasscodeLockSwitch = false;
    private boolean isTouchedGettingWeatherInformationSwitch = false;

    // ViewModel関係
    private SettingsViewModel settingsViewModel;

    // ActivityResultLauncher関係
    private ActivityResultLauncher<String> requestPostNotificationsPermissionLauncher;
    private ActivityResultLauncher<String[]> requestAccessLocationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        requestPostNotificationsPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        new ActivityResultCallback<Boolean>() {
                            @Override
                            public void onActivityResult(Boolean isGranted) {
                                if (isGranted && isGrantedPostNotifications()/*再確認*/) {
                                    showTimePickerDialog();
                                } else {
                                    binding.switchReminderNotificationValue.setChecked(false);
                                }
                            }
                        }
                );

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
                                    settingsViewModel.saveGettingWeatherInformation(true);
                                } else {
                                    binding.switchGettingWeatherInformationValue.setChecked(false);
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
    protected View initializeDataBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setSettingsViewModel(settingsViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpThemeColorSettingItem();
        setUpCalendarStartDaySettingItem();
        setUpReminderNotificationSettingItem();
        setUpPasscodeLockSettingItem();
        setUpGettingWeatherInformationSettingItem();
    }

    @Override
    protected void handleOnReceivingResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivingDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        receiveThemeColorPickerDialogResult(savedStateHandle);
        receiveDayOfWeekPickerDialogResult(savedStateHandle);
        receiveTimePickerDialogResult(savedStateHandle);
        receivePermissionDialogResult(savedStateHandle);
    }

    @Override
    protected void removeDialogResult(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_HOUR);
        savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
        savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
    }

    @Override
    protected void setUpErrorMessageDialog() {
        settingsViewModel.getAppErrorBufferListLiveData()
                .observe(getViewLifecycleOwner(), new AppErrorBufferListObserver(settingsViewModel));
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private void receiveThemeColorPickerDialogResult(SavedStateHandle savedStateHandle) {
        ThemeColor selectedThemeColor =
                receiveResulFromDialog(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        if (selectedThemeColor == null) {
            return;
        }

        settingsViewModel.saveThemeColor(selectedThemeColor);
    }

    // カレンダー開始曜日設定ダイアログフラグメントから結果受取
    private void receiveDayOfWeekPickerDialogResult(SavedStateHandle savedStateHandle) {
        DayOfWeek selectedDayOfWeek =
                receiveResulFromDialog(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        if (selectedDayOfWeek == null) {
            return;
        }

        settingsViewModel.saveCalendarStartDayOfWeek(selectedDayOfWeek);
    }

    // リマインダー通知時間設定ダイアログフラグメントから結果受取
    private void receiveTimePickerDialogResult(SavedStateHandle savedStateHandle) {
        Integer selectedHour = receiveResulFromDialog(TimePickerDialogFragment.KEY_SELECTED_HOUR);
        if (selectedHour == null) {
            return;
        }
        Integer selectedMinute = receiveResulFromDialog(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
        if (selectedMinute == null) {
            return;
        }

        LocalTime settingTime = LocalTime.of(selectedHour, selectedMinute);
        settingsViewModel.saveReminderNotificationValid(settingTime);

        DateTimeStringConverter converter = new DateTimeStringConverter();
        String strSettingTime = converter.toStringTimeHourMinute(settingTime);
        binding.textReminderNotificationTime.setText(strSettingTime);
        binding.textReminderNotificationTime.setVisibility(View.VISIBLE);
    }

    // 権限催促ダイアログフラグメントから結果受取
    private void receivePermissionDialogResult(SavedStateHandle savedStateHandle) {
        Integer selectedButton = receiveResulFromDialog(PermissionDialogFragment.KEY_SELECTED_BUTTON);
        if (selectedButton == null) {
            return;
        }
        if (selectedButton != Dialog.BUTTON_POSITIVE) {
            return;
        }

        showApplicationDetailsSettings();
    }

    private void setUpThemeColorSettingItem() {
        binding.textThemeColorSettingTitle.setOnClickListener(new View.OnClickListener() {
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
                        if (themeColor == null) {
                            return;
                        }

                        String strThemeColor = themeColor.toSting(requireContext());
                        binding.textThemeColorSettingValue.setText(strThemeColor);

                        ThemeColorSwitcher switcher = new ThemeColorSwitcher(getResources(), requireContext());
                        ColorSwitchingViewList<TextView> sectionList =
                                new ColorSwitchingViewList<>(
                                        binding.textSettingsSectionDesign,
                                        binding.textSettingsSectionSettings,
                                        binding.textSettingsSectionEnd
                                );
                        ColorSwitchingViewList<TextView> iconList =
                                new ColorSwitchingViewList<>(
                                        binding.textThemeColorSettingTitle,
                                        binding.textCalendarStartDaySettingTitle,
                                        binding.textReminderNotificationSettingTitle,
                                        binding.textGettingWeatherInformationSettingTitle
                                );
                        ColorSwitchingViewList<MaterialSwitch> switchList =
                                new ColorSwitchingViewList<>(
                                        binding.switchReminderNotificationValue,
                                        binding.switchPasscodeLockValue,
                                        binding.switchGettingWeatherInformationValue
                                );
                        switcher.switchSectionView(themeColor, sectionList);
                        switcher.switchTextIcon(themeColor, iconList);
                        switcher.switchSwitch(themeColor, switchList);
                    }
                });
    }

    private void setUpCalendarStartDaySettingItem() {
        binding.textCalendarStartDaySettingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayOfWeek currentCalendarStartDayOfWeek =
                        settingsViewModel.getCalendarStartDayOfWeekLiveData().getValue();
                if (currentCalendarStartDayOfWeek == null) {
                    throw new NullPointerException();
                }

                showDayOfWeekPickerDialog(currentCalendarStartDayOfWeek);
            }
        });

        settingsViewModel.getCalendarStartDayOfWeekLiveData()
                .observe(getViewLifecycleOwner(), new Observer<DayOfWeek>() {
                    @Override
                    public void onChanged(DayOfWeek dayOfWeek) {
                        if (dayOfWeek == null) {
                            return;
                        }

                        DayOfWeekStringConverter stringConverter =
                                new DayOfWeekStringConverter(requireContext());
                        String strDayOfWeek =
                                stringConverter.toCalendarStartDayOfWeek(dayOfWeek);
                        binding.textCalendarStartDaySettingValue.setText(strDayOfWeek);
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpReminderNotificationSettingItem() {
        binding.switchReminderNotificationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedReminderNotificationSwitch = true;
                }
                return false;
            }
        });
        binding.switchReminderNotificationValue
                .setOnCheckedChangeListener(
                        new ReminderNotificationCheckedChangeListener()
                );
    }

    private class ReminderNotificationCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isTouchedReminderNotificationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedPostNotifications()) {
                    showTimePickerDialog();
                } else {
                    boolean shouldShowRequestPermissionRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
                    if (shouldShowRequestPermissionRationale) {
                        requestPostNotificationsPermissionLauncher
                                .launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        binding.switchReminderNotificationValue.setChecked(false);
                        String permissionName = getString(R.string.fragment_settings_permission_name_notification);
                        showPermissionDialog(permissionName);
                    }
                }
            } else {
                settingsViewModel.saveReminderNotificationInvalid();
                binding.textReminderNotificationTime.setVisibility(View.INVISIBLE);
            }
            isTouchedReminderNotificationSwitch = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpPasscodeLockSettingItem() {
        binding.switchPasscodeLockValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedPasscodeLockSwitch = true;
                }
                return false;
            }
        });
        binding.switchPasscodeLockValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsViewModel.savePasscodeLock(isChecked);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpGettingWeatherInformationSettingItem() {
        binding.switchGettingWeatherInformationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedGettingWeatherInformationSwitch = true;
                }
                return false;
            }
        });
        binding.switchGettingWeatherInformationValue
                .setOnCheckedChangeListener(
                        new GettingWeatherInformationCheckedChangeListener()
                );
    }

    private class GettingWeatherInformationCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isTouchedGettingWeatherInformationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedAccessLocation()) {
                    settingsViewModel.saveGettingWeatherInformation(true);
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
                        binding.switchGettingWeatherInformationValue.setChecked(false);
                        String permissionName = getString(R.string.fragment_settings_permission_name_location);
                        showPermissionDialog(permissionName);
                    }
                }
            } else {
                settingsViewModel.saveGettingWeatherInformation(false);
            }
            isTouchedGettingWeatherInformationSwitch = false;
        }
    }

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
        if (themeColor == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToThemeColorPickerDialog(themeColor);
        navController.navigate(action);
    }

    private void showDayOfWeekPickerDialog(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new NullPointerException();
        }
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToDayOfWeekPickerDialog(dayOfWeek);
        navController.navigate(action);
    }

    private void showTimePickerDialog() {
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action = SettingsFragmentDirections
                .actionNavigationSettingsFragmentToTimePickerDialog();
        navController.navigate(action);
    }

    private void showPermissionDialog(String permissionName) {
        if (!canShowOtherFragment()) {
            return;
        }

        NavDirections action =
                SettingsFragmentDirections
                        .actionSettingsFragmentToPermissionDialog(permissionName);
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
