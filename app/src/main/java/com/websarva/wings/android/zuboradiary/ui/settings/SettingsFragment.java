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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.data.DayOfWeekConverter;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;
import com.websarva.wings.android.zuboradiary.databinding.FragmentSettingsBinding;
import com.websarva.wings.android.zuboradiary.ui.BaseFragment;
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

        // ViewModel設定
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        settingsViewModel = provider.get(SettingsViewModel.class);

        // ActivityResultLauncher設定
        // 通知権限取得結果処理
        requestPostNotificationsPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        new ActivityResultCallback<Boolean>() {
                            @Override
                            public void onActivityResult(Boolean isGranted) {
                                if (isGranted && isGrantedPostNotifications()/*再確認*/) {
                                    NavDirections action = SettingsFragmentDirections
                                            .actionNavigationSettingsFragmentToTimePickerDialog();
                                    navController.navigate(action);
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
                                    settingsViewModel.saveIsGettingWeatherInformation(true);
                                } else {
                                    binding.switchGettingWeatherInformationValue.setChecked(false);
                                }
                            }
                        }
                );
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // ビューバインディング設定
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        // データバインディング設定
        binding.setLifecycleOwner(this);
        binding.setSettingsViewModel(settingsViewModel);

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textThemeColorSettingTitle.setOnClickListener(
                new OnClickListenerOfThemeColorSetting(navController, settingsViewModel)
        );
        binding.textCalendarStartDaySettingTitle.setOnClickListener(
                new OnClickListenerOfCalendarStartDayOfWeekSetting(navController, settingsViewModel)
        );
        binding.switchReminderNotificationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedReminderNotificationSwitch = true;
                }
                return false;
            }
        });
        binding.switchReminderNotificationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfReminderNotificationSetting(navController, settingsViewModel)
        );
        binding.switchPasscodeLockValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedPasscodeLockSwitch = true;
                }
                return false;
            }
        });
        binding.switchPasscodeLockValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfPasscodeLockSetting(navController, settingsViewModel)
        );
        binding.switchGettingWeatherInformationValue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    isTouchedGettingWeatherInformationSwitch = true;
                }
                return false;
            }
        });
        binding.switchGettingWeatherInformationValue.setOnCheckedChangeListener(
                new OnCheckedChangeListenerOfGettingWeatherInformationSetting(navController, settingsViewModel)
        );

        settingsViewModel.getThemeColorLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String string) {
                ThemeColorSwitcher switcher = new ThemeColorSwitcher(getResources(), requireContext());
                ThemeColors themeColor = ThemeColors.values()[0]; // デフォルトテーマ
                TextView[] sections = {
                        binding.textSettingsSectionDesign,
                        binding.textSettingsSectionSettings,
                        binding.textSettingsSectionEnd};
                TextView[] icons = {
                        binding.textThemeColorSettingTitle,
                        binding.textCalendarStartDaySettingTitle,
                        binding.textReminderNotificationSettingTitle,
                        binding.textGettingWeatherInformationSettingTitle};
                MaterialSwitch[] switches = {
                        binding.switchReminderNotificationValue,
                        binding.switchPasscodeLockValue,
                        binding.switchGettingWeatherInformationValue};
                if (string.equals(getString(ThemeColors.WHITE.getThemeColorNameResId()))) {
                    themeColor = ThemeColors.WHITE;
                }
                if (string.equals(getString(ThemeColors.BLACK.getThemeColorNameResId()))) {
                    themeColor = ThemeColors.BLACK;
                }
                switcher.switchSectionView(themeColor, sections);
                switcher.switchTextIcon(themeColor, icons);
                switcher.switchSwitch(themeColor, switches);
            }
        });
    }

    @Override
    protected void handleOnReceivedResultFromPreviousFragment(@NonNull SavedStateHandle savedStateHandle) {
        // 処理なし
    }

    @Override
    protected void handleOnReceivedResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        receiveThemeColorPickerDialogResult(savedStateHandle);
        receiveDayOfWeekPickerDialogResult(savedStateHandle);
        receiveTimePickerDialogResult(savedStateHandle);
        receivePermissionDialogResult(savedStateHandle);
    }

    @Override
    protected void removeResulFromDialog(@NonNull SavedStateHandle savedStateHandle) {
        savedStateHandle.remove(ThemeColorPickerDialogFragment.KEY_SELECTED_THEME_COLOR);
        savedStateHandle.remove(DayOfWeekPickerDialogFragment.KEY_SELECTED_DAY_OF_WEEK);
        savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_HOUR);
        savedStateHandle.remove(TimePickerDialogFragment.KEY_SELECTED_MINUTE);
        savedStateHandle.remove(PermissionDialogFragment.KEY_SELECTED_BUTTON);
    }

    // テーマカラー設定ダイアログフラグメントから結果受取
    private void receiveThemeColorPickerDialogResult(SavedStateHandle savedStateHandle) {
        ThemeColors selectedThemeColor =
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
        settingsViewModel.registerReminderNotificationWorker(settingTime);
        settingsViewModel.saveIsReminderNotification(true);
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

        actionApplicationDetailsSettings();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class OnClickListenerOfThemeColorSetting implements View.OnClickListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnClickListenerOfThemeColorSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onClick(View v) {
            String currentThemeColorName =
                    settingsViewModel.getThemeColorLiveData().getValue();
            ThemeColors currentThemeColor = toThemeColors(currentThemeColorName);
            if (currentThemeColor == null) {
                currentThemeColor = ThemeColors.values()[0];
            }
            NavDirections action = SettingsFragmentDirections
                    .actionNavigationSettingsFragmentToThemeColorPickerDialog(currentThemeColor);
            navController.navigate(action);
        }
    }

    private ThemeColors toThemeColors(String themeColorName) {
        if (themeColorName == null || themeColorName.isEmpty()) {
            return null;
        }
        for (ThemeColors themeColor: ThemeColors.values()) {
            int themeColorNameResId = themeColor.getThemeColorNameResId();
            String _themeColorName = getString(themeColorNameResId);
            if (themeColorName.equals(_themeColorName)) {
                return themeColor;
            }
        }
        return null;
    }

    private class OnClickListenerOfCalendarStartDayOfWeekSetting implements View.OnClickListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnClickListenerOfCalendarStartDayOfWeekSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onClick(View v) {
            String currentCalendarStartDayOfWeekName =
                    settingsViewModel.getCalendarStartDayOfWeekLiveData().getValue();
            DayOfWeek currentCalendarStartDayOfWeek = toDayOfWeek(currentCalendarStartDayOfWeekName);
            if (currentCalendarStartDayOfWeek == null) {
                currentCalendarStartDayOfWeek = DayOfWeek.SUNDAY;
            }
            NavDirections action = SettingsFragmentDirections
                    .actionNavigationSettingsFragmentToDayOfWeekPickerDialog(currentCalendarStartDayOfWeek);
            navController.navigate(action);
        }
    }

    private DayOfWeek toDayOfWeek(String dayOfWeekName) {
        if (dayOfWeekName == null || dayOfWeekName.isEmpty()) {
            return null;
        }
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            DayOfWeekConverter dayOfWeekConverter = new DayOfWeekConverter(requireContext());
            String _dayOfWeekName = dayOfWeekConverter.toStringName(dayOfWeek);
            if (dayOfWeekName.equals(_dayOfWeekName)) {
                return dayOfWeek;
            }
        }
        return null;
    }

    private class OnCheckedChangeListenerOfReminderNotificationSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfReminderNotificationSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isTouchedReminderNotificationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedPostNotifications()) {
                    NavDirections action = SettingsFragmentDirections
                            .actionNavigationSettingsFragmentToTimePickerDialog();
                    navController.navigate(action);
                } else {
                    boolean shouldShowRequestPermissionRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(), Manifest.permission.POST_NOTIFICATIONS);
                    if (shouldShowRequestPermissionRationale) {
                        requestPostNotificationsPermissionLauncher
                                .launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        binding.switchReminderNotificationValue.setChecked(false);
                        String permissionName = "通知";
                        NavDirections action =
                                SettingsFragmentDirections
                                        .actionSettingsFragmentToPermissionDialog(permissionName);
                        navController.navigate(action);
                    }
                }
            } else {
                settingsViewModel.cancelReminderNotificationWorker();
                settingsViewModel.saveIsReminderNotification(false);
            }
            isTouchedReminderNotificationSwitch = false;
        }
    }

    private class OnCheckedChangeListenerOfPasscodeLockSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfPasscodeLockSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            settingsViewModel.saveIsPasscodeLock(isChecked);
        }
    }

    private class OnCheckedChangeListenerOfGettingWeatherInformationSetting
            implements CompoundButton.OnCheckedChangeListener {
        private NavController navController;
        private SettingsViewModel settingsViewModel;
        public OnCheckedChangeListenerOfGettingWeatherInformationSetting(
                NavController navController, SettingsViewModel settingsViewModel) {
            this.navController = navController;
            this.settingsViewModel = settingsViewModel;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isTouchedGettingWeatherInformationSwitch) {
                return;
            }
            if (isChecked) {
                if (isGrantedAccessLocation()) {
                    settingsViewModel.saveIsGettingWeatherInformation(true);
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
                        String permissionName = "位置情報利用";
                        NavDirections action =
                                SettingsFragmentDirections
                                        .actionSettingsFragmentToPermissionDialog(permissionName);
                        navController.navigate(action);
                    }
                }
            } else {
                settingsViewModel.saveIsGettingWeatherInformation(false);
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

    private void actionApplicationDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
